package com.urlshortener.dto;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class BioPageDto implements Serializable {

    private static final long serialVersionUID = 1L;

    private UUID id;
    private String username;
    private String displayName;
    private String bioDescription;
    private String avatarUrl;
    private String theme;
    private String socialLinks;
    private boolean verified;
    private Long viewCount;
    private Long createdAt;
    private Long updatedAt;
    private List<BioLinkItemDto> links = new ArrayList<>();

    public BioPageDto() {}

    public BioPageDto(UUID id, String username, String displayName, String bioDescription, String avatarUrl, String theme, String socialLinks, boolean verified, Long viewCount, Long createdAt, Long updatedAt, List<BioLinkItemDto> links) {
        this.id = id;
        this.username = username;
        this.displayName = displayName;
        this.bioDescription = bioDescription;
        this.avatarUrl = avatarUrl;
        this.theme = theme;
        this.socialLinks = socialLinks;
        this.verified = verified;
        this.viewCount = viewCount;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        if (links != null) {
            this.links = links;
        }
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private UUID id;
        private String username;
        private String displayName;
        private String bioDescription;
        private String avatarUrl;
        private String theme;
        private String socialLinks;
        private boolean verified;
        private Long viewCount;
        private Long createdAt;
        private Long updatedAt;
        private List<BioLinkItemDto> links = new ArrayList<>();

        public Builder id(UUID id) { this.id = id; return this; }
        public Builder username(String username) { this.username = username; return this; }
        public Builder displayName(String displayName) { this.displayName = displayName; return this; }
        public Builder bioDescription(String bioDescription) { this.bioDescription = bioDescription; return this; }
        public Builder avatarUrl(String avatarUrl) { this.avatarUrl = avatarUrl; return this; }
        public Builder theme(String theme) { this.theme = theme; return this; }
        public Builder socialLinks(String socialLinks) { this.socialLinks = socialLinks; return this; }
        public Builder verified(boolean verified) { this.verified = verified; return this; }
        public Builder viewCount(Long viewCount) { this.viewCount = viewCount; return this; }
        public Builder createdAt(Long createdAt) { this.createdAt = createdAt; return this; }
        public Builder updatedAt(Long updatedAt) { this.updatedAt = updatedAt; return this; }
        public Builder links(List<BioLinkItemDto> links) { this.links = links != null ? links : new ArrayList<>(); return this; }

        public BioPageDto build() {
            return new BioPageDto(id, username, displayName, bioDescription, avatarUrl, theme, socialLinks, verified, viewCount, createdAt, updatedAt, links);
        }
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
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
    public boolean isVerified() { return verified; }
    public void setVerified(boolean verified) { this.verified = verified; }
    public Long getViewCount() { return viewCount; }
    public void setViewCount(Long viewCount) { this.viewCount = viewCount; }
    public Long getCreatedAt() { return createdAt; }
    public void setCreatedAt(Long createdAt) { this.createdAt = createdAt; }
    public Long getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Long updatedAt) { this.updatedAt = updatedAt; }
    public List<BioLinkItemDto> getLinks() { return links; }
    public void setLinks(List<BioLinkItemDto> links) { this.links = links; }
}
