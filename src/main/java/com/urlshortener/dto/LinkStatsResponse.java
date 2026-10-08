package com.urlshortener.dto;

public class LinkStatsResponse {

    private long totalLinks;
    private long totalClicks;
    private long protectedCount;
    private long brokenCount;
    private long healthyCount;

    public LinkStatsResponse() {
    }

    public LinkStatsResponse(Long totalLinks, Long totalClicks, Long protectedCount, Long brokenCount, Long healthyCount) {
        this.totalLinks = totalLinks == null ? 0 : totalLinks;
        this.totalClicks = totalClicks == null ? 0 : totalClicks;
        this.protectedCount = protectedCount == null ? 0 : protectedCount;
        this.brokenCount = brokenCount == null ? 0 : brokenCount;
        this.healthyCount = healthyCount == null ? 0 : healthyCount;
    }

    public long getTotalLinks() { return totalLinks; }
    public void setTotalLinks(long totalLinks) { this.totalLinks = totalLinks; }

    public long getTotalClicks() { return totalClicks; }
    public void setTotalClicks(long totalClicks) { this.totalClicks = totalClicks; }

    public long getProtectedCount() { return protectedCount; }
    public void setProtectedCount(long protectedCount) { this.protectedCount = protectedCount; }

    public long getBrokenCount() { return brokenCount; }
    public void setBrokenCount(long brokenCount) { this.brokenCount = brokenCount; }

    public long getHealthyCount() { return healthyCount; }
    public void setHealthyCount(long healthyCount) { this.healthyCount = healthyCount; }
}
