package com.urlshortener.dto;

import java.io.Serializable;
import java.util.UUID;

public class UrlVariantResponse implements Serializable {

    private static final long serialVersionUID = 1L;

    private UUID id;
    private String label;
    private String targetUrl;
    private Integer weightPercent;
    private Long clickCount;
    private Double trafficSharePercent;
    private boolean active;
    private Long createdAt;

    public UrlVariantResponse() {}

    public UrlVariantResponse(UUID id, String label, String targetUrl, Integer weightPercent, Long clickCount, Double trafficSharePercent, boolean active, Long createdAt) {
        this.id = id;
        this.label = label;
        this.targetUrl = targetUrl;
        this.weightPercent = weightPercent;
        this.clickCount = clickCount;
        this.trafficSharePercent = trafficSharePercent;
        this.active = active;
        this.createdAt = createdAt;
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public String getLabel() { return label; }
    public void setLabel(String label) { this.label = label; }

    public String getTargetUrl() { return targetUrl; }
    public void setTargetUrl(String targetUrl) { this.targetUrl = targetUrl; }

    public Integer getWeightPercent() { return weightPercent; }
    public void setWeightPercent(Integer weightPercent) { this.weightPercent = weightPercent; }

    public Long getClickCount() { return clickCount; }
    public void setClickCount(Long clickCount) { this.clickCount = clickCount; }

    public Double getTrafficSharePercent() { return trafficSharePercent; }
    public void setTrafficSharePercent(Double trafficSharePercent) { this.trafficSharePercent = trafficSharePercent; }

    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }

    public Long getCreatedAt() { return createdAt; }
    public void setCreatedAt(Long createdAt) { this.createdAt = createdAt; }
}
