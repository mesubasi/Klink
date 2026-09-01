package com.urlshortener.dto;

import com.urlshortener.model.ClickAnalytics;

import java.util.List;

public class UrlStatsResponse {
    private String shortCode;
    private String originalUrl;
    private String shortUrl;
    private Long createdAt;
    private Long expiresAt;
    private Long totalClicks;
    private List<ClickAnalytics> recentClicks;
    private String blockedCountries;
    private String blockedIps;

    public UrlStatsResponse() {}

    public UrlStatsResponse(String shortCode, String originalUrl, String shortUrl, Long createdAt, Long expiresAt, Long totalClicks, List<ClickAnalytics> recentClicks) {
        this(shortCode, originalUrl, shortUrl, createdAt, expiresAt, totalClicks, recentClicks, null, null);
    }

    public UrlStatsResponse(String shortCode, String originalUrl, String shortUrl, Long createdAt, Long expiresAt, Long totalClicks, List<ClickAnalytics> recentClicks, String blockedCountries, String blockedIps) {
        this.shortCode = shortCode;
        this.originalUrl = originalUrl;
        this.shortUrl = shortUrl;
        this.createdAt = createdAt;
        this.expiresAt = expiresAt;
        this.totalClicks = totalClicks;
        this.recentClicks = recentClicks;
        this.blockedCountries = blockedCountries;
        this.blockedIps = blockedIps;
    }

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private String shortCode;
        private String originalUrl;
        private String shortUrl;
        private Long createdAt;
        private Long expiresAt;
        private Long totalClicks;
        private List<ClickAnalytics> recentClicks;
        private String blockedCountries;
        private String blockedIps;

        public Builder shortCode(String shortCode) { this.shortCode = shortCode; return this; }
        public Builder originalUrl(String originalUrl) { this.originalUrl = originalUrl; return this; }
        public Builder shortUrl(String shortUrl) { this.shortUrl = shortUrl; return this; }
        public Builder createdAt(Long createdAt) { this.createdAt = createdAt; return this; }
        public Builder expiresAt(Long expiresAt) { this.expiresAt = expiresAt; return this; }
        public Builder totalClicks(Long totalClicks) { this.totalClicks = totalClicks; return this; }
        public Builder recentClicks(List<ClickAnalytics> recentClicks) { this.recentClicks = recentClicks; return this; }
        public Builder blockedCountries(String blockedCountries) { this.blockedCountries = blockedCountries; return this; }
        public Builder blockedIps(String blockedIps) { this.blockedIps = blockedIps; return this; }

        public UrlStatsResponse build() {
            return new UrlStatsResponse(shortCode, originalUrl, shortUrl, createdAt, expiresAt, totalClicks, recentClicks, blockedCountries, blockedIps);
        }
    }

    public String getShortCode() { return shortCode; }
    public void setShortCode(String shortCode) { this.shortCode = shortCode; }
    public String getOriginalUrl() { return originalUrl; }
    public void setOriginalUrl(String originalUrl) { this.originalUrl = originalUrl; }
    public String getShortUrl() { return shortUrl; }
    public void setShortUrl(String shortUrl) { this.shortUrl = shortUrl; }
    public Long getCreatedAt() { return createdAt; }
    public void setCreatedAt(Long createdAt) { this.createdAt = createdAt; }
    public Long getExpiresAt() { return expiresAt; }
    public void setExpiresAt(Long expiresAt) { this.expiresAt = expiresAt; }
    public Long getTotalClicks() { return totalClicks; }
    public void setTotalClicks(Long totalClicks) { this.totalClicks = totalClicks; }
    public List<ClickAnalytics> getRecentClicks() { return recentClicks; }
    public void setRecentClicks(List<ClickAnalytics> recentClicks) { this.recentClicks = recentClicks; }
    public String getBlockedCountries() { return blockedCountries; }
    public void setBlockedCountries(String blockedCountries) { this.blockedCountries = blockedCountries; }
    public String getBlockedIps() { return blockedIps; }
    public void setBlockedIps(String blockedIps) { this.blockedIps = blockedIps; }
}
