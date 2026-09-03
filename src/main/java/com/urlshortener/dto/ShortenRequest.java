package com.urlshortener.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public class ShortenRequest {

    @NotBlank(message = "{validation.original_url.notblank}")
    @Pattern(regexp = "^(https?://)?([a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,}(/.*)?$", message = "{validation.original_url.invalid}")
    private String originalUrl;

    @Pattern(regexp = "^$|^[a-zA-Z0-9_-]{3,20}$", message = "{validation.custom_alias.pattern}")
    private String customAlias;

    @Min(value = 1, message = "{validation.expiration.min}")
    @Max(value = 365, message = "{validation.expiration.max}")
    private Integer expirationDays;

    private String password;

    private String fallbackUrl;

    private String blockedCountries;

    private String blockedIps;

    private Boolean previewEnabled;

    private Long expiresAt;

    private String iosUrl;

    private String androidUrl;

    private String desktopUrl;

    private String webhookUrl;

    private String webhookSecret;

    private String workspaceId;

    private Boolean abTestingEnabled = false;

    private java.util.List<UrlVariantRequest> variants;

    public ShortenRequest() {}

    public ShortenRequest(String originalUrl, String customAlias, Integer expirationDays, String password) {
        this.originalUrl = originalUrl;
        this.customAlias = customAlias;
        this.expirationDays = expirationDays;
        this.password = password;
    }

    public ShortenRequest(String originalUrl, String customAlias, Integer expirationDays, String password, String fallbackUrl) {
        this.originalUrl = originalUrl;
        this.customAlias = customAlias;
        this.expirationDays = expirationDays;
        this.password = password;
        this.fallbackUrl = fallbackUrl;
    }

    public ShortenRequest(String originalUrl, String customAlias, Integer expirationDays, String password, String fallbackUrl, String blockedCountries, String blockedIps) {
        this.originalUrl = originalUrl;
        this.customAlias = customAlias;
        this.expirationDays = expirationDays;
        this.password = password;
        this.fallbackUrl = fallbackUrl;
        this.blockedCountries = blockedCountries;
        this.blockedIps = blockedIps;
    }

    public ShortenRequest(String originalUrl, String customAlias, Integer expirationDays, String password, String fallbackUrl, String blockedCountries, String blockedIps, Boolean previewEnabled) {
        this(originalUrl, customAlias, expirationDays, password, fallbackUrl, blockedCountries, blockedIps, previewEnabled, null);
    }

    public ShortenRequest(String originalUrl, String customAlias, Integer expirationDays, String password, String fallbackUrl, String blockedCountries, String blockedIps, Boolean previewEnabled, Long expiresAt) {
        this(originalUrl, customAlias, expirationDays, password, fallbackUrl, blockedCountries, blockedIps, previewEnabled, expiresAt, null, null, null, null, null);
    }

    public ShortenRequest(String originalUrl, String customAlias, Integer expirationDays, String password, String fallbackUrl, String blockedCountries, String blockedIps, Boolean previewEnabled, Long expiresAt, String iosUrl, String androidUrl, String desktopUrl) {
        this(originalUrl, customAlias, expirationDays, password, fallbackUrl, blockedCountries, blockedIps, previewEnabled, expiresAt, iosUrl, androidUrl, desktopUrl, null, null);
    }

    public ShortenRequest(String originalUrl, String customAlias, Integer expirationDays, String password, String fallbackUrl, String blockedCountries, String blockedIps, Boolean previewEnabled, Long expiresAt, String iosUrl, String androidUrl, String desktopUrl, String webhookUrl, String webhookSecret) {
        this.originalUrl = originalUrl;
        this.customAlias = customAlias;
        this.expirationDays = expirationDays;
        this.password = password;
        this.fallbackUrl = fallbackUrl;
        this.blockedCountries = blockedCountries;
        this.blockedIps = blockedIps;
        this.previewEnabled = previewEnabled;
        this.expiresAt = expiresAt;
        this.iosUrl = iosUrl;
        this.androidUrl = androidUrl;
        this.desktopUrl = desktopUrl;
        this.webhookUrl = webhookUrl;
        this.webhookSecret = webhookSecret;
    }

    public String getOriginalUrl() { return originalUrl; }
    public void setOriginalUrl(String originalUrl) { this.originalUrl = originalUrl; }
    public String getCustomAlias() { return customAlias; }
    public void setCustomAlias(String customAlias) { this.customAlias = customAlias; }
    public Integer getExpirationDays() { return expirationDays; }
    public void setExpirationDays(Integer expirationDays) { this.expirationDays = expirationDays; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    public String getFallbackUrl() { return fallbackUrl; }
    public void setFallbackUrl(String fallbackUrl) { this.fallbackUrl = fallbackUrl; }
    public String getBlockedCountries() { return blockedCountries; }
    public void setBlockedCountries(String blockedCountries) { this.blockedCountries = blockedCountries; }
    public String getBlockedIps() { return blockedIps; }
    public void setBlockedIps(String blockedIps) { this.blockedIps = blockedIps; }
    public Boolean getPreviewEnabled() { return previewEnabled; }
    public void setPreviewEnabled(Boolean previewEnabled) { this.previewEnabled = previewEnabled; }
    public Long getExpiresAt() { return expiresAt; }
    public void setExpiresAt(Long expiresAt) { this.expiresAt = expiresAt; }
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
    public String getWorkspaceId() { return workspaceId; }
    public void setWorkspaceId(String workspaceId) { this.workspaceId = workspaceId; }
    public Boolean getAbTestingEnabled() { return abTestingEnabled; }
    public void setAbTestingEnabled(Boolean abTestingEnabled) { this.abTestingEnabled = abTestingEnabled; }
    public java.util.List<UrlVariantRequest> getVariants() { return variants; }
    public void setVariants(java.util.List<UrlVariantRequest> variants) { this.variants = variants; }
}
