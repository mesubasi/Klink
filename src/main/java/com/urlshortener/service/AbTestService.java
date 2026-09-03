package com.urlshortener.service;

import com.urlshortener.dto.AbTestConfigResponse;
import com.urlshortener.dto.UpdateAbTestConfigRequest;
import com.urlshortener.dto.UrlVariantRequest;
import com.urlshortener.dto.UrlVariantResponse;
import com.urlshortener.model.UrlMapping;
import com.urlshortener.model.UrlVariant;
import com.urlshortener.model.UserAccount;
import com.urlshortener.model.WorkspaceMember;
import com.urlshortener.model.WorkspaceRole;
import com.urlshortener.repository.UrlMappingRepository;
import com.urlshortener.repository.UrlVariantRepository;
import com.urlshortener.repository.UserRepository;
import com.urlshortener.repository.WorkspaceMemberRepository;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

@Service
public class AbTestService {

    private static final Logger log = LoggerFactory.getLogger(AbTestService.class);
    private static final String COOKIE_PREFIX = "klink_ab_";

    private final UrlMappingRepository urlMappingRepository;
    private final UrlVariantRepository urlVariantRepository;
    private final UserRepository userRepository;
    private final WorkspaceMemberRepository workspaceMemberRepository;
    private final RedisTemplate<String, Object> redisTemplate;

    public AbTestService(UrlMappingRepository urlMappingRepository,
                         UrlVariantRepository urlVariantRepository,
                         UserRepository userRepository,
                         WorkspaceMemberRepository workspaceMemberRepository,
                         RedisTemplate<String, Object> redisTemplate) {
        this.urlMappingRepository = urlMappingRepository;
        this.urlVariantRepository = urlVariantRepository;
        this.userRepository = userRepository;
        this.workspaceMemberRepository = workspaceMemberRepository;
        this.redisTemplate = redisTemplate;
    }

    @Transactional(readOnly = true)
    public AbTestConfigResponse getAbTestConfig(String shortCode) {
        UrlMapping mapping = urlMappingRepository.findByShortCode(shortCode)
                .orElseThrow(() -> new IllegalArgumentException("Kısa link bulunamadı: " + shortCode));

        List<UrlVariant> variants = urlVariantRepository.findByUrlMappingId(mapping.getId());
        long totalVariantClicks = variants.stream().mapToLong(UrlVariant::getClickCount).sum();

        List<UrlVariantResponse> variantResponses = variants.stream().map(v -> {
            double share = totalVariantClicks > 0
                    ? Math.round(((double) v.getClickCount() / totalVariantClicks) * 1000.0) / 10.0
                    : 0.0;
            return new UrlVariantResponse(
                    v.getId(),
                    v.getLabel(),
                    v.getTargetUrl(),
                    v.getWeightPercent(),
                    v.getClickCount(),
                    share,
                    v.isActive(),
                    v.getCreatedAt()
            );
        }).toList();

        return new AbTestConfigResponse(
                mapping.getShortCode(),
                mapping.isAbTestingEnabled(),
                variantResponses,
                totalVariantClicks
        );
    }

