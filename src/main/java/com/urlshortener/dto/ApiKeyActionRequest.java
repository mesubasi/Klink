package com.urlshortener.dto;

import java.io.Serializable;

public class ApiKeyActionRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    private Integer rateLimitPerMinute;
    private String rejectionReason;

    public ApiKeyActionRequest() {}

    public ApiKeyActionRequest(Integer rateLimitPerMinute, String rejectionReason) {
        this.rateLimitPerMinute = rateLimitPerMinute;
        this.rejectionReason = rejectionReason;
    }

    public Integer getRateLimitPerMinute() { return rateLimitPerMinute; }
    public void setRateLimitPerMinute(Integer rateLimitPerMinute) { this.rateLimitPerMinute = rateLimitPerMinute; }
    public String getRejectionReason() { return rejectionReason; }
    public void setRejectionReason(String rejectionReason) { this.rejectionReason = rejectionReason; }
}
