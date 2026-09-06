package com.urlshortener.service;

import com.urlshortener.dto.ClickEventDto;
import com.urlshortener.dto.LiveClickDto;
import com.urlshortener.model.UrlMapping;
import com.urlshortener.repository.WorkspaceMemberRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

@Service
public class LiveClickStreamService {

    private static final Logger log = LoggerFactory.getLogger(LiveClickStreamService.class);
    private static final Long SSE_TIMEOUT = 30 * 60 * 1000L; // 30 Dakika
    private static final int MAX_RECENT_BUFFER = 50;

    private final WorkspaceMemberRepository workspaceMemberRepository;

    // Aktif istemci abonelikleri
    private final Map<String, ClientSubscription> subscriptions = new ConcurrentHashMap<>();

    // Bellekte tutulan son tıklamalar (Yeni bağlanan istemciye başlangıç akışı sunmak için)
    private final List<LiveClickRecord> recentClicks = new CopyOnWriteArrayList<>();

    public LiveClickStreamService(WorkspaceMemberRepository workspaceMemberRepository) {
        this.workspaceMemberRepository = workspaceMemberRepository;
    }

    public static class ClientSubscription {
        private final String id;
        private final String username;
        private final boolean isAdmin;
        private final String filterShortCode;
        private final SseEmitter emitter;
        private final long connectedAt;

        public ClientSubscription(String id, String username, boolean isAdmin, String filterShortCode, SseEmitter emitter) {
            this.id = id;
            this.username = username;
            this.isAdmin = isAdmin;
            this.filterShortCode = filterShortCode;
            this.emitter = emitter;
            this.connectedAt = System.currentTimeMillis();
        }

        public String getId() { return id; }
        public String getUsername() { return username; }
        public boolean isAdmin() { return isAdmin; }
        public String getFilterShortCode() { return filterShortCode; }
        public SseEmitter getEmitter() { return emitter; }
        public long getConnectedAt() { return connectedAt; }
    }

    public static class LiveClickRecord {
        private final LiveClickDto dto;
        private final String ownerUsername;
        private final UUID workspaceId;

        public LiveClickRecord(LiveClickDto dto, String ownerUsername, UUID workspaceId) {
            this.dto = dto;
            this.ownerUsername = ownerUsername;
            this.workspaceId = workspaceId;
        }

        public LiveClickDto getDto() { return dto; }
        public String getOwnerUsername() { return ownerUsername; }
        public UUID getWorkspaceId() { return workspaceId; }
    }

    /**
     * İstemci için SSE akış aboneliği başlatır.
     */
    public SseEmitter subscribe(String username, boolean isAdmin, String filterShortCode) {
        String subId = UUID.randomUUID().toString();
        SseEmitter emitter = new SseEmitter(SSE_TIMEOUT);

        ClientSubscription subscription = new ClientSubscription(subId, username, isAdmin, filterShortCode, emitter);
        subscriptions.put(subId, subscription);

        emitter.onCompletion(() -> {
            log.debug("SSE Bağlantısı tamamlandı: {}", subId);
            subscriptions.remove(subId);
        });

        emitter.onTimeout(() -> {
            log.debug("SSE Bağlantısı zaman aşımına uğradı: {}", subId);
            subscriptions.remove(subId);
            emitter.complete();
        });

        emitter.onError((ex) -> {
            log.debug("SSE Bağlantı hatası: {}, sebep: {}", subId, ex.getMessage());
            subscriptions.remove(subId);
        });

        try {
            // 1. Bağlantı onay olayı
            Map<String, Object> connectPayload = new HashMap<>();
            connectPayload.put("subscriptionId", subId);
            connectPayload.put("status", "CONNECTED");
            connectPayload.put("timestamp", System.currentTimeMillis());
            if (filterShortCode != null) {
                connectPayload.put("filterShortCode", filterShortCode);
            }
            emitter.send(SseEmitter.event()
                    .name("connected")
                    .data(connectPayload));

            // 2. Kullanıcının erişebileceği geçmiş son tıklamaları gönder (İlk yükleme)
            List<LiveClickDto> initClicks = getRecentClicksForSubscription(subscription);
            if (!initClicks.isEmpty()) {
                emitter.send(SseEmitter.event()
                        .name("init")
                        .data(initClicks));
            }
        } catch (IOException e) {
            log.warn("İlk SSE mesajı gönderilemedi: {}", e.getMessage());
            subscriptions.remove(subId);
        }

        log.info("Yeni SSE Canlı Ziyaret Akışı abonesi bağlandı: subId={}, user={}, filter={}", subId, username, filterShortCode);
        return emitter;
    }

