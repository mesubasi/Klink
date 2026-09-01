package com.urlshortener.service;

import com.urlshortener.dto.ApiKeyActionRequest;
import com.urlshortener.dto.ApiKeyApplyRequest;
import com.urlshortener.dto.ApiKeyResponse;
import com.urlshortener.model.ApiKey;
import com.urlshortener.model.ApiKeyStatus;
import com.urlshortener.model.UserAccount;
import com.urlshortener.repository.ApiKeyRepository;
import com.urlshortener.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.HexFormat;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class ApiKeyService {

    private static final Logger log = LoggerFactory.getLogger(ApiKeyService.class);
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    private final ApiKeyRepository apiKeyRepository;
    private final UserRepository userRepository;

    public ApiKeyService(ApiKeyRepository apiKeyRepository, UserRepository userRepository) {
        this.apiKeyRepository = apiKeyRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public ApiKeyResponse applyForApiKey(ApiKeyApplyRequest req) {
        UserAccount user = getCurrentUser();
        if (user == null) {
            throw new IllegalStateException("API anahtarı başvurusu için giriş yapmalısınız.");
        }

        if (req.getAppName() == null || req.getAppName().trim().isEmpty()) {
            throw new IllegalArgumentException("Uygulama / Proje adı boş bırakılamaz.");
        }
        if (req.getPurpose() == null || req.getPurpose().trim().isEmpty()) {
            throw new IllegalArgumentException("Kullanım amacı açıklaması boş bırakılamaz.");
        }

        ApiKey apiKey = ApiKey.builder()
                .user(user)
                .appName(req.getAppName().trim())
                .purpose(req.getPurpose().trim())
                .websiteUrl(req.getWebsiteUrl() != null ? req.getWebsiteUrl().trim() : null)
                .expectedMonthlyClicks(req.getExpectedMonthlyClicks() != null ? req.getExpectedMonthlyClicks().trim() : "1.000 - 10.000")
                .ipWhitelist(req.getIpWhitelist() != null ? req.getIpWhitelist().trim() : null)
                .status(ApiKeyStatus.PENDING)
                .rateLimitPerMinute(60)
                .totalCalls(0L)
                .createdAt(System.currentTimeMillis())
                .build();

        ApiKey saved = apiKeyRepository.save(apiKey);
        log.info("📝 Yeni API Key başvurusu alındı: User: {}, App: {}", user.getUsername(), saved.getAppName());
        return toResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<ApiKeyResponse> getMyApiKeys() {
        UserAccount user = getCurrentUser();
        if (user == null) {
            throw new IllegalStateException("Giriş yapmalısınız.");
        }
        return apiKeyRepository.findByUserIdOrderByCreatedAtDesc(user.getId())
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ApiKeyResponse> getAllApplications(ApiKeyStatus status) {
        List<ApiKey> list = (status != null) 
                ? apiKeyRepository.findByStatusOrderByCreatedAtDesc(status) 
                : apiKeyRepository.findAllByOrderByCreatedAtDesc();
        return list.stream().map(this::toResponse).collect(Collectors.toList());
    }

    @Transactional
    public ApiKeyResponse approveApplication(UUID keyId, ApiKeyActionRequest actionRequest) {
        ApiKey apiKey = apiKeyRepository.findById(keyId)
                .orElseThrow(() -> new IllegalArgumentException("API başvurusu bulunamadı: " + keyId));

        String rawKey = generateSecureApiKey();
        String keyHash = hashKey(rawKey);
        String prefix = rawKey.substring(0, 15) + "...";

        int rateLimit = (actionRequest != null && actionRequest.getRateLimitPerMinute() != null && actionRequest.getRateLimitPerMinute() > 0)
                ? actionRequest.getRateLimitPerMinute()
                : 120;

        apiKey.setRawKey(rawKey);
        apiKey.setKeyHash(keyHash);
        apiKey.setKeyPrefix(prefix);
        apiKey.setStatus(ApiKeyStatus.APPROVED);
        apiKey.setRateLimitPerMinute(rateLimit);
        apiKey.setApprovedAt(System.currentTimeMillis());
        apiKey.setRejectionReason(null);

        ApiKey updated = apiKeyRepository.save(apiKey);
        log.info("✅ API Key başvurusu onaylandı: ID: {}, App: {}, User: {}", updated.getId(), updated.getAppName(), updated.getUser().getUsername());
        return toResponse(updated);
    }

    @Transactional
    public ApiKeyResponse rejectApplication(UUID keyId, ApiKeyActionRequest actionRequest) {
        ApiKey apiKey = apiKeyRepository.findById(keyId)
                .orElseThrow(() -> new IllegalArgumentException("API başvurusu bulunamadı: " + keyId));

        String reason = (actionRequest != null && actionRequest.getRejectionReason() != null && !actionRequest.getRejectionReason().trim().isEmpty())
                ? actionRequest.getRejectionReason().trim()
                : "Başvuru kriterleri karşılanamadı.";

        apiKey.setStatus(ApiKeyStatus.REJECTED);
        apiKey.setRejectionReason(reason);
        apiKey.setRawKey(null);
        apiKey.setKeyHash(null);

        ApiKey updated = apiKeyRepository.save(apiKey);
        log.info("❌ API Key başvurusu reddedildi: ID: {}, Sebep: {}", updated.getId(), reason);
        return toResponse(updated);
    }

    @Transactional
    public ApiKeyResponse revokeApiKey(UUID keyId) {
        ApiKey apiKey = apiKeyRepository.findById(keyId)
                .orElseThrow(() -> new IllegalArgumentException("API anahtarı bulunamadı: " + keyId));

        apiKey.setStatus(ApiKeyStatus.REVOKED);
        ApiKey updated = apiKeyRepository.save(apiKey);
        log.info("🚫 API Key iptal edildi (REVOKED): ID: {}", updated.getId());
        return toResponse(updated);
    }

    @Transactional
    public ApiKeyResponse regenerateApiKey(UUID keyId) {
        UserAccount user = getCurrentUser();
        ApiKey apiKey = apiKeyRepository.findById(keyId)
                .orElseThrow(() -> new IllegalArgumentException("API anahtarı bulunamadı: " + keyId));

        if (user != null && !user.getId().equals(apiKey.getUser().getId()) && !"ROLE_ADMIN".equals(user.getRole())) {
            throw new SecurityException("Bu işlem için yetkiniz yok.");
        }

        if (apiKey.getStatus() != ApiKeyStatus.APPROVED) {
            throw new IllegalStateException("Sadece onaylanmış API anahtarları yeniden üretilebilir.");
        }

        String newRawKey = generateSecureApiKey();
        String newKeyHash = hashKey(newRawKey);
        String prefix = newRawKey.substring(0, 15) + "...";

        apiKey.setRawKey(newRawKey);
        apiKey.setKeyHash(newKeyHash);
        apiKey.setKeyPrefix(prefix);

        ApiKey updated = apiKeyRepository.save(apiKey);
        log.info("🔄 API Key yenilendi: ID: {}", updated.getId());
        return toResponse(updated);
    }

    @Transactional
    public ApiKey authenticateApiKey(String rawKey) {
        if (rawKey == null || rawKey.trim().isEmpty()) {
            return null;
        }

        String hash = hashKey(rawKey.trim());
        ApiKey apiKey = apiKeyRepository.findByKeyHash(hash).orElse(null);
        if (apiKey == null || apiKey.getStatus() != ApiKeyStatus.APPROVED) {
            return null;
        }

        try {
            apiKeyRepository.recordUsage(apiKey.getId(), System.currentTimeMillis());
        } catch (Exception e) {
            log.warn("API Key kullanım kaydı güncellenemedi: {}", e.getMessage());
        }

        return apiKey;
    }

    private String generateSecureApiKey() {
        byte[] randomBytes = new byte[24];
        SECURE_RANDOM.nextBytes(randomBytes);
        return "kl_live_" + HexFormat.of().formatHex(randomBytes);
    }

    public static String hashKey(String rawKey) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] encodedhash = digest.digest(rawKey.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(encodedhash);
        } catch (Exception e) {
            throw new RuntimeException("SHA-256 algoritması bulunamadı", e);
        }
    }

    private ApiKeyResponse toResponse(ApiKey entity) {
        return ApiKeyResponse.builder()
                .id(entity.getId())
                .keyPrefix(entity.getKeyPrefix())
                .rawKey(entity.getRawKey())
                .appName(entity.getAppName())
                .purpose(entity.getPurpose())
                .websiteUrl(entity.getWebsiteUrl())
                .expectedMonthlyClicks(entity.getExpectedMonthlyClicks())
                .ipWhitelist(entity.getIpWhitelist())
                .status(entity.getStatus())
                .rejectionReason(entity.getRejectionReason())
                .rateLimitPerMinute(entity.getRateLimitPerMinute())
                .totalCalls(entity.getTotalCalls())
                .lastUsedAt(entity.getLastUsedAt())
                .createdAt(entity.getCreatedAt())
                .approvedAt(entity.getApprovedAt())
                .username(entity.getUser() != null ? entity.getUser().getUsername() : null)
                .userEmail(entity.getUser() != null ? entity.getUser().getEmail() : null)
                .build();
    }

    private UserAccount getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getPrincipal())) {
            return null;
        }
        return userRepository.findByUsername(auth.getName()).orElse(null);
    }
}
