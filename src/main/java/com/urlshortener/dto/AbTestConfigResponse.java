package com.urlshortener.dto;

import java.io.Serializable;
import java.util.List;

public class AbTestConfigResponse implements Serializable {

    private static final long serialVersionUID = 1L;

    private String shortCode;
    private boolean abTestingEnabled;
    private List<UrlVariantResponse> variants;
    private Long totalClicks;

    public AbTestConfigResponse() {}

    public AbTestConfigResponse(String shortCode, boolean abTestingEnabled, List<UrlVariantResponse> variants, Long totalClicks) {
        this.shortCode = shortCode;
        this.abTestingEnabled = abTestingEnabled;
        this.variants = variants;
        this.totalClicks = totalClicks;
    }

    public String getShortCode() { return shortCode; }
    public void setShortCode(String shortCode) { this.shortCode = shortCode; }

    public boolean isAbTestingEnabled() { return abTestingEnabled; }
    public void setAbTestingEnabled(boolean abTestingEnabled) { this.abTestingEnabled = abTestingEnabled; }

    public List<UrlVariantResponse> getVariants() { return variants; }
    public void setVariants(List<UrlVariantResponse> variants) { this.variants = variants; }

    public Long getTotalClicks() { return totalClicks; }
    public void setTotalClicks(Long totalClicks) { this.totalClicks = totalClicks; }
}