    /**
     * RabbitMQ consumer tarafından çağrılır; tıklama olayını bağlı tüm yetkili SSE abonelerine iletir.
     */
    public void broadcastClick(ClickEventDto event, UrlMapping mapping) {
        if (event == null || mapping == null) {
            return;
        }

        LiveClickDto liveDto = convertToLiveDto(event, mapping);

        String ownerUsername = mapping.getUser() != null ? mapping.getUser().getUsername() : null;
        UUID workspaceId = mapping.getWorkspace() != null ? mapping.getWorkspace().getId() : null;

        // Bellekteki son tıklamalara ekle
        LiveClickRecord record = new LiveClickRecord(liveDto, ownerUsername, workspaceId);
        recentClicks.add(record);
        while (recentClicks.size() > MAX_RECENT_BUFFER) {
            recentClicks.remove(0);
        }

        // Aktif abonelere yayınla
        subscriptions.values().forEach(sub -> {
            if (canAccessClick(sub, record)) {
                try {
                    sub.getEmitter().send(SseEmitter.event()
                            .name("click")
                            .data(liveDto));
                } catch (Exception ex) {
                    log.debug("SSE yayını başarısız, abone temizleniyor: {}", sub.getId());
                    subscriptions.remove(sub.getId());
                    try {
                        sub.getEmitter().complete();
                    } catch (Exception ignored) {}
                }
            }
        });
    }

    /**
     * 20 saniyede bir ping yorumu göndererek bağlantının açık kalmasını sağlar.
     */
    @Scheduled(fixedRate = 20000)
    public void sendHeartbeat() {
        if (subscriptions.isEmpty()) {
            return;
        }

        List<String> deadSubs = new ArrayList<>();
        subscriptions.values().forEach(sub -> {
            try {
                sub.getEmitter().send(SseEmitter.event()
                        .comment("keep-alive"));
            } catch (Exception ex) {
                deadSubs.add(sub.getId());
            }
        });

        deadSubs.forEach(subscriptions::remove);
    }

    /**
     * Belirli bir abone için son tıklamaları filtreler.
     */
    private List<LiveClickDto> getRecentClicksForSubscription(ClientSubscription sub) {
        List<LiveClickDto> result = new ArrayList<>();
        for (LiveClickRecord record : recentClicks) {
            if (canAccessClick(sub, record)) {
                result.add(record.getDto());
            }
        }
        return result;
    }

    /**
     * REST endpoint'i için kullanıcının son tıklamalarını döner.
     */
    public List<LiveClickDto> getRecentClicks(String username, boolean isAdmin, String filterShortCode) {
        ClientSubscription dummySub = new ClientSubscription("query", username, isAdmin, filterShortCode, null);
        return getRecentClicksForSubscription(dummySub);
    }

    /**
     * Abonenin bu tıklama olayını görme yetkisi var mı kontrol eder.
     */
    private boolean canAccessClick(ClientSubscription sub, LiveClickRecord record) {
        // 1. Eğer belirli bir link filtresi varsa önce ona bak
        if (sub.getFilterShortCode() != null && !sub.getFilterShortCode().equalsIgnoreCase(record.getDto().getShortCode())) {
            return false;
        }

        // 2. Sistem yöneticisi ise tüm trafiği görebilir
        if (sub.isAdmin()) {
            return true;
        }

        // 3. Linkin doğrudan sahibi ise görebilir
        if (record.getOwnerUsername() != null && record.getOwnerUsername().equalsIgnoreCase(sub.getUsername())) {
            return true;
        }

        // 4. Link bir çalışma alanına (Workspace) aitse ve kullanıcı o workspace'in üyesi ise görebilir
        if (record.getWorkspaceId() != null) {
            return workspaceMemberRepository.existsByWorkspaceIdAndUserUsername(record.getWorkspaceId(), sub.getUsername());
        }

        return false;
    }

