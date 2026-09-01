package com.urlshortener.dto;

import java.io.Serializable;

public class ApiKeyApplyRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    private String appName;
    private String purpose;
    private String websiteUrl;
    private String expectedMonthlyClicks;
    private String ipWhitelist;

    public ApiKeyApplyRequest() {}

    public ApiKeyApplyRequest(String appName, String purpose, String websiteUrl, String expectedMonthlyClicks, String ipWhitelist) {
        this.appName = appName;
        this.purpose = purpose;
        this.websiteUrl = websiteUrl;
        this.expectedMonthlyClicks = expectedMonthlyClicks;
        this.ipWhitelist = ipWhitelist;
    }

    public String getAppName() { return appName; }
    public void setAppName(String appName) { this.appName = appName; }
    public String getPurpose() { return purpose; }
    public void setPurpose(String purpose) { this.purpose = purpose; }
    public String getWebsiteUrl() { return websiteUrl; }
    public void setWebsiteUrl(String websiteUrl) { this.websiteUrl = websiteUrl; }
    public String getExpectedMonthlyClicks() { return expectedMonthlyClicks; }
    public void setExpectedMonthlyClicks(String expectedMonthlyClicks) { this.expectedMonthlyClicks = expectedMonthlyClicks; }
    public String getIpWhitelist() { return ipWhitelist; }
    public void setIpWhitelist(String ipWhitelist) { this.ipWhitelist = ipWhitelist; }
}
