package com.urlshortener.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;

import java.io.Serializable;
import java.util.UUID;

@Entity
@Table(name = "url_variants", indexes = {
    @Index(name = "idx_variant_mapping", columnList = "url_mapping_id")
})
public class UrlVariant implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "url_mapping_id", nullable = false)
    @JsonIgnore
    private UrlMapping urlMapping;

    @Column(nullable = false, length = 100)
    private String label;

    @Column(nullable = false, length = 2048)
    private String targetUrl;

    @Column(nullable = false)
    private Integer weightPercent = 50;

    @Column(nullable = false)
    private Long clickCount = 0L;

    @Column(nullable = false)
    private boolean active = true;

    @Column(nullable = false)
    private Long createdAt;

    public UrlVariant() {}

    public UrlVariant(UUID id, UrlMapping urlMapping, String label, String targetUrl, Integer weightPercent, Long clickCount, boolean active, Long createdAt) {
        this.id = id;
        this.urlMapping = urlMapping;
        this.label = label;
        this.targetUrl = targetUrl;
        this.weightPercent = weightPercent != null ? weightPercent : 50;
        this.clickCount = clickCount != null ? clickCount : 0L;
        this.active = active;
        this.createdAt = createdAt != null ? createdAt : System.currentTimeMillis();
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private UUID id;
        private UrlMapping urlMapping;
        private String label;
        private String targetUrl;
        private Integer weightPercent = 50;
        private Long clickCount = 0L;
        private boolean active = true;
        private Long createdAt;

        public Builder id(UUID id) { this.id = id; return this; }
        public Builder urlMapping(UrlMapping urlMapping) { this.urlMapping = urlMapping; return this; }
        public Builder label(String label) { this.label = label; return this; }
        public Builder targetUrl(String targetUrl) { this.targetUrl = targetUrl; return this; }
        public Builder weightPercent(Integer weightPercent) { this.weightPercent = weightPercent; return this; }
        public Builder clickCount(Long clickCount) { this.clickCount = clickCount; return this; }
        public Builder active(boolean active) { this.active = active; return this; }
        public Builder createdAt(Long createdAt) { this.createdAt = createdAt; return this; }

        public UrlVariant build() {
            return new UrlVariant(id, urlMapping, label, targetUrl, weightPercent, clickCount, active, createdAt);
        }
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public UrlMapping getUrlMapping() { return urlMapping; }
    public void setUrlMapping(UrlMapping urlMapping) { this.urlMapping = urlMapping; }

    public String getLabel() { return label; }
    public void setLabel(String label) { this.label = label; }

    public String getTargetUrl() { return targetUrl; }
    public void setTargetUrl(String targetUrl) { this.targetUrl = targetUrl; }

    public Integer getWeightPercent() { return weightPercent; }
    public void setWeightPercent(Integer weightPercent) { this.weightPercent = weightPercent; }

    public Long getClickCount() { return clickCount; }
    public void setClickCount(Long clickCount) { this.clickCount = clickCount; }

    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }

    public Long getCreatedAt() { return createdAt; }
    public void setCreatedAt(Long createdAt) { this.createdAt = createdAt; }
}
