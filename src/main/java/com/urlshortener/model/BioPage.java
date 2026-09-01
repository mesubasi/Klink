package com.urlshortener.model;

import jakarta.persistence.*;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "bio_pages", indexes = {
    @Index(name = "idx_biopage_username", columnList = "username", unique = true),
    @Index(name = "idx_biopage_user", columnList = "user_id")
})
public class BioPage implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, unique = true, length = 50)
    private String username;

    @Column(length = 100)
    private String displayName;

    @Column(length = 1000)
    private String bioDescription;

    @Column(length = 2048)
    private String avatarUrl;

    @Column(nullable = false, length = 50)
    private String theme = "classic_dark";

    @Column(length = 2048)
    private String socialLinks; // JSON map: {"twitter":"...", "instagram":"...", "github":"...", "youtube":"...", "linkedin":"...", "email":"..."}

    @Column(nullable = false)
    private boolean verified = false;

    @Column(nullable = false)
    private Long viewCount = 0L;

    @Column(nullable = false)
    private Long createdAt;

    @Column(nullable = false)
    private Long updatedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private UserAccount user;

    @OneToMany(mappedBy = "bioPage", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @OrderBy("sortOrder ASC")
    private List<BioLinkItem> links = new ArrayList<>();

    public BioPage() {}

    public BioPage(UUID id, String username, String displayName, String bioDescription, String avatarUrl, String theme, String socialLinks, boolean verified, Long viewCount, Long createdAt, Long updatedAt, UserAccount user, List<BioLinkItem> links) {
        this.id = id;
        this.username = username;
        this.displayName = displayName;
        this.bioDescription = bioDescription;
        this.avatarUrl = avatarUrl;
        this.theme = theme != null ? theme : "classic_dark";
        this.socialLinks = socialLinks;
        this.verified = verified;
        this.viewCount = viewCount != null ? viewCount : 0L;
        this.createdAt = createdAt != null ? createdAt : System.currentTimeMillis();
        this.updatedAt = updatedAt != null ? updatedAt : System.currentTimeMillis();
        this.user = user;
        if (links != null) {
            this.links = links;
        }
    }

    @PrePersist
    protected void onCreate() {
        long now = System.currentTimeMillis();
        if (createdAt == null) {
            createdAt = now;
        }
        if (updatedAt == null) {
            updatedAt = now;
        }
        if (viewCount == null) {
            viewCount = 0L;
        }
        if (theme == null) {
            theme = "classic_dark";
        }
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = System.currentTimeMillis();
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
        private String theme = "classic_dark";
        private String socialLinks;
        private boolean verified = false;
        private Long viewCount = 0L;
        private Long createdAt;
        private Long updatedAt;
        private UserAccount user;
        private List<BioLinkItem> links = new ArrayList<>();

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
        public Builder user(UserAccount user) { this.user = user; return this; }
        public Builder links(List<BioLinkItem> links) { this.links = links != null ? links : new ArrayList<>(); return this; }

        public BioPage build() {
            return new BioPage(id, username, displayName, bioDescription, avatarUrl, theme, socialLinks, verified, viewCount, createdAt, updatedAt, user, links);
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
    public UserAccount getUser() { return user; }
    public void setUser(UserAccount user) { this.user = user; }
    public List<BioLinkItem> getLinks() { return links; }
    public void setLinks(List<BioLinkItem> links) { this.links = links; }
}
