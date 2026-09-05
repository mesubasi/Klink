package com.urlshortener.dto;

public class ShortenResponse {
    private String shortCode;
    private String shortUrl;
    private String originalUrl;
    private Long createdAt;
    private Long expiresAt;
    private Long clickCount;
    private Long maxClicks;
    private String fallbackUrl;
    private boolean passwordProtected;
    private String blockedCountries;
    private String blockedIps;
    private boolean previewEnabled;
    private String iosUrl;
    private String androidUrl;
    private String desktopUrl;
    private String webhookUrl;
    private String healthStatus;
    private Long lastHealthCheck;
    private Integer healthStatusCode;
    private String healthErrorMessage;
    private Long healthResponseTimeMs;
    private String workspaceId;
    private String workspaceName;
    private boolean abTestingEnabled;
    private java.util.List<UrlVariantResponse> variants;

    public ShortenResponse() {}

    public ShortenResponse(String shortCode, String shortUrl, String originalUrl, Long createdAt, Long expiresAt, Long clickCount, boolean passwordProtected) {
        this(shortCode, shortUrl, originalUrl, createdAt, expiresAt, clickCount, passwordProtected, null, null, false, null, null, null, null, "UNKNOWN", null, null, null, null);
    }

    public ShortenResponse(String shortCode, String shortUrl, String originalUrl, Long createdAt, Long expiresAt, Long clickCount, boolean passwordProtected, String blockedCountries, String blockedIps) {
        this(shortCode, shortUrl, originalUrl, createdAt, expiresAt, clickCount, passwordProtected, blockedCountries, blockedIps, false, null, null, null, null, "UNKNOWN", null, null, null, null);
    }

    public ShortenResponse(String shortCode, String shortUrl, String originalUrl, Long createdAt, Long expiresAt, Long clickCount, boolean passwordProtected, String blockedCountries, String blockedIps, boolean previewEnabled) {
        this(shortCode, shortUrl, originalUrl, createdAt, expiresAt, clickCount, passwordProtected, blockedCountries, blockedIps, previewEnabled, null, null, null, null, "UNKNOWN", null, null, null, null);
    }

    public ShortenResponse(String shortCode, String shortUrl, String originalUrl, Long createdAt, Long expiresAt, Long clickCount, boolean passwordProtected, String blockedCountries, String blockedIps, boolean previewEnabled, String iosUrl, String androidUrl, String desktopUrl) {
        this(shortCode, shortUrl, originalUrl, createdAt, expiresAt, clickCount, passwordProtected, blockedCountries, blockedIps, previewEnabled, iosUrl, androidUrl, desktopUrl, null, "UNKNOWN", null, null, null, null);
    }

    public ShortenResponse(String shortCode, String shortUrl, String originalUrl, Long createdAt, Long expiresAt, Long clickCount, boolean passwordProtected, String blockedCountries, String blockedIps, boolean previewEnabled, String iosUrl, String androidUrl, String desktopUrl, String webhookUrl) {
        this(shortCode, shortUrl, originalUrl, createdAt, expiresAt, clickCount, passwordProtected, blockedCountries, blockedIps, previewEnabled, iosUrl, androidUrl, desktopUrl, webhookUrl, "UNKNOWN", null, null, null, null);
    }

