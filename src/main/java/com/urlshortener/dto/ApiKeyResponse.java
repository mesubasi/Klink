package com.urlshortener.dto;

import com.urlshortener.model.ApiKeyStatus;

import java.io.Serializable;
import java.util.UUID;

public class ApiKeyResponse implements Serializable {

    private static final long serialVersionUID = 1L;

    private UUID id;
    private String keyPrefix;
    private String rawKey;
    private String appName;
    private String purpose;
    private String websiteUrl;
    private String expectedMonthlyClicks;
    private String ipWhitelist;
    private ApiKeyStatus status;
    private String rejectionReason;
    private int rateLimitPerMinute;
    private Long totalCalls;
    private Long lastUsedAt;
    private Long createdAt;
    private Long approvedAt;
    private String username;
    private String userEmail;

    public ApiKeyResponse() {}

    public ApiKeyResponse(UUID id, String keyPrefix, String rawKey, String appName, String purpose, String websiteUrl, String expectedMonthlyClicks, String ipWhitelist, ApiKeyStatus status, String rejectionReason, int rateLimitPerMinute, Long totalCalls, Long lastUsedAt, Long createdAt, Long approvedAt, String username, String userEmail) {
        this.id = id;
        this.keyPrefix = keyPrefix;
        this.rawKey = rawKey;
        this.appName = appName;
        this.purpose = purpose;
        this.websiteUrl = websiteUrl;
        this.expectedMonthlyClicks = expectedMonthlyClicks;
        this.ipWhitelist = ipWhitelist;
        this.status = status;
        this.rejectionReason = rejectionReason;
        this.rateLimitPerMinute = rateLimitPerMinute;
        this.totalCalls = totalCalls;
        this.lastUsedAt = lastUsedAt;
        this.createdAt = createdAt;
        this.approvedAt = approvedAt;
        this.username = username;
        this.userEmail = userEmail;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private UUID id;
        private String keyPrefix;
        private String rawKey;
        private String appName;
        private String purpose;
        private String websiteUrl;
        private String expectedMonthlyClicks;
        private String ipWhitelist;
        private ApiKeyStatus status;
        private String rejectionReason;
        private int rateLimitPerMinute = 60;
        private Long totalCalls = 0L;
        private Long lastUsedAt;
        private Long createdAt;
        private Long approvedAt;
        private String username;
        private String userEmail;

        public Builder id(UUID id) { this.id = id; return this; }
        public Builder keyPrefix(String keyPrefix) { this.keyPrefix = keyPrefix; return this; }
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
        public Builder username(String username) { this.username = username; return this; }
        public Builder userEmail(String userEmail) { this.userEmail = userEmail; return this; }

        public ApiKeyResponse build() {
            return new ApiKeyResponse(id, keyPrefix, rawKey, appName, purpose, websiteUrl, expectedMonthlyClicks, ipWhitelist, status, rejectionReason, rateLimitPerMinute, totalCalls, lastUsedAt, createdAt, approvedAt, username, userEmail);
        }
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public String getKeyPrefix() { return keyPrefix; }
    public void setKeyPrefix(String keyPrefix) { this.keyPrefix = keyPrefix; }
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
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getUserEmail() { return userEmail; }
    public void setUserEmail(String userEmail) { this.userEmail = userEmail; }
}
