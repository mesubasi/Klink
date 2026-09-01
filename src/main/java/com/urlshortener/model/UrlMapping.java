package com.urlshortener.model;

import jakarta.persistence.*;

import java.io.Serializable;
import java.util.UUID;

@Entity
@Table(name = "url_mappings", indexes = {
    @Index(name = "idx_short_code", columnList = "shortCode")
})
public class UrlMapping implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, length = 2048)
    private String originalUrl;

    @Column(nullable = false, unique = true, length = 20)
    private String shortCode;

    @Column(nullable = false)
    private Long createdAt;

    private Long expiresAt;

    @Column(length = 2048)
    private String fallbackUrl;

    @Column(nullable = false)
    private Long clickCount = 0L;

    @Column(nullable = false)
    private boolean active = true;

    @Column(length = 255)
    private String passwordHash;

    @Column(length = 1000)
    private String blockedCountries;

    @Column(length = 2000)
    private String blockedIps;

    @Column(nullable = false)
    private boolean previewEnabled = false;

    @Column(length = 2048)
    private String iosUrl;

    @Column(length = 2048)
    private String androidUrl;

    @Column(length = 2048)
    private String desktopUrl;

    @Column(length = 2048)
    private String webhookUrl;

    @Column(length = 255)
    private String webhookSecret;

    @Column(length = 30)
    private String healthStatus = "UNKNOWN";

    private Long lastHealthCheck;

    private Integer healthStatusCode;

    @Column(length = 500)
    private String healthErrorMessage;

    private Long healthResponseTimeMs;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private UserAccount user;

    public UrlMapping() {}

    public UrlMapping(UUID id, String originalUrl, String shortCode, Long createdAt, Long expiresAt, String fallbackUrl, Long clickCount, boolean active, String passwordHash, String blockedCountries, String blockedIps, boolean previewEnabled, String iosUrl, String androidUrl, String desktopUrl, String webhookUrl, String webhookSecret, String healthStatus, Long lastHealthCheck, Integer healthStatusCode, String healthErrorMessage, Long healthResponseTimeMs, UserAccount user) {
        this.id = id;
        this.originalUrl = originalUrl;
        this.shortCode = shortCode;
        this.createdAt = createdAt != null ? createdAt : System.currentTimeMillis();
        this.expiresAt = expiresAt;
        this.fallbackUrl = fallbackUrl;
        this.clickCount = clickCount != null ? clickCount : 0L;
        this.active = active;
        this.passwordHash = passwordHash;
        this.blockedCountries = blockedCountries;
        this.blockedIps = blockedIps;
        this.previewEnabled = previewEnabled;
        this.iosUrl = iosUrl;
        this.androidUrl = androidUrl;
        this.desktopUrl = desktopUrl;
        this.webhookUrl = webhookUrl;
        this.webhookSecret = webhookSecret;
        this.healthStatus = healthStatus != null ? healthStatus : "UNKNOWN";
        this.lastHealthCheck = lastHealthCheck;
        this.healthStatusCode = healthStatusCode;
        this.healthErrorMessage = healthErrorMessage;
        this.healthResponseTimeMs = healthResponseTimeMs;
        this.user = user;
    }

    public UrlMapping(UUID id, String originalUrl, String shortCode, Long createdAt, Long expiresAt, String fallbackUrl, Long clickCount, boolean active, String passwordHash, String blockedCountries, String blockedIps, boolean previewEnabled, String iosUrl, String androidUrl, String desktopUrl, String webhookUrl, String webhookSecret, UserAccount user) {
        this(id, originalUrl, shortCode, createdAt, expiresAt, fallbackUrl, clickCount, active, passwordHash, blockedCountries, blockedIps, previewEnabled, iosUrl, androidUrl, desktopUrl, webhookUrl, webhookSecret, "UNKNOWN", null, null, null, null, user);
    }

    public UrlMapping(UUID id, String originalUrl, String shortCode, Long createdAt, Long expiresAt, String fallbackUrl, Long clickCount, boolean active, String passwordHash, String blockedCountries, String blockedIps, boolean previewEnabled, String iosUrl, String androidUrl, String desktopUrl, UserAccount user) {
        this(id, originalUrl, shortCode, createdAt, expiresAt, fallbackUrl, clickCount, active, passwordHash, blockedCountries, blockedIps, previewEnabled, iosUrl, androidUrl, desktopUrl, null, null, "UNKNOWN", null, null, null, null, user);
    }

    public UrlMapping(UUID id, String originalUrl, String shortCode, Long createdAt, Long expiresAt, String fallbackUrl, Long clickCount, boolean active, String passwordHash, String blockedCountries, String blockedIps, boolean previewEnabled, UserAccount user) {
        this(id, originalUrl, shortCode, createdAt, expiresAt, fallbackUrl, clickCount, active, passwordHash, blockedCountries, blockedIps, previewEnabled, null, null, null, null, null, "UNKNOWN", null, null, null, null, user);
    }

    public UrlMapping(UUID id, String originalUrl, String shortCode, Long createdAt, Long expiresAt, String fallbackUrl, Long clickCount, boolean active, String passwordHash, String blockedCountries, String blockedIps, UserAccount user) {
        this(id, originalUrl, shortCode, createdAt, expiresAt, fallbackUrl, clickCount, active, passwordHash, blockedCountries, blockedIps, false, null, null, null, null, null, "UNKNOWN", null, null, null, null, user);
    }

    public UrlMapping(UUID id, String originalUrl, String shortCode, Long createdAt, Long expiresAt, String fallbackUrl, Long clickCount, boolean active, String passwordHash, UserAccount user) {
        this(id, originalUrl, shortCode, createdAt, expiresAt, fallbackUrl, clickCount, active, passwordHash, null, null, false, null, null, null, null, null, "UNKNOWN", null, null, null, null, user);
    }

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = System.currentTimeMillis();
        }
        if (clickCount == null) {
            clickCount = 0L;
        }
        if (healthStatus == null) {
            healthStatus = "UNKNOWN";
        }
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private UUID id;
        private String originalUrl;
        private String shortCode;
        private Long createdAt;
        private Long expiresAt;
        private String fallbackUrl;
        private Long clickCount = 0L;
        private boolean active = true;
        private String passwordHash;
        private String blockedCountries;
        private String blockedIps;
        private boolean previewEnabled = false;
        private String iosUrl;
        private String androidUrl;
        private String desktopUrl;
        private String webhookUrl;
        private String webhookSecret;
        private String healthStatus = "UNKNOWN";
        private Long lastHealthCheck;
        private Integer healthStatusCode;
        private String healthErrorMessage;
        private Long healthResponseTimeMs;
        private UserAccount user;

        public Builder id(UUID id) { this.id = id; return this; }
        public Builder originalUrl(String originalUrl) { this.originalUrl = originalUrl; return this; }
        public Builder shortCode(String shortCode) { this.shortCode = shortCode; return this; }
        public Builder createdAt(Long createdAt) { this.createdAt = createdAt; return this; }
        public Builder expiresAt(Long expiresAt) { this.expiresAt = expiresAt; return this; }
        public Builder fallbackUrl(String fallbackUrl) { this.fallbackUrl = fallbackUrl; return this; }
        public Builder clickCount(Long clickCount) { this.clickCount = clickCount; return this; }
        public Builder active(boolean active) { this.active = active; return this; }
        public Builder passwordHash(String passwordHash) { this.passwordHash = passwordHash; return this; }
        public Builder blockedCountries(String blockedCountries) { this.blockedCountries = blockedCountries; return this; }
        public Builder blockedIps(String blockedIps) { this.blockedIps = blockedIps; return this; }
        public Builder previewEnabled(boolean previewEnabled) { this.previewEnabled = previewEnabled; return this; }
        public Builder iosUrl(String iosUrl) { this.iosUrl = iosUrl; return this; }
        public Builder androidUrl(String androidUrl) { this.androidUrl = androidUrl; return this; }
        public Builder desktopUrl(String desktopUrl) { this.desktopUrl = desktopUrl; return this; }
        public Builder webhookUrl(String webhookUrl) { this.webhookUrl = webhookUrl; return this; }
        public Builder webhookSecret(String webhookSecret) { this.webhookSecret = webhookSecret; return this; }
        public Builder healthStatus(String healthStatus) { this.healthStatus = healthStatus; return this; }
        public Builder lastHealthCheck(Long lastHealthCheck) { this.lastHealthCheck = lastHealthCheck; return this; }
        public Builder healthStatusCode(Integer healthStatusCode) { this.healthStatusCode = healthStatusCode; return this; }
        public Builder healthErrorMessage(String healthErrorMessage) { this.healthErrorMessage = healthErrorMessage; return this; }
        public Builder healthResponseTimeMs(Long healthResponseTimeMs) { this.healthResponseTimeMs = healthResponseTimeMs; return this; }
        public Builder user(UserAccount user) { this.user = user; return this; }

        public UrlMapping build() {
            return new UrlMapping(id, originalUrl, shortCode, createdAt, expiresAt, fallbackUrl, clickCount, active, passwordHash, blockedCountries, blockedIps, previewEnabled, iosUrl, androidUrl, desktopUrl, webhookUrl, webhookSecret, healthStatus, lastHealthCheck, healthStatusCode, healthErrorMessage, healthResponseTimeMs, user);
        }
    }

    public boolean isPasswordProtected() {
        return passwordHash != null && !passwordHash.trim().isEmpty();
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public String getOriginalUrl() { return originalUrl; }
    public void setOriginalUrl(String originalUrl) { this.originalUrl = originalUrl; }
    public String getShortCode() { return shortCode; }
    public void setShortCode(String shortCode) { this.shortCode = shortCode; }
    public Long getCreatedAt() { return createdAt; }
    public void setCreatedAt(Long createdAt) { this.createdAt = createdAt; }
    public Long getExpiresAt() { return expiresAt; }
    public void setExpiresAt(Long expiresAt) { this.expiresAt = expiresAt; }
    public String getFallbackUrl() { return fallbackUrl; }
    public void setFallbackUrl(String fallbackUrl) { this.fallbackUrl = fallbackUrl; }
    public Long getClickCount() { return clickCount; }
    public void setClickCount(Long clickCount) { this.clickCount = clickCount; }
    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }
    public String getBlockedCountries() { return blockedCountries; }
    public void setBlockedCountries(String blockedCountries) { this.blockedCountries = blockedCountries; }
    public String getBlockedIps() { return blockedIps; }
    public void setBlockedIps(String blockedIps) { this.blockedIps = blockedIps; }
    public boolean isPreviewEnabled() { return previewEnabled; }
    public void setPreviewEnabled(boolean previewEnabled) { this.previewEnabled = previewEnabled; }
    public String getIosUrl() { return iosUrl; }
    public void setIosUrl(String iosUrl) { this.iosUrl = iosUrl; }
    public String getAndroidUrl() { return androidUrl; }
    public void setAndroidUrl(String androidUrl) { this.androidUrl = androidUrl; }
    public String getDesktopUrl() { return desktopUrl; }
    public void setDesktopUrl(String desktopUrl) { this.desktopUrl = desktopUrl; }
    public String getWebhookUrl() { return webhookUrl; }
    public void setWebhookUrl(String webhookUrl) { this.webhookUrl = webhookUrl; }
    public String getWebhookSecret() { return webhookSecret; }
    public void setWebhookSecret(String webhookSecret) { this.webhookSecret = webhookSecret; }
    public String getHealthStatus() { return healthStatus; }
    public void setHealthStatus(String healthStatus) { this.healthStatus = healthStatus; }
    public Long getLastHealthCheck() { return lastHealthCheck; }
    public void setLastHealthCheck(Long lastHealthCheck) { this.lastHealthCheck = lastHealthCheck; }
    public Integer getHealthStatusCode() { return healthStatusCode; }
    public void setHealthStatusCode(Integer healthStatusCode) { this.healthStatusCode = healthStatusCode; }
    public String getHealthErrorMessage() { return healthErrorMessage; }
    public void setHealthErrorMessage(String healthErrorMessage) { this.healthErrorMessage = healthErrorMessage; }
    public Long getHealthResponseTimeMs() { return healthResponseTimeMs; }
    public void setHealthResponseTimeMs(Long healthResponseTimeMs) { this.healthResponseTimeMs = healthResponseTimeMs; }
    public UserAccount getUser() { return user; }
    public void setUser(UserAccount user) { this.user = user; }
}
