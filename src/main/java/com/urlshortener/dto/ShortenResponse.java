package com.urlshortener.dto;

public class ShortenResponse {
    private String shortCode;
    private String shortUrl;
    private String originalUrl;
    private Long createdAt;
    private Long expiresAt;
    private Long clickCount;
    private boolean passwordProtected;
    private String blockedCountries;
    private String blockedIps;
    private boolean previewEnabled;
    private String iosUrl;
    private String androidUrl;
    private String desktopUrl;
    private String webhookUrl;

    public ShortenResponse() {}

    public ShortenResponse(String shortCode, String shortUrl, String originalUrl, Long createdAt, Long expiresAt, Long clickCount, boolean passwordProtected) {
        this(shortCode, shortUrl, originalUrl, createdAt, expiresAt, clickCount, passwordProtected, null, null, false, null, null, null, null);
    }

    public ShortenResponse(String shortCode, String shortUrl, String originalUrl, Long createdAt, Long expiresAt, Long clickCount, boolean passwordProtected, String blockedCountries, String blockedIps) {
        this(shortCode, shortUrl, originalUrl, createdAt, expiresAt, clickCount, passwordProtected, blockedCountries, blockedIps, false, null, null, null, null);
    }

    public ShortenResponse(String shortCode, String shortUrl, String originalUrl, Long createdAt, Long expiresAt, Long clickCount, boolean passwordProtected, String blockedCountries, String blockedIps, boolean previewEnabled) {
        this(shortCode, shortUrl, originalUrl, createdAt, expiresAt, clickCount, passwordProtected, blockedCountries, blockedIps, previewEnabled, null, null, null, null);
    }

    public ShortenResponse(String shortCode, String shortUrl, String originalUrl, Long createdAt, Long expiresAt, Long clickCount, boolean passwordProtected, String blockedCountries, String blockedIps, boolean previewEnabled, String iosUrl, String androidUrl, String desktopUrl) {
        this(shortCode, shortUrl, originalUrl, createdAt, expiresAt, clickCount, passwordProtected, blockedCountries, blockedIps, previewEnabled, iosUrl, androidUrl, desktopUrl, null);
    }

    public ShortenResponse(String shortCode, String shortUrl, String originalUrl, Long createdAt, Long expiresAt, Long clickCount, boolean passwordProtected, String blockedCountries, String blockedIps, boolean previewEnabled, String iosUrl, String androidUrl, String desktopUrl, String webhookUrl) {
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
    }

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private String shortCode;
        private String shortUrl;
        private String originalUrl;
        private Long createdAt;
        private Long expiresAt;
        private Long clickCount;
        private boolean passwordProtected;
        private String blockedCountries;
        private String blockedIps;
        private boolean previewEnabled;
        private String iosUrl;
        private String androidUrl;
        private String desktopUrl;
        private String webhookUrl;

        public Builder shortCode(String shortCode) { this.shortCode = shortCode; return this; }
        public Builder shortUrl(String shortUrl) { this.shortUrl = shortUrl; return this; }
        public Builder originalUrl(String originalUrl) { this.originalUrl = originalUrl; return this; }
        public Builder createdAt(Long createdAt) { this.createdAt = createdAt; return this; }
        public Builder expiresAt(Long expiresAt) { this.expiresAt = expiresAt; return this; }
        public Builder clickCount(Long clickCount) { this.clickCount = clickCount; return this; }
        public Builder passwordProtected(boolean passwordProtected) { this.passwordProtected = passwordProtected; return this; }
        public Builder blockedCountries(String blockedCountries) { this.blockedCountries = blockedCountries; return this; }
        public Builder blockedIps(String blockedIps) { this.blockedIps = blockedIps; return this; }
        public Builder previewEnabled(boolean previewEnabled) { this.previewEnabled = previewEnabled; return this; }
        public Builder iosUrl(String iosUrl) { this.iosUrl = iosUrl; return this; }
        public Builder androidUrl(String androidUrl) { this.androidUrl = androidUrl; return this; }
        public Builder desktopUrl(String desktopUrl) { this.desktopUrl = desktopUrl; return this; }
        public Builder webhookUrl(String webhookUrl) { this.webhookUrl = webhookUrl; return this; }

        public ShortenResponse build() {
            return new ShortenResponse(shortCode, shortUrl, originalUrl, createdAt, expiresAt, clickCount, passwordProtected, blockedCountries, blockedIps, previewEnabled, iosUrl, androidUrl, desktopUrl, webhookUrl);
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
}
