package com.urlshortener.dto;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class BioPageUpdateRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    private String username;
    private String displayName;
    private String bioDescription;
    private String avatarUrl;
    private String theme;
    private String socialLinks;
    private List<BioLinkItemDto> links = new ArrayList<>();

    public BioPageUpdateRequest() {}

    public BioPageUpdateRequest(String username, String displayName, String bioDescription, String avatarUrl, String theme, String socialLinks, List<BioLinkItemDto> links) {
        this.username = username;
        this.displayName = displayName;
        this.bioDescription = bioDescription;
        this.avatarUrl = avatarUrl;
        this.theme = theme;
        this.socialLinks = socialLinks;
        if (links != null) {
            this.links = links;
        }
    }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getDisplayName() { return displayName; }
    public void setDisplayName(String displayName) { this.displayName = displayName; }
    public String getBioDescription() { return bioDescription; }
    public void setBioDescription(String bioDescription) { this.bioDescription = bioDescription; }
    public String getAvatarUrl() { return avatarUrl; }
    public void setAvatarUrl(String avatarUrl) { this.avatarUrl = avatarUrl; }
    public String getTheme() { return theme; }
    public void setTheme(String theme) { this.theme = theme; }
    public String getSocialLinks() { return socialLinks; }
    public void setSocialLinks(String socialLinks) { this.socialLinks = socialLinks; }
    public List<BioLinkItemDto> getLinks() { return links; }
    public void setLinks(List<BioLinkItemDto> links) { this.links = links; }
}
