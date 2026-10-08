package com.urlshortener.dto;

/** Plan limits for a workspace; null or 0 means unlimited. */
public class UpdateQuotaRequest {

    private Integer maxMembers;
    private Integer maxLinks;

    public UpdateQuotaRequest() {}

    public Integer getMaxMembers() { return maxMembers; }
    public void setMaxMembers(Integer maxMembers) { this.maxMembers = maxMembers; }
    public Integer getMaxLinks() { return maxLinks; }
    public void setMaxLinks(Integer maxLinks) { this.maxLinks = maxLinks; }
}