    @Transactional
    public AbTestConfigResponse updateAbTestConfig(String shortCode, UpdateAbTestConfigRequest request) {
        UrlMapping mapping = urlMappingRepository.findByShortCode(shortCode)
                .orElseThrow(() -> new IllegalArgumentException("Kısa link bulunamadı: " + shortCode));

        checkOwnershipOrAdmin(mapping);

        boolean enable = Boolean.TRUE.equals(request.getAbTestingEnabled());
        List<UrlVariantRequest> requestedVariants = request.getVariants() != null ? request.getVariants() : Collections.emptyList();

        if (enable) {
            if (requestedVariants.size() < 2) {
                throw new IllegalArgumentException("A/B testi için en az 2 hedef varyant tanımlanmalıdır.");
            }

            int totalWeight = requestedVariants.stream()
                    .mapToInt(v -> v.getWeightPercent() != null ? v.getWeightPercent() : 0)
                    .sum();

            if (totalWeight != 100) {
                throw new IllegalArgumentException("Varyant ağırlıklarının toplamı tam olarak %100 olmalıdır. Şu anki toplam: %" + totalWeight);
            }

            // Mevcut varyantları temizle ve yenilerini ekle
            urlVariantRepository.deleteByUrlMappingId(mapping.getId());

            for (UrlVariantRequest vr : requestedVariants) {
                UrlVariant variant = UrlVariant.builder()
                        .urlMapping(mapping)
                        .label(vr.getLabel().trim())
                        .targetUrl(vr.getTargetUrl().trim())
                        .weightPercent(vr.getWeightPercent())
                        .clickCount(0L)
                        .active(true)
                        .createdAt(System.currentTimeMillis())
                        .build();
                urlVariantRepository.save(variant);
            }
        }

        mapping.setAbTestingEnabled(enable);
        urlMappingRepository.save(mapping);

        // Redis önbelleğini temizle
        try {
            redisTemplate.delete("short_url:" + shortCode);
        } catch (Exception e) {
            log.warn("Redis önbellek temizleme hatası: {}", e.getMessage());
        }

        log.info("A/B testi güncellendi: shortCode={}, enabled={}, varyantSayisi={}", 
                shortCode, enable, requestedVariants.size());

        return getAbTestConfig(shortCode);
    }

    @Transactional
    public UrlVariant selectVariant(UrlMapping mapping, HttpServletRequest request, HttpServletResponse response) {
        List<UrlVariant> variants = urlVariantRepository.findByUrlMappingId(mapping.getId());
        if (variants.isEmpty()) {
            return null;
        }

        List<UrlVariant> activeVariants = variants.stream().filter(UrlVariant::isActive).toList();
        if (activeVariants.isEmpty()) {
            return variants.get(0);
        }

        String cookieName = COOKIE_PREFIX + mapping.getShortCode();
        String stickyVariantId = null;

        if (request != null && request.getCookies() != null) {
            for (Cookie c : request.getCookies()) {
                if (cookieName.equals(c.getName())) {
                    stickyVariantId = c.getValue();
                    break;
                }
            }
        }

        // 1. Sticky Session Kontrolü: Kullanıcı daha önce bu linke tıkladıysa aynı varyantı ver
        if (stickyVariantId != null) {
            for (UrlVariant v : activeVariants) {
                if (v.getId().toString().equals(stickyVariantId)) {
                    v.setClickCount(v.getClickCount() + 1);
                    urlVariantRepository.save(v);
                    return v;
                }
            }
        }

        // 2. Ağırlıklı Rastgele Seçim (Weighted Random Selection)
        int randomVal = ThreadLocalRandom.current().nextInt(1, 101); // 1 - 100 arası
        int cumulativeWeight = 0;
        UrlVariant selected = activeVariants.get(0);

        for (UrlVariant v : activeVariants) {
            cumulativeWeight += v.getWeightPercent();
            if (randomVal <= cumulativeWeight) {
                selected = v;
                break;
            }
        }

        // 3. Kullanıcıya Sticky Session Çerezi Bırak (30 Günlük)
        if (response != null) {
            Cookie abCookie = new Cookie(cookieName, selected.getId().toString());
            abCookie.setMaxAge(30 * 24 * 60 * 60); // 30 gün
            abCookie.setPath("/");
            abCookie.setHttpOnly(true);
            response.addCookie(abCookie);
        }

        // 4. Varyant Sayacını Artır
        selected.setClickCount(selected.getClickCount() + 1);
        urlVariantRepository.save(selected);

        return selected;
    }

    private void checkOwnershipOrAdmin(UrlMapping mapping) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            throw new SecurityException("Bu işlem için oturum açmanız gerekmektedir.");
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

        throw new SecurityException("Bu linkin A/B test ayarlarını değiştirme yetkiniz bulunmamaktadır.");
    }
}
