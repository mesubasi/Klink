package com.urlshortener.dto;

import java.io.Serializable;
import java.util.UUID;

public class BioLinkItemDto implements Serializable {

    private static final long serialVersionUID = 1L;

    private UUID id;
    private String title;
    private String url;
    private String icon;
    private boolean highlighted;
    private boolean active;
    private int sortOrder;
    private Long clickCount;

    public BioLinkItemDto() {}

    public BioLinkItemDto(UUID id, String title, String url, String icon, boolean highlighted, boolean active, int sortOrder, Long clickCount) {
        this.id = id;
        this.title = title;
        this.url = url;
        this.icon = icon;
        this.highlighted = highlighted;
        this.active = active;
        this.sortOrder = sortOrder;
        this.clickCount = clickCount;
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

        public Builder id(UUID id) { this.id = id; return this; }
        public Builder title(String title) { this.title = title; return this; }
        public Builder url(String url) { this.url = url; return this; }
        public Builder icon(String icon) { this.icon = icon; return this; }
        public Builder highlighted(boolean highlighted) { this.highlighted = highlighted; return this; }
        public Builder active(boolean active) { this.active = active; return this; }
        public Builder sortOrder(int sortOrder) { this.sortOrder = sortOrder; return this; }
        public Builder clickCount(Long clickCount) { this.clickCount = clickCount; return this; }

        public BioLinkItemDto build() {
            return new BioLinkItemDto(id, title, url, icon, highlighted, active, sortOrder, clickCount);
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
}