    public ShortenResponse(String shortCode, String shortUrl, String originalUrl, Long createdAt, Long expiresAt, Long clickCount, boolean passwordProtected, String blockedCountries, String blockedIps, boolean previewEnabled, String iosUrl, String androidUrl, String desktopUrl, String webhookUrl, String healthStatus, Long lastHealthCheck, Integer healthStatusCode, String healthErrorMessage, Long healthResponseTimeMs) {
        this.shortCode = shortCode;
        this.shortUrl = shortUrl;
        this.originalUrl = originalUrl;
        this.createdAt = createdAt;
        this.expiresAt = expiresAt;
        this.clickCount = clickCount;
        this.passwordProtected = passwordProtected;
        this.blockedCountries = blockedCountries;
        this.blockedIps = blockedIps;
        this.previewEnabled = previewEnabled;
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

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private String shortCode;
        private String shortUrl;
        private String originalUrl;
        private Long createdAt;
        private Long expiresAt;
        private Long clickCount;
        private Long maxClicks;
        private String fallbackUrl;
        private boolean passwordProtected;
        private String blockedCountries;
        private String blockedIps;
        private boolean previewEnabled;
        private String iosUrl;
        private String androidUrl;
        private String desktopUrl;
        private String webhookUrl;
        private String healthStatus = "UNKNOWN";
        private Long lastHealthCheck;
        private Integer healthStatusCode;
        private String healthErrorMessage;
        private Long healthResponseTimeMs;
        private String workspaceId;
        private String workspaceName;
        private boolean abTestingEnabled;
        private java.util.List<UrlVariantResponse> variants;

        public Builder shortCode(String shortCode) { this.shortCode = shortCode; return this; }
        public Builder shortUrl(String shortUrl) { this.shortUrl = shortUrl; return this; }
        public Builder originalUrl(String originalUrl) { this.originalUrl = originalUrl; return this; }
        public Builder createdAt(Long createdAt) { this.createdAt = createdAt; return this; }
        public Builder expiresAt(Long expiresAt) { this.expiresAt = expiresAt; return this; }
        public Builder clickCount(Long clickCount) { this.clickCount = clickCount; return this; }
        public Builder maxClicks(Long maxClicks) { this.maxClicks = maxClicks; return this; }
        public Builder fallbackUrl(String fallbackUrl) { this.fallbackUrl = fallbackUrl; return this; }
        public Builder passwordProtected(boolean passwordProtected) { this.passwordProtected = passwordProtected; return this; }
        public Builder blockedCountries(String blockedCountries) { this.blockedCountries = blockedCountries; return this; }
        public Builder blockedIps(String blockedIps) { this.blockedIps = blockedIps; return this; }
        public Builder previewEnabled(boolean previewEnabled) { this.previewEnabled = previewEnabled; return this; }
        public Builder iosUrl(String iosUrl) { this.iosUrl = iosUrl; return this; }
        public Builder androidUrl(String androidUrl) { this.androidUrl = androidUrl; return this; }
        public Builder desktopUrl(String desktopUrl) { this.desktopUrl = desktopUrl; return this; }
        public Builder webhookUrl(String webhookUrl) { this.webhookUrl = webhookUrl; return this; }
        public Builder healthStatus(String healthStatus) { this.healthStatus = healthStatus; return this; }
        public Builder lastHealthCheck(Long lastHealthCheck) { this.lastHealthCheck = lastHealthCheck; return this; }
        public Builder healthStatusCode(Integer healthStatusCode) { this.healthStatusCode = healthStatusCode; return this; }
        public Builder healthErrorMessage(String healthErrorMessage) { this.healthErrorMessage = healthErrorMessage; return this; }
        public Builder healthResponseTimeMs(Long healthResponseTimeMs) { this.healthResponseTimeMs = healthResponseTimeMs; return this; }
        public Builder workspaceId(String workspaceId) { this.workspaceId = workspaceId; return this; }
        public Builder workspaceName(String workspaceName) { this.workspaceName = workspaceName; return this; }
        public Builder abTestingEnabled(boolean abTestingEnabled) { this.abTestingEnabled = abTestingEnabled; return this; }
        public Builder variants(java.util.List<UrlVariantResponse> variants) { this.variants = variants; return this; }

        public ShortenResponse build() {
            ShortenResponse resp = new ShortenResponse(shortCode, shortUrl, originalUrl, createdAt, expiresAt, clickCount, passwordProtected, blockedCountries, blockedIps, previewEnabled, iosUrl, androidUrl, desktopUrl, webhookUrl, healthStatus, lastHealthCheck, healthStatusCode, healthErrorMessage, healthResponseTimeMs);
            resp.setMaxClicks(maxClicks);
            resp.setFallbackUrl(fallbackUrl);
            resp.setWorkspaceId(workspaceId);
            resp.setWorkspaceName(workspaceName);
            resp.setAbTestingEnabled(abTestingEnabled);
            resp.setVariants(variants);
            return resp;
        }
    }

    public String getShortCode() { return shortCode; }
    public void setShortCode(String shortCode) { this.shortCode = shortCode; }
    public String getShortUrl() { return shortUrl; }
    public void setShortUrl(String shortUrl) { this.shortUrl = shortUrl; }
    public String getOriginalUrl() { return originalUrl; }
    public void setOriginalUrl(String originalUrl) { this.originalUrl = originalUrl; }
    public Long getCreatedAt() { return createdAt; }
    public void setCreatedAt(Long createdAt) { this.createdAt = createdAt; }
    public Long getExpiresAt() { return expiresAt; }
    public void setExpiresAt(Long expiresAt) { this.expiresAt = expiresAt; }
    public Long getClickCount() { return clickCount; }
    public void setClickCount(Long clickCount) { this.clickCount = clickCount; }
    public boolean isPasswordProtected() { return passwordProtected; }
    public void setPasswordProtected(boolean passwordProtected) { this.passwordProtected = passwordProtected; }
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
    public String getWorkspaceId() { return workspaceId; }
    public void setWorkspaceId(String workspaceId) { this.workspaceId = workspaceId; }
    public String getWorkspaceName() { return workspaceName; }
    public void setWorkspaceName(String workspaceName) { this.workspaceName = workspaceName; }
    public boolean isAbTestingEnabled() { return abTestingEnabled; }
    public void setAbTestingEnabled(boolean abTestingEnabled) { this.abTestingEnabled = abTestingEnabled; }
    public java.util.List<UrlVariantResponse> getVariants() { return variants; }
    public void setVariants(java.util.List<UrlVariantResponse> variants) { this.variants = variants; }
    public Long getMaxClicks() { return maxClicks; }
    public void setMaxClicks(Long maxClicks) { this.maxClicks = maxClicks; }
    public String getFallbackUrl() { return fallbackUrl; }
    public void setFallbackUrl(String fallbackUrl) { this.fallbackUrl = fallbackUrl; }
}
