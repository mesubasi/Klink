package com.urlshortener.dto;

import java.io.Serializable;
import java.util.UUID;

public class ClickEventDto implements Serializable {

    private static final long serialVersionUID = 1L;

    private String shortCode;
    private Long clickedAt;
    private String ipAddress;
    private String userAgent;
    private String referrer;

    private String country;
    private String countryCode;
    private String city;

    private boolean bot;
    private String botCategory;

    private UUID variantId;
    private String variantLabel;

    public ClickEventDto() {}

    public ClickEventDto(String shortCode, Long clickedAt, String ipAddress, String userAgent, String referrer, String country, String countryCode, String city, boolean bot, String botCategory) {
        this(shortCode, clickedAt, ipAddress, userAgent, referrer, country, countryCode, city, bot, botCategory, null, null);
    }

    public ClickEventDto(String shortCode, Long clickedAt, String ipAddress, String userAgent, String referrer, String country, String countryCode, String city, boolean bot, String botCategory, UUID variantId, String variantLabel) {
        this.shortCode = shortCode;
        this.clickedAt = clickedAt;
        this.ipAddress = ipAddress;
        this.userAgent = userAgent;
        this.referrer = referrer;
        this.country = country;
        this.countryCode = countryCode;
        this.city = city;
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
        private Long clickedAt;
        private String ipAddress;
        private String userAgent;
        private String referrer;
        private String country;
        private String countryCode;
        private String city;
        private boolean bot;
        private String botCategory;
        private UUID variantId;
        private String variantLabel;

        public Builder shortCode(String shortCode) { this.shortCode = shortCode; return this; }
        public Builder clickedAt(Long clickedAt) { this.clickedAt = clickedAt; return this; }
        public Builder ipAddress(String ipAddress) { this.ipAddress = ipAddress; return this; }
        public Builder userAgent(String userAgent) { this.userAgent = userAgent; return this; }
        public Builder referrer(String referrer) { this.referrer = referrer; return this; }
        public Builder country(String country) { this.country = country; return this; }
        public Builder countryCode(String countryCode) { this.countryCode = countryCode; return this; }
        public Builder city(String city) { this.city = city; return this; }
        public Builder bot(boolean bot) { this.bot = bot; return this; }
        public Builder botCategory(String botCategory) { this.botCategory = botCategory; return this; }
        public Builder variantId(UUID variantId) { this.variantId = variantId; return this; }
        public Builder variantLabel(String variantLabel) { this.variantLabel = variantLabel; return this; }

        public ClickEventDto build() {
            return new ClickEventDto(shortCode, clickedAt, ipAddress, userAgent, referrer, country, countryCode, city, bot, botCategory, variantId, variantLabel);
        }
    }

    public String getShortCode() { return shortCode; }
    public void setShortCode(String shortCode) { this.shortCode = shortCode; }
    public Long getClickedAt() { return clickedAt; }
    public void setClickedAt(Long clickedAt) { this.clickedAt = clickedAt; }
    public String getIpAddress() { return ipAddress; }
    public void setIpAddress(String ipAddress) { this.ipAddress = ipAddress; }
    public String getUserAgent() { return userAgent; }
    public void setUserAgent(String userAgent) { this.userAgent = userAgent; }
    public String getReferrer() { return referrer; }
    public void setReferrer(String referrer) { this.referrer = referrer; }
    public String getCountry() { return country; }
    public void setCountry(String country) { this.country = country; }
    public String getCountryCode() { return countryCode; }
    public void setCountryCode(String countryCode) { this.countryCode = countryCode; }
    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }
    public boolean isBot() { return bot; }
    public void setBot(boolean bot) { this.bot = bot; }
    public String getBotCategory() { return botCategory; }
    public void setBotCategory(String botCategory) { this.botCategory = botCategory; }
    public UUID getVariantId() { return variantId; }
    public void setVariantId(UUID variantId) { this.variantId = variantId; }
    public String getVariantLabel() { return variantLabel; }
    public void setVariantLabel(String variantLabel) { this.variantLabel = variantLabel; }
}
