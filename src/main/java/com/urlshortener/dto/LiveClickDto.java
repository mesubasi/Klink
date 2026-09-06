package com.urlshortener.dto;

import java.io.Serializable;
import java.util.UUID;

public class LiveClickDto implements Serializable {

    private static final long serialVersionUID = 1L;

    private String shortCode;
    private String originalUrl;
    private Long clickedAt;
    private String country;
    private String countryCode;
    private String city;
    private String maskedIp;
    private String userAgent;
    private String deviceType;
    private String browser;
    private String os;
    private String referrer;
    private boolean bot;
    private String botCategory;
    private UUID variantId;
    private String variantLabel;

    public LiveClickDto() {}

    public LiveClickDto(String shortCode, String originalUrl, Long clickedAt, String country,
                        String countryCode, String city, String maskedIp, String userAgent,
                        String deviceType, String browser, String os, String referrer,
                        boolean bot, String botCategory, UUID variantId, String variantLabel) {
        this.shortCode = shortCode;
        this.originalUrl = originalUrl;
        this.clickedAt = clickedAt;
        this.country = country;
        this.countryCode = countryCode;
        this.city = city;
        this.maskedIp = maskedIp;
        this.userAgent = userAgent;
        this.deviceType = deviceType;
        this.browser = browser;
        this.os = os;
        this.referrer = referrer;
        this.bot = bot;
        this.botCategory = botCategory;
        this.variantId = variantId;
        this.variantLabel = variantLabel;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String shortCode;
        private String originalUrl;
        private Long clickedAt;
        private String country;
        private String countryCode;
        private String city;
        private String maskedIp;
        private String userAgent;
        private String deviceType;
        private String browser;
        private String os;
        private String referrer;
        private boolean bot;
        private String botCategory;
        private UUID variantId;
        private String variantLabel;

        public Builder shortCode(String shortCode) { this.shortCode = shortCode; return this; }
        public Builder originalUrl(String originalUrl) { this.originalUrl = originalUrl; return this; }
        public Builder clickedAt(Long clickedAt) { this.clickedAt = clickedAt; return this; }
        public Builder country(String country) { this.country = country; return this; }
        public Builder countryCode(String countryCode) { this.countryCode = countryCode; return this; }
        public Builder city(String city) { this.city = city; return this; }
        public Builder maskedIp(String maskedIp) { this.maskedIp = maskedIp; return this; }
        public Builder userAgent(String userAgent) { this.userAgent = userAgent; return this; }
        public Builder deviceType(String deviceType) { this.deviceType = deviceType; return this; }
        public Builder browser(String browser) { this.browser = browser; return this; }
        public Builder os(String os) { this.os = os; return this; }
        public Builder referrer(String referrer) { this.referrer = referrer; return this; }
        public Builder bot(boolean bot) { this.bot = bot; return this; }
        public Builder botCategory(String botCategory) { this.botCategory = botCategory; return this; }
        public Builder variantId(UUID variantId) { this.variantId = variantId; return this; }
        public Builder variantLabel(String variantLabel) { this.variantLabel = variantLabel; return this; }

        public LiveClickDto build() {
            return new LiveClickDto(shortCode, originalUrl, clickedAt, country, countryCode,
                    city, maskedIp, userAgent, deviceType, browser, os, referrer, bot,
                    botCategory, variantId, variantLabel);
        }
    }

    // Getters and Setters
    public String getShortCode() { return shortCode; }
    public void setShortCode(String shortCode) { this.shortCode = shortCode; }

    public String getOriginalUrl() { return originalUrl; }
    public void setOriginalUrl(String originalUrl) { this.originalUrl = originalUrl; }

    public Long getClickedAt() { return clickedAt; }
    public void setClickedAt(Long clickedAt) { this.clickedAt = clickedAt; }

    public String getCountry() { return country; }
    public void setCountry(String country) { this.country = country; }

    public String getCountryCode() { return countryCode; }
    public void setCountryCode(String countryCode) { this.countryCode = countryCode; }

    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }

    public String getMaskedIp() { return maskedIp; }
    public void setMaskedIp(String maskedIp) { this.maskedIp = maskedIp; }

    public String getUserAgent() { return userAgent; }
    public void setUserAgent(String userAgent) { this.userAgent = userAgent; }

    public String getDeviceType() { return deviceType; }
    public void setDeviceType(String deviceType) { this.deviceType = deviceType; }

    public String getBrowser() { return browser; }
    public void setBrowser(String browser) { this.browser = browser; }

    public String getOs() { return os; }
    public void setOs(String os) { this.os = os; }

    public String getReferrer() { return referrer; }
    public void setReferrer(String referrer) { this.referrer = referrer; }

    public boolean isBot() { return bot; }
    public void setBot(boolean bot) { this.bot = bot; }

    public String getBotCategory() { return botCategory; }
    public void setBotCategory(String botCategory) { this.botCategory = botCategory; }

    public UUID getVariantId() { return variantId; }
    public void setVariantId(UUID variantId) { this.variantId = variantId; }

    public String getVariantLabel() { return variantLabel; }
    public void setVariantLabel(String variantLabel) { this.variantLabel = variantLabel; }
}
