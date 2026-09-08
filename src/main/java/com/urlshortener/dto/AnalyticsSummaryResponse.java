package com.urlshortener.dto;

import java.util.Map;

public class AnalyticsSummaryResponse {

    private String shortCode;
    private String originalUrl;
    private Long totalClicks;
    private Long humanClicks;
    private Long botClicks;
    private Map<String, Long> clicksByDevice;
    private Map<String, Long> clicksByReferrer;
    private Map<String, Long> clicksByDate;
    private Map<String, Long> clicksByCountry;
    private Map<String, Long> clicksByCity;
    private Map<String, Long> clicksByBotCategory;
    private int[][] hourlyHeatmap;
    private Map<String, Long> clicksByUtmSource;
    private Map<String, Long> clicksByUtmCampaign;
    private Map<String, Long> clicksByUtmMedium;

    public AnalyticsSummaryResponse() {}

    public AnalyticsSummaryResponse(String shortCode, String originalUrl, Long totalClicks, Long humanClicks, Long botClicks, Map<String, Long> clicksByDevice, Map<String, Long> clicksByReferrer, Map<String, Long> clicksByDate, Map<String, Long> clicksByCountry, Map<String, Long> clicksByCity, Map<String, Long> clicksByBotCategory, int[][] hourlyHeatmap) {
        this(shortCode, originalUrl, totalClicks, humanClicks, botClicks, clicksByDevice, clicksByReferrer, clicksByDate, clicksByCountry, clicksByCity, clicksByBotCategory, hourlyHeatmap, null, null, null);
    }

    public AnalyticsSummaryResponse(String shortCode, String originalUrl, Long totalClicks, Long humanClicks, Long botClicks, Map<String, Long> clicksByDevice, Map<String, Long> clicksByReferrer, Map<String, Long> clicksByDate, Map<String, Long> clicksByCountry, Map<String, Long> clicksByCity, Map<String, Long> clicksByBotCategory, int[][] hourlyHeatmap, Map<String, Long> clicksByUtmSource, Map<String, Long> clicksByUtmCampaign, Map<String, Long> clicksByUtmMedium) {
        this.shortCode = shortCode;
        this.originalUrl = originalUrl;
        this.totalClicks = totalClicks;
        this.humanClicks = humanClicks;
        this.botClicks = botClicks;
        this.clicksByDevice = clicksByDevice;
        this.clicksByReferrer = clicksByReferrer;
        this.clicksByDate = clicksByDate;
        this.clicksByCountry = clicksByCountry;
        this.clicksByCity = clicksByCity;
        this.clicksByBotCategory = clicksByBotCategory;
        this.hourlyHeatmap = hourlyHeatmap;
        this.clicksByUtmSource = clicksByUtmSource;
        this.clicksByUtmCampaign = clicksByUtmCampaign;
        this.clicksByUtmMedium = clicksByUtmMedium;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String shortCode;
        private String originalUrl;
        private Long totalClicks;
        private Long humanClicks;
        private Long botClicks;
        private Map<String, Long> clicksByDevice;
        private Map<String, Long> clicksByReferrer;
        private Map<String, Long> clicksByDate;
        private Map<String, Long> clicksByCountry;
        private Map<String, Long> clicksByCity;
        private Map<String, Long> clicksByBotCategory;
        private int[][] hourlyHeatmap;
        private Map<String, Long> clicksByUtmSource;
        private Map<String, Long> clicksByUtmCampaign;
        private Map<String, Long> clicksByUtmMedium;

        public Builder shortCode(String shortCode) { this.shortCode = shortCode; return this; }
        public Builder originalUrl(String originalUrl) { this.originalUrl = originalUrl; return this; }
        public Builder totalClicks(Long totalClicks) { this.totalClicks = totalClicks; return this; }
        public Builder humanClicks(Long humanClicks) { this.humanClicks = humanClicks; return this; }
        public Builder botClicks(Long botClicks) { this.botClicks = botClicks; return this; }
        public Builder clicksByDevice(Map<String, Long> clicksByDevice) { this.clicksByDevice = clicksByDevice; return this; }
        public Builder clicksByReferrer(Map<String, Long> clicksByReferrer) { this.clicksByReferrer = clicksByReferrer; return this; }
        public Builder clicksByDate(Map<String, Long> clicksByDate) { this.clicksByDate = clicksByDate; return this; }
        public Builder clicksByCountry(Map<String, Long> clicksByCountry) { this.clicksByCountry = clicksByCountry; return this; }
        public Builder clicksByCity(Map<String, Long> clicksByCity) { this.clicksByCity = clicksByCity; return this; }
        public Builder clicksByBotCategory(Map<String, Long> clicksByBotCategory) { this.clicksByBotCategory = clicksByBotCategory; return this; }
        public Builder hourlyHeatmap(int[][] hourlyHeatmap) { this.hourlyHeatmap = hourlyHeatmap; return this; }
        public Builder clicksByUtmSource(Map<String, Long> clicksByUtmSource) { this.clicksByUtmSource = clicksByUtmSource; return this; }
        public Builder clicksByUtmCampaign(Map<String, Long> clicksByUtmCampaign) { this.clicksByUtmCampaign = clicksByUtmCampaign; return this; }
        public Builder clicksByUtmMedium(Map<String, Long> clicksByUtmMedium) { this.clicksByUtmMedium = clicksByUtmMedium; return this; }

        public AnalyticsSummaryResponse build() {
            return new AnalyticsSummaryResponse(shortCode, originalUrl, totalClicks, humanClicks, botClicks, clicksByDevice, clicksByReferrer, clicksByDate, clicksByCountry, clicksByCity, clicksByBotCategory, hourlyHeatmap, clicksByUtmSource, clicksByUtmCampaign, clicksByUtmMedium);
        }
    }

