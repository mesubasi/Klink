package com.urlshortener.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;

import java.io.Serializable;
import java.util.UUID;

@Entity
@Table(name = "bio_link_items", indexes = {
    @Index(name = "idx_biolink_page", columnList = "bio_page_id"),
    @Index(name = "idx_biolink_order", columnList = "sort_order")
})
public class BioLinkItem implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, length = 150)
    private String title;

    @Column(nullable = false, length = 2048)
    private String url;

    @Column(length = 50)
    private String icon = "Globe";

    @Column(nullable = false)
    private boolean highlighted = false;

    @Column(nullable = false)
    private boolean active = true;

    @Column(name = "sort_order", nullable = false)
    private int sortOrder = 0;

    @Column(nullable = false)
    private Long clickCount = 0L;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "bio_page_id")
    @JsonIgnore
    private BioPage bioPage;

    public BioLinkItem() {}

    public BioLinkItem(UUID id, String title, String url, String icon, boolean highlighted, boolean active, int sortOrder, Long clickCount, BioPage bioPage) {
        this.id = id;
        this.title = title;
        this.url = url;
        this.icon = icon != null ? icon : "Globe";
        this.highlighted = highlighted;
        this.active = active;
        this.sortOrder = sortOrder;
        this.clickCount = clickCount != null ? clickCount : 0L;
        this.bioPage = bioPage;
    }

    @PrePersist
    protected void onCreate() {
        if (clickCount == null) {
            clickCount = 0L;
        }
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private UUID id;
        private String title;
        private String url;
        private String icon = "Globe";
        private boolean highlighted = false;
        private boolean active = true;
        private int sortOrder = 0;
        private Long clickCount = 0L;
        private BioPage bioPage;

        public Builder id(UUID id) { this.id = id; return this; }
        public Builder title(String title) { this.title = title; return this; }
        public Builder url(String url) { this.url = url; return this; }
        public Builder icon(String icon) { this.icon = icon; return this; }
        public Builder highlighted(boolean highlighted) { this.highlighted = highlighted; return this; }
        public Builder active(boolean active) { this.active = active; return this; }
        public Builder sortOrder(int sortOrder) { this.sortOrder = sortOrder; return this; }
        public Builder clickCount(Long clickCount) { this.clickCount = clickCount; return this; }
        public Builder bioPage(BioPage bioPage) { this.bioPage = bioPage; return this; }

        public BioLinkItem build() {
            return new BioLinkItem(id, title, url, icon, highlighted, active, sortOrder, clickCount, bioPage);
        }
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getUrl() { return url; }
    public void setUrl(String url) { this.url = url; }
    public String getIcon() { return icon; }
    public void setIcon(String icon) { this.icon = icon; }
    public boolean isHighlighted() { return highlighted; }
    public void setHighlighted(boolean highlighted) { this.highlighted = highlighted; }
    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
    public int getSortOrder() { return sortOrder; }
    public void setSortOrder(int sortOrder) { this.sortOrder = sortOrder; }
    public Long getClickCount() { return clickCount; }
    public void setClickCount(Long clickCount) { this.clickCount = clickCount; }
    public BioPage getBioPage() { return bioPage; }
    public void setBioPage(BioPage bioPage) { this.bioPage = bioPage; }
}
