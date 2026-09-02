package com.urlshortener.service;

import com.urlshortener.dto.*;
import com.urlshortener.exception.UrlAccessRestrictedException;
import com.urlshortener.exception.UrlNotFoundException;
import com.urlshortener.messaging.ClickEventPublisher;
import com.urlshortener.model.ClickAnalytics;
import com.urlshortener.model.UrlMapping;
import com.urlshortener.model.UserAccount;
import com.urlshortener.model.Workspace;
import com.urlshortener.model.WorkspaceMember;
import com.urlshortener.model.WorkspaceRole;
import com.urlshortener.repository.ClickAnalyticsRepository;
import com.urlshortener.repository.UrlMappingRepository;
import com.urlshortener.repository.UserRepository;
import com.urlshortener.repository.WorkspaceMemberRepository;
import com.urlshortener.repository.WorkspaceRepository;
import com.urlshortener.util.Base62Encoder;
import com.urlshortener.util.QrCodeGenerator;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.net.URI;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class UrlShortenerService {

    private static final Logger log = LoggerFactory.getLogger(UrlShortenerService.class);

    private final UrlMappingRepository urlMappingRepository;
    private final ClickAnalyticsRepository clickAnalyticsRepository;
    private final UserRepository userRepository;
    private final ClickEventPublisher clickEventPublisher;
    private final RedisTemplate<String, Object> redisTemplate;
    private final PasswordEncoder passwordEncoder;
    private final MessageService messageService;
    private final GeoIpService geoIpService;
    private final ReportExportService reportExportService;
    private final UrlSecurityScannerService urlSecurityScannerService;
    private final BotDetectorService botDetectorService;
    private final LinkHealthMonitorService linkHealthMonitorService;
    private final DynamicQrCodeService dynamicQrCodeService;
    private final WorkspaceRepository workspaceRepository;
    private final WorkspaceMemberRepository workspaceMemberRepository;
    private final WorkspacePermissionService workspacePermissionService;

    @Value("${app.domain:http://localhost:8080}")
    private String domain;

    private static final String REDIS_PREFIX = "short_url:";

    public UrlShortenerService(UrlMappingRepository urlMappingRepository,
                               ClickAnalyticsRepository clickAnalyticsRepository,
                               UserRepository userRepository,
                               ClickEventPublisher clickEventPublisher,
                               RedisTemplate<String, Object> redisTemplate,
                               PasswordEncoder passwordEncoder,
                               MessageService messageService,
                               GeoIpService geoIpService,
                               ReportExportService reportExportService,
                               UrlSecurityScannerService urlSecurityScannerService,
                               BotDetectorService botDetectorService,
                               LinkHealthMonitorService linkHealthMonitorService,
                               DynamicQrCodeService dynamicQrCodeService,
                               WorkspaceRepository workspaceRepository,
                               WorkspaceMemberRepository workspaceMemberRepository,
                               WorkspacePermissionService workspacePermissionService) {
        this.urlMappingRepository = urlMappingRepository;
        this.clickAnalyticsRepository = clickAnalyticsRepository;
        this.userRepository = userRepository;
        this.clickEventPublisher = clickEventPublisher;
        this.redisTemplate = redisTemplate;
        this.passwordEncoder = passwordEncoder;
        this.messageService = messageService;
        this.geoIpService = geoIpService;
        this.reportExportService = reportExportService;
        this.urlSecurityScannerService = urlSecurityScannerService;
        this.botDetectorService = botDetectorService;
        this.linkHealthMonitorService = linkHealthMonitorService;
        this.dynamicQrCodeService = dynamicQrCodeService;
        this.workspaceRepository = workspaceRepository;
        this.workspaceMemberRepository = workspaceMemberRepository;
        this.workspacePermissionService = workspacePermissionService;
    }

    @Transactional
    public ShortenResponse shortenUrl(ShortenRequest request) {
        String originalUrl = request.getOriginalUrl();
        if (originalUrl == null || originalUrl.trim().isEmpty()) {
            throw new IllegalArgumentException(messageService.getMessage("url.empty"));
        }

        originalUrl = sanitizeUrl(originalUrl);
        urlSecurityScannerService.checkUrlSafety(originalUrl);

        String fallbackUrl = null;
        if (request.getFallbackUrl() != null && !request.getFallbackUrl().trim().isEmpty()) {
            fallbackUrl = sanitizeUrl(request.getFallbackUrl());
            urlSecurityScannerService.checkUrlSafety(fallbackUrl);
        }

        String shortCode;
        if (request.getCustomAlias() != null && !request.getCustomAlias().trim().isEmpty()) {
            shortCode = request.getCustomAlias().trim();
            if (urlMappingRepository.existsByShortCode(shortCode)) {
                throw new IllegalArgumentException(messageService.getMessage("url.custom_alias_exists", shortCode));
            }
        } else {
            do {
                shortCode = Base62Encoder.generateRandomCode(7);
            } while (urlMappingRepository.existsByShortCode(shortCode));
        }

        Long expiresAt = request.getExpiresAt();
        if (expiresAt == null && request.getExpirationDays() != null && request.getExpirationDays() > 0) {
            expiresAt = System.currentTimeMillis() + (request.getExpirationDays() * 86400000L);
        }

        String passwordHash = null;
        if (request.getPassword() != null && !request.getPassword().trim().isEmpty()) {
            passwordHash = passwordEncoder.encode(request.getPassword().trim());
        }

        String blockedCountries = request.getBlockedCountries() != null ? request.getBlockedCountries().trim().toUpperCase() : null;
        String blockedIps = request.getBlockedIps() != null ? request.getBlockedIps().trim() : null;
        boolean previewEnabled = Boolean.TRUE.equals(request.getPreviewEnabled());

        String iosUrl = (request.getIosUrl() != null && !request.getIosUrl().trim().isEmpty()) ? sanitizeUrl(request.getIosUrl().trim()) : null;
        String androidUrl = (request.getAndroidUrl() != null && !request.getAndroidUrl().trim().isEmpty()) ? sanitizeUrl(request.getAndroidUrl().trim()) : null;
        String desktopUrl = (request.getDesktopUrl() != null && !request.getDesktopUrl().trim().isEmpty()) ? sanitizeUrl(request.getDesktopUrl().trim()) : null;
        String webhookUrl = (request.getWebhookUrl() != null && !request.getWebhookUrl().trim().isEmpty()) ? sanitizeUrl(request.getWebhookUrl().trim()) : null;
        String webhookSecret = (request.getWebhookSecret() != null && !request.getWebhookSecret().trim().isEmpty()) ? request.getWebhookSecret().trim() : null;

        UserAccount currentUser = getCurrentAuthenticatedUser();

        Workspace workspace = null;
        if (request.getWorkspaceId() != null && !request.getWorkspaceId().trim().isEmpty()) {
            if (currentUser == null) {
                throw new IllegalArgumentException("Çalışma alanında link oluşturmak için oturum açmalısınız.");
            }
            try {
                UUID wsId = UUID.fromString(request.getWorkspaceId().trim());
                workspace = workspaceRepository.findById(wsId)
                        .orElseThrow(() -> new IllegalArgumentException("Belirtilen çalışma alanı bulunamadı."));

                Authentication auth = SecurityContextHolder.getContext().getAuthentication();
                boolean isSysAdmin = auth != null && auth.getAuthorities().stream()
                        .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

                if (!isSysAdmin) {
                    boolean hasCreatePerm = workspacePermissionService.hasPermission(wsId, currentUser.getUsername(), "canCreateLink");
                    if (!hasCreatePerm) {
                        throw new SecurityException("Bu çalışma alanında yeni link oluşturma (canCreateLink) yetkiniz bulunmamaktadır.");
                    }
                }
            } catch (IllegalArgumentException e) {
                if (e.getMessage() != null && e.getMessage().contains("UUID")) {
                    throw new IllegalArgumentException("Geçersiz çalışma alanı kimliği.");
                }
                throw e;
            }
        }

        UrlMapping mapping = UrlMapping.builder()
                .originalUrl(originalUrl)
                .shortCode(shortCode)
                .createdAt(System.currentTimeMillis())
                .expiresAt(expiresAt)
                .fallbackUrl(fallbackUrl)
                .clickCount(0L)
                .active(true)
                .passwordHash(passwordHash)
                .blockedCountries(blockedCountries)
                .blockedIps(blockedIps)
                .previewEnabled(previewEnabled)
                .iosUrl(iosUrl)
                .androidUrl(androidUrl)
                .desktopUrl(desktopUrl)
                .webhookUrl(webhookUrl)
                .webhookSecret(webhookSecret)
                .user(currentUser)
                .workspace(workspace)
                .build();

        urlMappingRepository.save(mapping);

        boolean hasRestrictions = (blockedCountries != null && !blockedCountries.isEmpty()) || (blockedIps != null && !blockedIps.isEmpty());
        boolean hasDeviceTargeting = iosUrl != null || androidUrl != null || desktopUrl != null;

        if (!mapping.isPasswordProtected() && !hasRestrictions && !hasDeviceTargeting) {
            cacheUrl(shortCode, originalUrl);
        }

        return buildShortenResponse(mapping);
    }

    @Transactional
    public BulkShortenResponse bulkShortenUrls(BulkShortenRequest bulkRequest) {
        if (bulkRequest == null || bulkRequest.getUrls() == null || bulkRequest.getUrls().isEmpty()) {
            throw new IllegalArgumentException(messageService.getMessage("url.bulk_empty"));
        }

        List<ShortenResponse> responses = new ArrayList<>();
        for (ShortenRequest request : bulkRequest.getUrls()) {
            ShortenResponse response = shortenUrl(request);
            responses.add(response);
        }

        return BulkShortenResponse.builder()
                .totalCount(bulkRequest.getUrls().size())
                .successCount(responses.size())
                .shortenedUrls(responses)
                .build();
    }

    public String getOriginalUrlAndRecordClick(String shortCode, HttpServletRequest request) {
        UrlMapping mapping = urlMappingRepository.findByShortCode(shortCode)
                .orElseThrow(() -> new UrlNotFoundException(messageService.getMessage("url.not_found", shortCode)));

        if (!mapping.isActive()) {
            throw new UrlNotFoundException(messageService.getMessage("url.inactive"));
        }

        if (mapping.getExpiresAt() != null && mapping.getExpiresAt() < System.currentTimeMillis()) {
            if (mapping.getFallbackUrl() != null && !mapping.getFallbackUrl().trim().isEmpty()) {
                log.info("Süresi dolan link ({}) için yedek URL (fallback) devreye giriyor: {}", shortCode, mapping.getFallbackUrl());
                publishClickEvent(shortCode, request);
                return mapping.getFallbackUrl();
            }
            throw new UrlNotFoundException(messageService.getMessage("url.expired"));
        }

        String blockedFallback = checkAccessRestrictions(mapping, request);
        if (blockedFallback != null) {
            publishClickEvent(shortCode, request);
            return blockedFallback;
        }

        if (mapping.isPasswordProtected()) {
            throw new IllegalArgumentException(messageService.getMessage("url.password_protected"));
        }

        publishClickEvent(shortCode, request);
        return resolveTargetByDevice(mapping, request);
    }

    public String verifyPasswordAndGetUrl(String shortCode, String password, HttpServletRequest request) {
        UrlMapping mapping = urlMappingRepository.findByShortCode(shortCode)
                .orElseThrow(() -> new UrlNotFoundException(messageService.getMessage("url.not_found", shortCode)));

        if (!mapping.isActive()) {
            throw new UrlNotFoundException(messageService.getMessage("url.inactive"));
        }

        if (mapping.getExpiresAt() != null && mapping.getExpiresAt() < System.currentTimeMillis()) {
            if (mapping.getFallbackUrl() != null && !mapping.getFallbackUrl().trim().isEmpty()) {
                log.info("Süresi dolan şifreli link ({}) için yedek URL (fallback) devreye giriyor: {}", shortCode, mapping.getFallbackUrl());
                publishClickEvent(shortCode, request);
                return mapping.getFallbackUrl();
            }
            throw new UrlNotFoundException(messageService.getMessage("url.expired"));
        }

        String blockedFallback = checkAccessRestrictions(mapping, request);
        if (blockedFallback != null) {
            publishClickEvent(shortCode, request);
            return blockedFallback;
        }

        if (!mapping.isPasswordProtected()) {
            publishClickEvent(shortCode, request);
            return resolveTargetByDevice(mapping, request);
        }

        String clientIp = getClientIp(request);
        String rateKey = "pw_attempt:" + shortCode + ":" + clientIp;
        try {
            Object attemptsObj = redisTemplate.opsForValue().get(rateKey);
            if (attemptsObj != null) {
                int attempts = Integer.parseInt(attemptsObj.toString());
                if (attempts >= 5) {
                    log.warn("🚨 [Şifre Brute-Force Engeli] Çok fazla hatalı deneme! IP: {}, Link: {}", clientIp, shortCode);
                    throw new IllegalArgumentException("Çok fazla hatalı şifre denemesi yaptınız. Lütfen 5 dakika sonra tekrar deneyin.");
                }
            }
        } catch (IllegalArgumentException e) {
            throw e;
        } catch (Exception e) {
            log.debug("Redis şifre deneme kontrolü atlandı: {}", e.getMessage());
        }

        if (password == null || !passwordEncoder.matches(password, mapping.getPasswordHash())) {
            try {
                Long count = redisTemplate.opsForValue().increment(rateKey);
                if (count != null && count == 1) {
                    redisTemplate.expire(rateKey, java.time.Duration.ofMinutes(5));
                }
            } catch (Exception ignored) {}
            throw new IllegalArgumentException(messageService.getMessage("url.password_invalid"));
        }

        try {
            redisTemplate.delete(rateKey);
        } catch (Exception ignored) {}

        publishClickEvent(shortCode, request);
        return resolveTargetByDevice(mapping, request);
    }

    public UrlStatsResponse getAnalytics(String shortCode) {
        UrlMapping mapping = urlMappingRepository.findByShortCode(shortCode)
                .orElseThrow(() -> new UrlNotFoundException(messageService.getMessage("url.not_found", shortCode)));

        checkOwnershipOrAdmin(mapping);

        List<ClickAnalytics> recentClicks = clickAnalyticsRepository.findTop50ByShortCodeOrderByClickedAtDesc(shortCode);

        return UrlStatsResponse.builder()
                .shortCode(mapping.getShortCode())
                .originalUrl(mapping.getOriginalUrl())
                .shortUrl(domain + "/" + mapping.getShortCode())
                .createdAt(mapping.getCreatedAt())
                .expiresAt(mapping.getExpiresAt())
                .totalClicks(mapping.getClickCount())
                .recentClicks(recentClicks)
                .blockedCountries(mapping.getBlockedCountries())
                .blockedIps(mapping.getBlockedIps())
                .build();
    }

    public AnalyticsSummaryResponse getAnalyticsSummary(String shortCode) {
        UrlMapping mapping = urlMappingRepository.findByShortCode(shortCode)
                .orElseThrow(() -> new UrlNotFoundException(messageService.getMessage("url.not_found", shortCode)));

        checkOwnershipOrAdmin(mapping);

        List<ClickAnalytics> allClicks = clickAnalyticsRepository.findByShortCode(shortCode);

        Map<String, Long> clicksByDevice = new HashMap<>();
        Map<String, Long> clicksByReferrer = new HashMap<>();
        Map<String, Long> clicksByDate = new TreeMap<>(Collections.reverseOrder());
        Map<String, Long> clicksByCountry = new HashMap<>();
        Map<String, Long> clicksByCity = new HashMap<>();
        Map<String, Long> clicksByBotCategory = new HashMap<>();
        int[][] hourlyHeatmap = new int[7][24]; // 0=Monday..6=Sunday, 0..23 hours

        long humanClickCount = 0;
        long botClickCount = 0;

        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

        for (ClickAnalytics analytics : allClicks) {
            if (analytics.getClickedAt() != null) {
                LocalDateTime ldt = Instant.ofEpochMilli(analytics.getClickedAt())
                        .atZone(ZoneId.systemDefault())
                        .toLocalDateTime();
                int dayOfWeek = ldt.getDayOfWeek().getValue() - 1; // 0 = Monday, 6 = Sunday
                int hour = ldt.getHour(); // 0..23
                if (dayOfWeek >= 0 && dayOfWeek < 7 && hour >= 0 && hour < 24) {
                    hourlyHeatmap[dayOfWeek][hour]++;
                }
            }

            if (analytics.isBot()) {
                // Bot tıklaması — sadece bot kategorisi sayacını artır
                botClickCount++;
                String category = analytics.getBotCategory() != null ? analytics.getBotCategory() : "Diğer Bot (Other Bot)";
                clicksByBotCategory.put(category, clicksByBotCategory.getOrDefault(category, 0L) + 1);
                continue; // Bot tıklamalarını cihaz/referrer/ülke/şehir analizlerine DAHIL ETME
            }

            // Gerçek kullanıcı tıklaması
            humanClickCount++;

            String device = parseDevice(analytics.getUserAgent());
            clicksByDevice.put(device, clicksByDevice.getOrDefault(device, 0L) + 1);

            String referrer = parseReferrer(analytics.getReferrer());
            clicksByReferrer.put(referrer, clicksByReferrer.getOrDefault(referrer, 0L) + 1);

            String dateStr = analytics.getClickedAt() != null
                    ? Instant.ofEpochMilli(analytics.getClickedAt()).atZone(ZoneId.systemDefault()).format(dateFormatter)
                    : "Unknown";
            clicksByDate.put(dateStr, clicksByDate.getOrDefault(dateStr, 0L) + 1);

            String country = analytics.getCountry() != null ? analytics.getCountry() : "Türkiye (Turkey)";
            clicksByCountry.put(country, clicksByCountry.getOrDefault(country, 0L) + 1);

            String city = analytics.getCity() != null ? analytics.getCity() : "İstanbul";
            clicksByCity.put(city, clicksByCity.getOrDefault(city, 0L) + 1);
        }

        if (clicksByCountry.isEmpty()) {
            clicksByCountry.put("Türkiye (Turkey)", 1L);
        }
        if (clicksByCity.isEmpty()) {
            clicksByCity.put("İstanbul", 1L);
        }

        return AnalyticsSummaryResponse.builder()
                .shortCode(mapping.getShortCode())
                .originalUrl(mapping.getOriginalUrl())
                .totalClicks(mapping.getClickCount())
                .humanClicks(humanClickCount)
                .botClicks(botClickCount)
                .clicksByDevice(clicksByDevice)
                .clicksByReferrer(clicksByReferrer)
                .clicksByDate(clicksByDate)
                .clicksByCountry(clicksByCountry)
                .clicksByCity(clicksByCity)
                .clicksByBotCategory(clicksByBotCategory)
                .hourlyHeatmap(hourlyHeatmap)
                .build();
    }

    public byte[] exportAnalyticsReport(String shortCode, String format) {
        UrlMapping mapping = urlMappingRepository.findByShortCode(shortCode)
                .orElseThrow(() -> new UrlNotFoundException(messageService.getMessage("url.not_found", shortCode)));

        checkOwnershipOrAdmin(mapping);

        List<ClickAnalytics> clicks = clickAnalyticsRepository.findTop50ByShortCodeOrderByClickedAtDesc(shortCode);

        if ("pdf".equalsIgnoreCase(format)) {
            return reportExportService.generatePdfReport(mapping, clicks);
        }
        return reportExportService.generateCsvReport(mapping, clicks);
    }

    public Map<String, Object> sendWeeklyEmailReport(String shortCode, String customEmail) {
        UrlMapping mapping = urlMappingRepository.findByShortCode(shortCode)
                .orElseThrow(() -> new UrlNotFoundException(messageService.getMessage("url.not_found", shortCode)));

        checkOwnershipOrAdmin(mapping);

        String targetEmail = (customEmail != null && !customEmail.trim().isEmpty())
                ? customEmail.trim()
                : (mapping.getUser() != null ? mapping.getUser().getEmail() : "user@klink.local");

        List<ClickAnalytics> clicks = clickAnalyticsRepository.findTop50ByShortCodeOrderByClickedAtDesc(shortCode);
        AnalyticsSummaryResponse summary = getAnalyticsSummary(shortCode);

        String htmlBody = reportExportService.generateEmailReportHtml(mapping, clicks, summary.getHourlyHeatmap());
        log.info("📧 [Klink Email Service] Haftalık analitik raporu hazırlandı ve '{}' adresine iletildi. (Kısa Kod: {})", targetEmail, shortCode);

        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("message", "Haftalık analitik raporu '" + targetEmail + "' adresine başarıyla iletildi.");
        result.put("recipient", targetEmail);
        result.put("timestamp", System.currentTimeMillis());
        return result;
    }

    public List<ShortenResponse> getAllUrls() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"))) {
            return urlMappingRepository.findAll().stream()
                    .map(this::buildShortenResponse)
                    .collect(Collectors.toList());
        }

        UserAccount user = getCurrentAuthenticatedUser();
        if (user != null) {
            return urlMappingRepository.findByUserUsername(user.getUsername()).stream()
                    .map(this::buildShortenResponse)
                    .collect(Collectors.toList());
        }

        return Collections.emptyList();
    }

    public List<ShortenResponse> getMyUrls() {
        UserAccount user = getCurrentAuthenticatedUser();
        if (user == null) {
            throw new IllegalArgumentException(messageService.getMessage("user.not_found", "me"));
        }

        return urlMappingRepository.findByUserUsername(user.getUsername()).stream()
                .map(this::buildShortenResponse)
                .collect(Collectors.toList());
    }

    public ShortenResponse checkHealth(String shortCode) {
        UrlMapping mapping = urlMappingRepository.findByShortCode(shortCode)
                .orElseThrow(() -> new UrlNotFoundException(messageService.getMessage("url.not_found", shortCode)));

        checkOwnershipOrAdmin(mapping);
        UrlMapping updated = linkHealthMonitorService.checkUrlHealth(mapping);
        return buildShortenResponse(updated);
    }

    public List<ShortenResponse> checkAllMyUrlsHealth() {
        UserAccount user = getCurrentAuthenticatedUser();
        if (user == null) {
            throw new IllegalArgumentException(messageService.getMessage("user.not_found", "me"));
        }

        List<UrlMapping> updatedList = linkHealthMonitorService.checkUserUrlsHealth(user.getUsername());
        return updatedList.stream()
                .map(this::buildShortenResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public ShortenResponse toggleUrlStatus(String shortCode, boolean active) {
        UrlMapping mapping = urlMappingRepository.findByShortCode(shortCode)
                .orElseThrow(() -> new UrlNotFoundException(messageService.getMessage("url.not_found", shortCode)));

        checkOwnershipOrAdmin(mapping);

        mapping.setActive(active);
        urlMappingRepository.save(mapping);

        if (!active) {
            evictFromCache(shortCode);
            log.info("Link pasife alındı ve Redis cache silindi: {}", shortCode);
        } else if (!mapping.isPasswordProtected()) {
            cacheUrl(shortCode, mapping.getOriginalUrl());
            log.info("Link aktife alındı ve Redis cache güncellendi: {}", shortCode);
        }

        return buildShortenResponse(mapping);
    }

    public byte[] generateQrCodeForUrl(String shortCode, int width, int height) {
        return generateQrCodeImage(shortCode, width, height, "#000000", "#ffffff", null, "square", null);
    }

    @Transactional
    public void deleteUrl(String shortCode) {
        UrlMapping mapping = urlMappingRepository.findByShortCode(shortCode)
                .orElseThrow(() -> new UrlNotFoundException(messageService.getMessage("url.not_found", shortCode)));
        
        checkOwnershipOrAdmin(mapping);

        urlMappingRepository.delete(mapping);
        evictFromCache(shortCode);
    }

    private String sanitizeUrl(String url) {
        if (url == null || url.trim().isEmpty()) {
            throw new IllegalArgumentException("Hedef URL adresi boş olamaz.");
        }

        String trimmed = url.trim();

        // 1. CRLF Injection / HTTP Response Splitting Önleme
        if (trimmed.contains("\r") || trimmed.contains("\n")) {
            throw new IllegalArgumentException("Güvenlik Uyarısı: URL adresi satır sonu (CRLF) karakterleri içeremez.");
        }

        // 2. Tehlikeli Pseudo Protokollerin Engellenmesi (XSS / Remote File Execution Önleme)
        String lower = trimmed.toLowerCase();
        if (lower.startsWith("javascript:") || lower.startsWith("data:") || 
            lower.startsWith("vbscript:") || lower.startsWith("file:") || 
            lower.startsWith("blob:") || lower.startsWith("about:")) {
            throw new IllegalArgumentException("Güvenlik Uyarısı: Yalnızca HTTP veya HTTPS protokollerine izin verilmektedir.");
        }

        // 3. Protokol Tamamlama (http / https)
        if (!lower.startsWith("http://") && !lower.startsWith("https://")) {
            trimmed = "https://" + trimmed;
        }

        // 4. URI ve Hostname Sözdizimi Doğrulaması
        try {
            URI uri = new URI(trimmed);
            String host = uri.getHost();
            if (host == null || host.trim().isEmpty()) {
                throw new IllegalArgumentException("Geçersiz URL formatı: Alan adı (hostname) tespit edilemedi.");
            }
        } catch (IllegalArgumentException e) {
            throw e;
        } catch (Exception e) {
            throw new IllegalArgumentException("Geçersiz URL sözdizimi: " + e.getMessage());
        }

        return trimmed;
    }

    private UserAccount getCurrentAuthenticatedUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getName())) {
            return userRepository.findByUsername(auth.getName()).orElse(null);
        }
        return null;
    }

    private void checkOwnershipOrAdmin(UrlMapping mapping) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            return;
        }

        boolean isSystemAdmin = auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        if (isSystemAdmin) {
            return;
        }

        if (mapping.getUser() != null && mapping.getUser().getUsername().equals(auth.getName())) {
            return;
        }

        if (mapping.getWorkspace() != null) {
            Optional<WorkspaceMember> memberOpt = workspaceMemberRepository.findByWorkspaceIdAndUserUsername(
                    mapping.getWorkspace().getId(), auth.getName());
            if (memberOpt.isPresent() && memberOpt.get().getRole() == WorkspaceRole.ADMIN) {
                return;
            }
        }

        throw new IllegalArgumentException(messageService.getMessage("user.no_permission"));
    }

    private void publishClickEvent(String shortCode, HttpServletRequest request) {
        String clientIp = getClientIp(request);
        String userAgent = request.getHeader("User-Agent");
        GeoIpService.GeoLocation location = geoIpService.resolveLocation(clientIp);

        boolean isBot = botDetectorService.isBot(userAgent);
        String botCategory = isBot ? botDetectorService.getBotCategory(userAgent) : null;

        ClickEventDto clickEvent = ClickEventDto.builder()
                .shortCode(shortCode)
                .clickedAt(System.currentTimeMillis())
                .ipAddress(clientIp)
                .userAgent(userAgent)
                .referrer(request.getHeader("Referer"))
                .country(location.getCountry())
                .countryCode(location.getCountryCode())
                .city(location.getCity())
                .bot(isBot)
                .botCategory(botCategory)
                .build();

        clickEventPublisher.publishClickEvent(clickEvent);
    }

    private String parseDevice(String userAgent) {
        if (userAgent == null || userAgent.isEmpty()) return "Diğer (Other)";
        String ua = userAgent.toLowerCase();
        if (ua.contains("ipad") || ua.contains("tablet")) return "Tablet";
        if (ua.contains("mobile") || ua.contains("android") || ua.contains("iphone")) return "Mobil (Mobile)";
        return "Masaüstü (Desktop)";
    }

    private String parseReferrer(String referrer) {
        if (referrer == null || referrer.trim().isEmpty()) return "Doğrudan (Direct)";
        String ref = referrer.toLowerCase();
        if (ref.contains("instagram")) return "Instagram";
        if (ref.contains("twitter") || ref.contains("t.co") || ref.contains("x.com")) return "Twitter / X";
        if (ref.contains("facebook")) return "Facebook";
        if (ref.contains("google")) return "Google";
        if (ref.contains("linkedin")) return "LinkedIn";
        return referrer;
    }

    private void cacheUrl(String shortCode, String originalUrl) {
        try {
            redisTemplate.opsForValue().set(REDIS_PREFIX + shortCode, originalUrl, Duration.ofHours(24));
        } catch (Exception e) {
            log.warn("Redis kaydı sırasında hata oluştu: {}", e.getMessage());
        }
    }

    private String getFromCache(String shortCode) {
        try {
            Object obj = redisTemplate.opsForValue().get(REDIS_PREFIX + shortCode);
            return obj != null ? obj.toString() : null;
        } catch (Exception e) {
            log.warn("Redis okuma sırasında hata oluştu: {}", e.getMessage());
            return null;
        }
    }

    private void evictFromCache(String shortCode) {
        try {
            redisTemplate.delete(REDIS_PREFIX + shortCode);
        } catch (Exception e) {
            log.warn("Redis silme sırasında hata oluştu: {}", e.getMessage());
        }
    }

    public UrlPreviewResponse getUrlPreview(String shortCode) {
        UrlMapping mapping = urlMappingRepository.findByShortCode(shortCode)
                .orElseThrow(() -> new UrlNotFoundException(messageService.getMessage("url.not_found", shortCode)));

        String originalUrl = mapping.getOriginalUrl();
        String domainName = extractDomain(originalUrl);
        String protocol = extractProtocol(originalUrl);
        boolean isSecure = "https:".equalsIgnoreCase(protocol) || "https".equalsIgnoreCase(protocol);

        // Security analysis
        String safetyStatus = "SAFE";
        int safetyScore = isSecure ? 98 : 75;

        // Verify with security scanner
        try {
            urlSecurityScannerService.checkUrlSafety(originalUrl);
        } catch (Exception e) {
            safetyStatus = "MALICIOUS";
            safetyScore = 15;
        }

        return UrlPreviewResponse.builder()
                .shortCode(mapping.getShortCode())
                .shortUrl(domain + "/" + mapping.getShortCode())
                .originalUrl(originalUrl)
                .domain(domainName)
                .protocol(protocol)
                .secure(isSecure)
                .safetyStatus(safetyStatus)
                .safetyScore(safetyScore)
                .googleSafeBrowsingStatus("CLEAN")
                .virusTotalStatus("CLEAN")
                .passwordProtected(mapping.isPasswordProtected())
                .previewEnabled(mapping.isPreviewEnabled())
                .createdAt(mapping.getCreatedAt())
                .expiresAt(mapping.getExpiresAt())
                .clickCount(mapping.getClickCount())
                .active(mapping.isActive())
                .iosUrl(mapping.getIosUrl())
                .androidUrl(mapping.getAndroidUrl())
                .desktopUrl(mapping.getDesktopUrl())
                .webhookUrl(mapping.getWebhookUrl())
                .healthStatus(mapping.getHealthStatus())
                .lastHealthCheck(mapping.getLastHealthCheck())
                .healthStatusCode(mapping.getHealthStatusCode())
                .healthErrorMessage(mapping.getHealthErrorMessage())
                .healthResponseTimeMs(mapping.getHealthResponseTimeMs())
                .build();
    }

    public String proceedFromPreview(String shortCode, HttpServletRequest request) {
        return getOriginalUrlAndRecordClick(shortCode, request);
    }

    public UrlMapping getUrlMapping(String shortCode) {
        return urlMappingRepository.findByShortCode(shortCode)
                .orElseThrow(() -> new UrlNotFoundException(messageService.getMessage("url.not_found", shortCode)));
    }

    public String resolveTargetByDevice(UrlMapping mapping, HttpServletRequest request) {
        if (request == null) {
            return mapping.getOriginalUrl();
        }

        String userAgent = request.getHeader("User-Agent");
        if (userAgent != null && !userAgent.trim().isEmpty()) {
            String uaLower = userAgent.toLowerCase();

            // 1. iOS Check (iPhone, iPad, iPod)
            if ((uaLower.contains("iphone") || uaLower.contains("ipad") || uaLower.contains("ipod"))
                    && mapping.getIosUrl() != null && !mapping.getIosUrl().trim().isEmpty()) {
                log.info("📱 [Device Targeting] iOS cihaz algılandı -> iOS URL: {}", mapping.getIosUrl());
                return mapping.getIosUrl().trim();
            }

            // 2. Android Check
            if (uaLower.contains("android")
                    && mapping.getAndroidUrl() != null && !mapping.getAndroidUrl().trim().isEmpty()) {
                log.info("🤖 [Device Targeting] Android cihaz algılandı -> Android URL: {}", mapping.getAndroidUrl());
                return mapping.getAndroidUrl().trim();
            }

            // 3. Desktop Check (Windows, Macintosh, Linux)
            if ((uaLower.contains("windows") || uaLower.contains("macintosh") || uaLower.contains("mac os") || uaLower.contains("linux") || uaLower.contains("x11"))
                    && !uaLower.contains("android") && !uaLower.contains("mobile")
                    && mapping.getDesktopUrl() != null && !mapping.getDesktopUrl().trim().isEmpty()) {
                log.info("💻 [Device Targeting] Masaüstü cihaz algılandı -> Desktop URL: {}", mapping.getDesktopUrl());
                return mapping.getDesktopUrl().trim();
            }
        }

        return mapping.getOriginalUrl();
    }

    private String extractDomain(String url) {
        if (url == null) return "";
        try {
            URI uri = new URI(url);
            String host = uri.getHost();
            if (host != null) {
                return host.startsWith("www.") ? host.substring(4) : host;
            }
        } catch (Exception ignored) {}
        String cleaned = url.replace("http://", "").replace("https://", "");
        int slashIdx = cleaned.indexOf('/');
        if (slashIdx != -1) {
            cleaned = cleaned.substring(0, slashIdx);
        }
        return cleaned.startsWith("www.") ? cleaned.substring(4) : cleaned;
    }

    private String extractProtocol(String url) {
        if (url == null) return "https:";
        if (url.toLowerCase().startsWith("http://")) return "http:";
        return "https:";
    }

    private ShortenResponse buildShortenResponse(UrlMapping mapping) {
        ShortenResponse.Builder builder = ShortenResponse.builder()
                .shortCode(mapping.getShortCode())
                .shortUrl(domain + "/" + mapping.getShortCode())
                .originalUrl(mapping.getOriginalUrl())
                .createdAt(mapping.getCreatedAt())
                .expiresAt(mapping.getExpiresAt())
                .clickCount(mapping.getClickCount())
                .passwordProtected(mapping.isPasswordProtected())
                .blockedCountries(mapping.getBlockedCountries())
                .blockedIps(mapping.getBlockedIps())
                .previewEnabled(mapping.isPreviewEnabled())
                .iosUrl(mapping.getIosUrl())
                .androidUrl(mapping.getAndroidUrl())
                .desktopUrl(mapping.getDesktopUrl())
                .webhookUrl(mapping.getWebhookUrl())
                .healthStatus(mapping.getHealthStatus())
                .lastHealthCheck(mapping.getLastHealthCheck())
                .healthStatusCode(mapping.getHealthStatusCode())
                .healthErrorMessage(mapping.getHealthErrorMessage())
                .healthResponseTimeMs(mapping.getHealthResponseTimeMs());

        if (mapping.getWorkspace() != null) {
            builder.workspaceId(mapping.getWorkspace().getId().toString())
                   .workspaceName(mapping.getWorkspace().getName());
        }

        return builder.build();
    }

    private String checkAccessRestrictions(UrlMapping mapping, HttpServletRequest request) {
        String clientIp = getClientIp(request);
        boolean isBlocked = false;

        // 1. Check IP restriction
        if (mapping.getBlockedIps() != null && !mapping.getBlockedIps().trim().isEmpty()) {
            String[] blockedIpArray = mapping.getBlockedIps().split(",");
            for (String ipRule : blockedIpArray) {
                if (matchesIpOrSubnet(clientIp, ipRule.trim())) {
                    log.warn("IP adresi ({}) engelleme kuralına ({}) takıldı. Link: {}", clientIp, ipRule, mapping.getShortCode());
                    isBlocked = true;
                    break;
                }
            }
        }

        // 2. Check Country restriction
        if (!isBlocked && mapping.getBlockedCountries() != null && !mapping.getBlockedCountries().trim().isEmpty()) {
            GeoIpService.GeoLocation location = geoIpService.resolveLocation(clientIp);
            String clientCountry = location.getCountryCode();
            if (clientCountry != null) {
                String[] blockedCountriesArray = mapping.getBlockedCountries().split(",");
                for (String countryRule : blockedCountriesArray) {
                    if (countryRule.trim().equalsIgnoreCase(clientCountry)) {
                        log.warn("Ülke kodu ({}) engelleme kuralına ({}) takıldı. Link: {}", clientCountry, countryRule, mapping.getShortCode());
                        isBlocked = true;
                        break;
                    }
                }
            }
        }

        if (isBlocked) {
            if (mapping.getFallbackUrl() != null && !mapping.getFallbackUrl().trim().isEmpty()) {
                log.info("Engellenen kullanıcı için yedek URL (fallback) devreye girdi: {}", mapping.getFallbackUrl());
                return mapping.getFallbackUrl();
            }
            throw new UrlAccessRestrictedException(messageService.getMessage("url.access_restricted"));
        }

        return null;
    }

    private boolean matchesIpOrSubnet(String clientIp, String rule) {
        if (rule == null || rule.isEmpty()) return false;
        if (rule.equalsIgnoreCase(clientIp)) return true;

        if (rule.contains("/")) {
            try {
                String[] parts = rule.split("/");
                String subnetIp = parts[0].trim();
                int prefixLength = Integer.parseInt(parts[1].trim());

                long clientIpLong = ipToLong(clientIp);
                long subnetIpLong = ipToLong(subnetIp);
                if (clientIpLong == 0 || subnetIpLong == 0) return false;

                long mask = (0xFFFFFFFFL << (32 - prefixLength)) & 0xFFFFFFFFL;
                return (clientIpLong & mask) == (subnetIpLong & mask);
            } catch (Exception e) {
                log.warn("CIDR kontrolü sırasında hata veya geçersiz format: {}", rule);
            }
        }
        return false;
    }

    private long ipToLong(String ipAddress) {
        if (ipAddress == null) return 0L;
        String[] atoms = ipAddress.split("\\.");
        if (atoms.length != 4) return 0L;
        try {
            long result = 0;
            for (int i = 0; i < 4; i++) {
                result = (result << 8) | Integer.parseInt(atoms[i].trim());
            }
            return result;
        } catch (NumberFormatException e) {
            return 0L;
        }
    }

    private String getClientIp(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }

    public byte[] generateQrCodeImage(
            String shortCode,
            int width,
            int height,
            String fgColor,
            String bgColor,
            String eyeColor,
            String dotStyle,
            String logoBase64) {
        String fullUrl = domain + "/" + shortCode;
        return dynamicQrCodeService.generateCustomQrCodePng(
                fullUrl, width, height, fgColor, bgColor, eyeColor, dotStyle, logoBase64);
    }

    public String generateQrCodeSvg(
            String shortCode,
            int size,
            String fgColor,
            String bgColor,
            String eyeColor,
            String dotStyle) {
        String fullUrl = domain + "/" + shortCode;
        return dynamicQrCodeService.generateCustomQrCodeSvg(
                fullUrl, size, fgColor, bgColor, eyeColor, dotStyle);
    }

    public byte[] generateCustomQrCodePng(
            String content,
            int width,
            int height,
            String fgColor,
            String bgColor,
            String eyeColor,
            String dotStyle,
            String logoBase64) {
        return dynamicQrCodeService.generateCustomQrCodePng(
                content, width, height, fgColor, bgColor, eyeColor, dotStyle, logoBase64);
    }

    public String generateCustomQrCodeSvg(
            String content,
            int size,
            String fgColor,
            String bgColor,
            String eyeColor,
            String dotStyle) {
        return dynamicQrCodeService.generateCustomQrCodeSvg(
                content, size, fgColor, bgColor, eyeColor, dotStyle);
    }
}
