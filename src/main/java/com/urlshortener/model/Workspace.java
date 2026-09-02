package com.urlshortener.model;

import jakarta.persistence.*;
import java.io.Serializable;
import java.util.UUID;

@Entity
@Table(name = "workspaces", indexes = {
    @Index(name = "idx_workspace_slug", columnList = "slug"),
    @Index(name = "idx_workspace_owner", columnList = "owner_id")
})
public class Workspace implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(length = 500)
    private String description;

    @Column(nullable = false, unique = true, length = 120)
    private String slug;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id", nullable = false)
    private UserAccount owner;

    @Column(nullable = false)
    private Long createdAt;

    public Workspace() {}

    public Workspace(UUID id, String name, String description, String slug, UserAccount owner, Long createdAt) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.slug = slug;
        this.owner = owner;
        this.createdAt = createdAt != null ? createdAt : System.currentTimeMillis();
    }

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = System.currentTimeMillis();
        }
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private UUID id;
        private String name;
        private String description;
        private String slug;
        private UserAccount owner;
        private Long createdAt;

        public Builder id(UUID id) { this.id = id; return this; }
        public Builder name(String name) { this.name = name; return this; }
        public Builder description(String description) { this.description = description; return this; }
        public Builder slug(String slug) { this.slug = slug; return this; }
        public Builder owner(UserAccount owner) { this.owner = owner; return this; }
        public Builder createdAt(Long createdAt) { this.createdAt = createdAt; return this; }

        public Workspace build() {
            return new Workspace(id, name, description, slug, owner, createdAt);
        }
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getSlug() { return slug; }
    public void setSlug(String slug) { this.slug = slug; }

    public UserAccount getOwner() { return owner; }
    public void setOwner(UserAccount owner) { this.owner = owner; }

    public Long getCreatedAt() { return createdAt; }
    public void setCreatedAt(Long createdAt) { this.createdAt = createdAt; }
}
