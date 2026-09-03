package com.urlshortener.model;

import jakarta.persistence.*;

import java.io.Serializable;
import java.util.UUID;

@Entity
@Table(name = "click_analytics", indexes = {
    @Index(name = "idx_analytics_short_code", columnList = "shortCode")
})
public class ClickAnalytics implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, length = 20)
    private String shortCode;

    @Column(nullable = false)
    private Long clickedAt;

    private String ipAddress;
    private String userAgent;
    private String referrer;

    private String country;
    private String countryCode;
    private String city;

    @Column(nullable = false)
    private boolean bot = false;

    @Column(length = 100)
    private String botCategory;

    private UUID variantId;

    @Column(length = 100)
    private String variantLabel;

    public ClickAnalytics() {}

    public ClickAnalytics(UUID id, String shortCode, Long clickedAt, String ipAddress, String userAgent, String referrer, String country, String countryCode, String city, boolean bot, String botCategory) {
        this(id, shortCode, clickedAt, ipAddress, userAgent, referrer, country, countryCode, city, bot, botCategory, null, null);
    }

    public ClickAnalytics(UUID id, String shortCode, Long clickedAt, String ipAddress, String userAgent, String referrer, String country, String countryCode, String city, boolean bot, String botCategory, UUID variantId, String variantLabel) {
        this.id = id;
        this.shortCode = shortCode;
        this.clickedAt = clickedAt != null ? clickedAt : System.currentTimeMillis();
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

    @PrePersist
    protected void onCreate() {
        if (clickedAt == null) {
            clickedAt = System.currentTimeMillis();
        }
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private UUID id;
        private String shortCode;
        private Long clickedAt;
        private String ipAddress;
        private String userAgent;
        private String referrer;
        private String country;
        private String countryCode;
        private String city;
        private boolean bot = false;
        private String botCategory;
        private UUID variantId;
        private String variantLabel;

        public Builder id(UUID id) { this.id = id; return this; }
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

        public ClickAnalytics build() {
            return new ClickAnalytics(id, shortCode, clickedAt, ipAddress, userAgent, referrer, country, countryCode, city, bot, botCategory, variantId, variantLabel);
        }
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
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
