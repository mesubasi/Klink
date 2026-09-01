package com.urlshortener.model;

import jakarta.persistence.*;

import java.io.Serializable;
import java.util.UUID;

@Entity
@Table(name = "api_keys", indexes = {
    @Index(name = "idx_apikey_hash", columnList = "key_hash"),
    @Index(name = "idx_apikey_user", columnList = "user_id"),
    @Index(name = "idx_apikey_status", columnList = "status")
})
public class ApiKey implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "key_prefix", length = 20)
    private String keyPrefix;

    @Column(name = "key_hash", length = 64)
    private String keyHash;

    @Column(name = "raw_key", length = 100)
    private String rawKey;

    @Column(name = "app_name", nullable = false, length = 100)
    private String appName;

    @Column(nullable = false, length = 1000)
    private String purpose;

    @Column(name = "website_url", length = 2048)
    private String websiteUrl;

    @Column(name = "expected_monthly_clicks", length = 50)
    private String expectedMonthlyClicks;

    @Column(name = "ip_whitelist", length = 1000)
    private String ipWhitelist;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ApiKeyStatus status = ApiKeyStatus.PENDING;

    @Column(name = "rejection_reason", length = 1000)
    private String rejectionReason;

    @Column(name = "rate_limit_per_minute", nullable = false)
    private int rateLimitPerMinute = 60;

    @Column(name = "total_calls", nullable = false)
    private Long totalCalls = 0L;

    @Column(name = "last_used_at")
    private Long lastUsedAt;

    @Column(name = "created_at", nullable = false)
    private Long createdAt;

    @Column(name = "approved_at")
    private Long approvedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private UserAccount user;

    public ApiKey() {}

    public ApiKey(UUID id, String keyPrefix, String keyHash, String rawKey, String appName, String purpose, String websiteUrl, String expectedMonthlyClicks, String ipWhitelist, ApiKeyStatus status, String rejectionReason, int rateLimitPerMinute, Long totalCalls, Long lastUsedAt, Long createdAt, Long approvedAt, UserAccount user) {
        this.id = id;
        this.keyPrefix = keyPrefix;
        this.keyHash = keyHash;
        this.rawKey = rawKey;
        this.appName = appName;
        this.purpose = purpose;
        this.websiteUrl = websiteUrl;
        this.expectedMonthlyClicks = expectedMonthlyClicks;
        this.ipWhitelist = ipWhitelist;
        this.status = status != null ? status : ApiKeyStatus.PENDING;
        this.rejectionReason = rejectionReason;
        this.rateLimitPerMinute = rateLimitPerMinute > 0 ? rateLimitPerMinute : 60;
        this.totalCalls = totalCalls != null ? totalCalls : 0L;
        this.lastUsedAt = lastUsedAt;
        this.createdAt = createdAt != null ? createdAt : System.currentTimeMillis();
        this.approvedAt = approvedAt;
        this.user = user;
    }

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = System.currentTimeMillis();
        }
        if (totalCalls == null) {
            totalCalls = 0L;
        }
        if (status == null) {
            status = ApiKeyStatus.PENDING;
        }
        if (rateLimitPerMinute <= 0) {
            rateLimitPerMinute = 60;
        }
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private UUID id;
        private String keyPrefix;
        private String keyHash;
        private String rawKey;
        private String appName;
        private String purpose;
        private String websiteUrl;
        private String expectedMonthlyClicks;
        private String ipWhitelist;
        private ApiKeyStatus status = ApiKeyStatus.PENDING;
        private String rejectionReason;
        private int rateLimitPerMinute = 60;
        private Long totalCalls = 0L;
        private Long lastUsedAt;
        private Long createdAt;
        private Long approvedAt;
        private UserAccount user;

        public Builder id(UUID id) { this.id = id; return this; }
        public Builder keyPrefix(String keyPrefix) { this.keyPrefix = keyPrefix; return this; }
        public Builder keyHash(String keyHash) { this.keyHash = keyHash; return this; }
        public Builder rawKey(String rawKey) { this.rawKey = rawKey; return this; }
        public Builder appName(String appName) { this.appName = appName; return this; }
        public Builder purpose(String purpose) { this.purpose = purpose; return this; }
        public Builder websiteUrl(String websiteUrl) { this.websiteUrl = websiteUrl; return this; }
        public Builder expectedMonthlyClicks(String expectedMonthlyClicks) { this.expectedMonthlyClicks = expectedMonthlyClicks; return this; }
        public Builder ipWhitelist(String ipWhitelist) { this.ipWhitelist = ipWhitelist; return this; }
        public Builder status(ApiKeyStatus status) { this.status = status; return this; }
        public Builder rejectionReason(String rejectionReason) { this.rejectionReason = rejectionReason; return this; }
        public Builder rateLimitPerMinute(int rateLimitPerMinute) { this.rateLimitPerMinute = rateLimitPerMinute; return this; }
        public Builder totalCalls(Long totalCalls) { this.totalCalls = totalCalls; return this; }
        public Builder lastUsedAt(Long lastUsedAt) { this.lastUsedAt = lastUsedAt; return this; }
        public Builder createdAt(Long createdAt) { this.createdAt = createdAt; return this; }
        public Builder approvedAt(Long approvedAt) { this.approvedAt = approvedAt; return this; }
        public Builder user(UserAccount user) { this.user = user; return this; }

        public ApiKey build() {
            return new ApiKey(id, keyPrefix, keyHash, rawKey, appName, purpose, websiteUrl, expectedMonthlyClicks, ipWhitelist, status, rejectionReason, rateLimitPerMinute, totalCalls, lastUsedAt, createdAt, approvedAt, user);
        }
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public String getKeyPrefix() { return keyPrefix; }
    public void setKeyPrefix(String keyPrefix) { this.keyPrefix = keyPrefix; }
    public String getKeyHash() { return keyHash; }
    public void setKeyHash(String keyHash) { this.keyHash = keyHash; }
    public String getRawKey() { return rawKey; }
    public void setRawKey(String rawKey) { this.rawKey = rawKey; }
    public String getAppName() { return appName; }
    public void setAppName(String appName) { this.appName = appName; }
    public String getPurpose() { return purpose; }
    public void setPurpose(String purpose) { this.purpose = purpose; }
    public String getWebsiteUrl() { return websiteUrl; }
    public void setWebsiteUrl(String websiteUrl) { this.websiteUrl = websiteUrl; }
    public String getExpectedMonthlyClicks() { return expectedMonthlyClicks; }
    public void setExpectedMonthlyClicks(String expectedMonthlyClicks) { this.expectedMonthlyClicks = expectedMonthlyClicks; }
    public String getIpWhitelist() { return ipWhitelist; }
    public void setIpWhitelist(String ipWhitelist) { this.ipWhitelist = ipWhitelist; }
    public ApiKeyStatus getStatus() { return status; }
    public void setStatus(ApiKeyStatus status) { this.status = status; }
    public String getRejectionReason() { return rejectionReason; }
    public void setRejectionReason(String rejectionReason) { this.rejectionReason = rejectionReason; }
    public int getRateLimitPerMinute() { return rateLimitPerMinute; }
    public void setRateLimitPerMinute(int rateLimitPerMinute) { this.rateLimitPerMinute = rateLimitPerMinute; }
    public Long getTotalCalls() { return totalCalls; }
    public void setTotalCalls(Long totalCalls) { this.totalCalls = totalCalls; }
    public Long getLastUsedAt() { return lastUsedAt; }
    public void setLastUsedAt(Long lastUsedAt) { this.lastUsedAt = lastUsedAt; }
    public Long getCreatedAt() { return createdAt; }
    public void setCreatedAt(Long createdAt) { this.createdAt = createdAt; }
    public Long getApprovedAt() { return approvedAt; }
    public void setApprovedAt(Long approvedAt) { this.approvedAt = approvedAt; }
    public UserAccount getUser() { return user; }
    public void setUser(UserAccount user) { this.user = user; }
}
