package com.urlshortener.dto;

public class UrlPreviewResponse {
    private String shortCode;
    private String shortUrl;
    private String originalUrl;
    private String domain;
    private String protocol;
    private boolean secure;
    private String safetyStatus;
    private int safetyScore;
    private String googleSafeBrowsingStatus;
    private String virusTotalStatus;
    private boolean passwordProtected;
    private boolean previewEnabled;
    private Long createdAt;
    private Long expiresAt;
    private Long clickCount;
    private boolean active;
    private String iosUrl;
    private String androidUrl;
    private String desktopUrl;
    private String webhookUrl;
    private String healthStatus;
    private Long lastHealthCheck;
    private Integer healthStatusCode;
    private String healthErrorMessage;
    private Long healthResponseTimeMs;

    public UrlPreviewResponse() {}

    public UrlPreviewResponse(String shortCode, String shortUrl, String originalUrl, String domain, String protocol,
                              boolean secure, String safetyStatus, int safetyScore, String googleSafeBrowsingStatus,
                              String virusTotalStatus, boolean passwordProtected, boolean previewEnabled,
                              Long createdAt, Long expiresAt, Long clickCount, boolean active,
                              String iosUrl, String androidUrl, String desktopUrl) {
        this(shortCode, shortUrl, originalUrl, domain, protocol, secure, safetyStatus, safetyScore, googleSafeBrowsingStatus, virusTotalStatus, passwordProtected, previewEnabled, createdAt, expiresAt, clickCount, active, iosUrl, androidUrl, desktopUrl, null, "UNKNOWN", null, null, null, null);
    }

    public UrlPreviewResponse(String shortCode, String shortUrl, String originalUrl, String domain, String protocol,
                              boolean secure, String safetyStatus, int safetyScore, String googleSafeBrowsingStatus,
                              String virusTotalStatus, boolean passwordProtected, boolean previewEnabled,
                              Long createdAt, Long expiresAt, Long clickCount, boolean active,
                              String iosUrl, String androidUrl, String desktopUrl, String webhookUrl) {
        this(shortCode, shortUrl, originalUrl, domain, protocol, secure, safetyStatus, safetyScore, googleSafeBrowsingStatus, virusTotalStatus, passwordProtected, previewEnabled, createdAt, expiresAt, clickCount, active, iosUrl, androidUrl, desktopUrl, webhookUrl, "UNKNOWN", null, null, null, null);
    }

    public UrlPreviewResponse(String shortCode, String shortUrl, String originalUrl, String domain, String protocol,
                              boolean secure, String safetyStatus, int safetyScore, String googleSafeBrowsingStatus,
                              String virusTotalStatus, boolean passwordProtected, boolean previewEnabled,
                              Long createdAt, Long expiresAt, Long clickCount, boolean active,
                              String iosUrl, String androidUrl, String desktopUrl, String webhookUrl,
                              String healthStatus, Long lastHealthCheck, Integer healthStatusCode, String healthErrorMessage, Long healthResponseTimeMs) {
        this.shortCode = shortCode;
        this.shortUrl = shortUrl;
        this.originalUrl = originalUrl;
        this.domain = domain;
        this.protocol = protocol;
        this.secure = secure;
        this.safetyStatus = safetyStatus;
        this.safetyScore = safetyScore;
        this.googleSafeBrowsingStatus = googleSafeBrowsingStatus;
        this.virusTotalStatus = virusTotalStatus;
        this.passwordProtected = passwordProtected;
        this.previewEnabled = previewEnabled;
        this.createdAt = createdAt;
        this.expiresAt = expiresAt;
        this.clickCount = clickCount;
        this.active = active;
        this.iosUrl = iosUrl;
        this.androidUrl = androidUrl;
        this.desktopUrl = desktopUrl;
        this.webhookUrl = webhookUrl;
        this.healthStatus = healthStatus;
        this.lastHealthCheck = lastHealthCheck;
        this.healthStatusCode = healthStatusCode;
        this.healthErrorMessage = healthErrorMessage;
        this.healthResponseTimeMs = healthResponseTimeMs;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String shortCode;
        private String shortUrl;
        private String originalUrl;
        private String domain;
        private String protocol;
        private boolean secure;
        private String safetyStatus;
        private int safetyScore;
        private String googleSafeBrowsingStatus;
        private String virusTotalStatus;
        private boolean passwordProtected;
        private boolean previewEnabled;
        private Long createdAt;
        private Long expiresAt;
        private Long clickCount;
        private boolean active;
        private String iosUrl;
        private String androidUrl;
        private String desktopUrl;
        private String webhookUrl;
        private String healthStatus = "UNKNOWN";
        private Long lastHealthCheck;
        private Integer healthStatusCode;
        private String healthErrorMessage;
        private Long healthResponseTimeMs;

        public Builder shortCode(String shortCode) { this.shortCode = shortCode; return this; }
        public Builder shortUrl(String shortUrl) { this.shortUrl = shortUrl; return this; }
        public Builder originalUrl(String originalUrl) { this.originalUrl = originalUrl; return this; }
        public Builder domain(String domain) { this.domain = domain; return this; }
        public Builder protocol(String protocol) { this.protocol = protocol; return this; }
        public Builder secure(boolean secure) { this.secure = secure; return this; }
        public Builder safetyStatus(String safetyStatus) { this.safetyStatus = safetyStatus; return this; }
        public Builder safetyScore(int safetyScore) { this.safetyScore = safetyScore; return this; }
        public Builder googleSafeBrowsingStatus(String status) { this.googleSafeBrowsingStatus = status; return this; }
        public Builder virusTotalStatus(String status) { this.virusTotalStatus = status; return this; }
        public Builder passwordProtected(boolean passwordProtected) { this.passwordProtected = passwordProtected; return this; }
        public Builder previewEnabled(boolean previewEnabled) { this.previewEnabled = previewEnabled; return this; }
        public Builder createdAt(Long createdAt) { this.createdAt = createdAt; return this; }
        public Builder expiresAt(Long expiresAt) { this.expiresAt = expiresAt; return this; }
        public Builder clickCount(Long clickCount) { this.clickCount = clickCount; return this; }
        public Builder active(boolean active) { this.active = active; return this; }
        public Builder iosUrl(String iosUrl) { this.iosUrl = iosUrl; return this; }
        public Builder androidUrl(String androidUrl) { this.androidUrl = androidUrl; return this; }
        public Builder desktopUrl(String desktopUrl) { this.desktopUrl = desktopUrl; return this; }
        public Builder webhookUrl(String webhookUrl) { this.webhookUrl = webhookUrl; return this; }
        public Builder healthStatus(String healthStatus) { this.healthStatus = healthStatus; return this; }
        public Builder lastHealthCheck(Long lastHealthCheck) { this.lastHealthCheck = lastHealthCheck; return this; }
        public Builder healthStatusCode(Integer healthStatusCode) { this.healthStatusCode = healthStatusCode; return this; }
        public Builder healthErrorMessage(String healthErrorMessage) { this.healthErrorMessage = healthErrorMessage; return this; }
        public Builder healthResponseTimeMs(Long healthResponseTimeMs) { this.healthResponseTimeMs = healthResponseTimeMs; return this; }

        public UrlPreviewResponse build() {
            return new UrlPreviewResponse(shortCode, shortUrl, originalUrl, domain, protocol, secure, safetyStatus,
                    safetyScore, googleSafeBrowsingStatus, virusTotalStatus, passwordProtected, previewEnabled,
                    createdAt, expiresAt, clickCount, active, iosUrl, androidUrl, desktopUrl, webhookUrl,
                    healthStatus, lastHealthCheck, healthStatusCode, healthErrorMessage, healthResponseTimeMs);
        }
    }