    private LiveClickDto convertToLiveDto(ClickEventDto event, UrlMapping mapping) {
        String ua = event.getUserAgent();
        String deviceType = resolveDeviceType(ua, event.isBot());
        String browser = resolveBrowser(ua);
        String os = resolveOs(ua);
        String maskedIp = maskIp(event.getIpAddress());

        return LiveClickDto.builder()
                .shortCode(event.getShortCode())
                .originalUrl(mapping.getOriginalUrl())
                .clickedAt(event.getClickedAt() != null ? event.getClickedAt() : System.currentTimeMillis())
                .country(event.getCountry() != null ? event.getCountry() : "Türkiye")
                .countryCode(event.getCountryCode() != null ? event.getCountryCode() : "TR")
                .city(event.getCity() != null ? event.getCity() : "İstanbul")
                .maskedIp(maskedIp)
                .userAgent(ua)
                .deviceType(deviceType)
                .browser(browser)
                .os(os)
                .referrer(event.getReferrer() != null && !event.getReferrer().trim().isEmpty() ? event.getReferrer().trim() : "Doğrudan (Direct)")
                .bot(event.isBot())
                .botCategory(event.getBotCategory())
                .variantId(event.getVariantId())
                .variantLabel(event.getVariantLabel())
                .build();
    }

    private String maskIp(String ip) {
        if (ip == null || ip.trim().isEmpty()) return "N/A";
        String clean = ip.trim();
        if (clean.contains(".")) {
            // IPv4: 192.168.1.50 -> 192.168.***.***
            String[] parts = clean.split("\\.");
            if (parts.length == 4) {
                return parts[0] + "." + parts[1] + ".***.***";
            }
        } else if (clean.contains(":")) {
            // IPv6
            String[] parts = clean.split(":");
            if (parts.length >= 2) {
                return parts[0] + ":" + parts[1] + ":***:***";
            }
        }
        return "***";
    }

    private String resolveDeviceType(String ua, boolean isBot) {
        if (isBot) return "Bot / Crawler";
        if (ua == null || ua.isEmpty()) return "Masaüstü (Desktop)";
        String lower = ua.toLowerCase();
        if (lower.contains("ipad") || lower.contains("tablet")) return "Tablet";
        if (lower.contains("mobile") || lower.contains("android") || lower.contains("iphone") || lower.contains("ipod")) {
            return "Mobil (Mobile)";
        }
        return "Masaüstü (Desktop)";
    }

    private String resolveBrowser(String ua) {
        if (ua == null || ua.isEmpty()) return "Bilinmeyen (Unknown)";
        String lower = ua.toLowerCase();
        if (lower.contains("edg/") || lower.contains("edge/")) return "Microsoft Edge";
        if (lower.contains("opr/") || lower.contains("opera/")) return "Opera";
        if (lower.contains("chrome/") || lower.contains("crios/")) return "Google Chrome";
        if (lower.contains("firefox/") || lower.contains("fxios/")) return "Mozilla Firefox";
        if (lower.contains("safari/") && !lower.contains("chrome")) return "Apple Safari";
        if (lower.contains("curl")) return "cURL";
        if (lower.contains("postman")) return "Postman";
        return "Diğer Tarayıcı";
    }

    private String resolveOs(String ua) {
        if (ua == null || ua.isEmpty()) return "Bilinmeyen OS";
        String lower = ua.toLowerCase();
        if (lower.contains("windows")) return "Windows";
        if (lower.contains("macintosh") || lower.contains("mac os")) return "macOS";
        if (lower.contains("iphone") || lower.contains("ipad") || lower.contains("ipod")) return "iOS";
        if (lower.contains("android")) return "Android";
        if (lower.contains("linux")) return "Linux";
        return "Diğer";
    }

    public int getActiveSubscriberCount() {
        return subscriptions.size();
    }
}