    public String getShortCode() { return shortCode; }
    public void setShortCode(String shortCode) { this.shortCode = shortCode; }
    public String getOriginalUrl() { return originalUrl; }
    public void setOriginalUrl(String originalUrl) { this.originalUrl = originalUrl; }
    public Long getTotalClicks() { return totalClicks; }
    public void setTotalClicks(Long totalClicks) { this.totalClicks = totalClicks; }
    public Long getHumanClicks() { return humanClicks; }
    public void setHumanClicks(Long humanClicks) { this.humanClicks = humanClicks; }
    public Long getBotClicks() { return botClicks; }
    public void setBotClicks(Long botClicks) { this.botClicks = botClicks; }
    public Map<String, Long> getClicksByDevice() { return clicksByDevice; }
    public void setClicksByDevice(Map<String, Long> clicksByDevice) { this.clicksByDevice = clicksByDevice; }
    public Map<String, Long> getClicksByReferrer() { return clicksByReferrer; }
    public void setClicksByReferrer(Map<String, Long> clicksByReferrer) { this.clicksByReferrer = clicksByReferrer; }
    public Map<String, Long> getClicksByDate() { return clicksByDate; }
    public void setClicksByDate(Map<String, Long> clicksByDate) { this.clicksByDate = clicksByDate; }
    public Map<String, Long> getClicksByCountry() { return clicksByCountry; }
    public void setClicksByCountry(Map<String, Long> clicksByCountry) { this.clicksByCountry = clicksByCountry; }
    public Map<String, Long> getClicksByCity() { return clicksByCity; }
    public void setClicksByCity(Map<String, Long> clicksByCity) { this.clicksByCity = clicksByCity; }
    public Map<String, Long> getClicksByBotCategory() { return clicksByBotCategory; }
    public void setClicksByBotCategory(Map<String, Long> clicksByBotCategory) { this.clicksByBotCategory = clicksByBotCategory; }
    public int[][] getHourlyHeatmap() { return hourlyHeatmap; }
    public void setHourlyHeatmap(int[][] hourlyHeatmap) { this.hourlyHeatmap = hourlyHeatmap; }
    public Map<String, Long> getClicksByUtmSource() { return clicksByUtmSource; }
    public void setClicksByUtmSource(Map<String, Long> clicksByUtmSource) { this.clicksByUtmSource = clicksByUtmSource; }
    public Map<String, Long> getClicksByUtmCampaign() { return clicksByUtmCampaign; }
    public void setClicksByUtmCampaign(Map<String, Long> clicksByUtmCampaign) { this.clicksByUtmCampaign = clicksByUtmCampaign; }
    public Map<String, Long> getClicksByUtmMedium() { return clicksByUtmMedium; }
    public void setClicksByUtmMedium(Map<String, Long> clicksByUtmMedium) { this.clicksByUtmMedium = clicksByUtmMedium; }
}