    public String getShortCode() { return shortCode; }
    public void setShortCode(String shortCode) { this.shortCode = shortCode; }

    public String getShortUrl() { return shortUrl; }
    public void setShortUrl(String shortUrl) { this.shortUrl = shortUrl; }

    public String getOriginalUrl() { return originalUrl; }
    public void setOriginalUrl(String originalUrl) { this.originalUrl = originalUrl; }

    public String getDomain() { return domain; }
    public void setDomain(String domain) { this.domain = domain; }

    public String getProtocol() { return protocol; }
    public void setProtocol(String protocol) { this.protocol = protocol; }

    public boolean isSecure() { return secure; }
    public void setSecure(boolean secure) { this.secure = secure; }

    public String getSafetyStatus() { return safetyStatus; }
    public void setSafetyStatus(String safetyStatus) { this.safetyStatus = safetyStatus; }

    public int getSafetyScore() { return safetyScore; }
    public void setSafetyScore(int safetyScore) { this.safetyScore = safetyScore; }

    public String getGoogleSafeBrowsingStatus() { return googleSafeBrowsingStatus; }
    public void setGoogleSafeBrowsingStatus(String googleSafeBrowsingStatus) { this.googleSafeBrowsingStatus = googleSafeBrowsingStatus; }

    public String getVirusTotalStatus() { return virusTotalStatus; }
    public void setVirusTotalStatus(String virusTotalStatus) { this.virusTotalStatus = virusTotalStatus; }

    public boolean isPasswordProtected() { return passwordProtected; }
    public void setPasswordProtected(boolean passwordProtected) { this.passwordProtected = passwordProtected; }

    public boolean isPreviewEnabled() { return previewEnabled; }
    public void setPreviewEnabled(boolean previewEnabled) { this.previewEnabled = previewEnabled; }

    public Long getCreatedAt() { return createdAt; }
    public void setCreatedAt(Long createdAt) { this.createdAt = createdAt; }

    public Long getExpiresAt() { return expiresAt; }
    public void setExpiresAt(Long expiresAt) { this.expiresAt = expiresAt; }

    public Long getClickCount() { return clickCount; }
    public void setClickCount(Long clickCount) { this.clickCount = clickCount; }

    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }

    public String getIosUrl() { return iosUrl; }
    public void setIosUrl(String iosUrl) { this.iosUrl = iosUrl; }

    public String getAndroidUrl() { return androidUrl; }
    public void setAndroidUrl(String androidUrl) { this.androidUrl = androidUrl; }

    public String getDesktopUrl() { return desktopUrl; }
    public void setDesktopUrl(String desktopUrl) { this.desktopUrl = desktopUrl; }

    public String getWebhookUrl() { return webhookUrl; }
    public void setWebhookUrl(String webhookUrl) { this.webhookUrl = webhookUrl; }

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
}