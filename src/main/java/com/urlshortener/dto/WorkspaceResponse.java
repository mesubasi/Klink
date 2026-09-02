package com.urlshortener.dto;

import com.urlshortener.model.WorkspaceRole;
import java.util.List;
import java.util.UUID;

public class WorkspaceResponse {

    private UUID id;
    private String name;
    private String description;
    private String slug;
    private String ownerUsername;
    private WorkspaceRole currentUserRole;
    private long memberCount;
    private long linkCount;
    private Long createdAt;
    private List<WorkspaceMemberResponse> members;

    public WorkspaceResponse() {}

    public WorkspaceResponse(UUID id, String name, String description, String slug, String ownerUsername, WorkspaceRole currentUserRole, long memberCount, long linkCount, Long createdAt, List<WorkspaceMemberResponse> members) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.slug = slug;
        this.ownerUsername = ownerUsername;
        this.currentUserRole = currentUserRole;
        this.memberCount = memberCount;
        this.linkCount = linkCount;
        this.createdAt = createdAt;
        this.members = members;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private UUID id;
        private String name;
        private String description;
        private String slug;
        private String ownerUsername;
        private WorkspaceRole currentUserRole;
        private long memberCount;
        private long linkCount;
        private Long createdAt;
        private List<WorkspaceMemberResponse> members;

        public Builder id(UUID id) { this.id = id; return this; }
        public Builder name(String name) { this.name = name; return this; }
        public Builder description(String description) { this.description = description; return this; }
        public Builder slug(String slug) { this.slug = slug; return this; }
        public Builder ownerUsername(String ownerUsername) { this.ownerUsername = ownerUsername; return this; }
        public Builder currentUserRole(WorkspaceRole currentUserRole) { this.currentUserRole = currentUserRole; return this; }
        public Builder memberCount(long memberCount) { this.memberCount = memberCount; return this; }
        public Builder linkCount(long linkCount) { this.linkCount = linkCount; return this; }
        public Builder createdAt(Long createdAt) { this.createdAt = createdAt; return this; }
        public Builder members(List<WorkspaceMemberResponse> members) { this.members = members; return this; }

        public WorkspaceResponse build() {
            return new WorkspaceResponse(id, name, description, slug, ownerUsername, currentUserRole, memberCount, linkCount, createdAt, members);
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

    public String getOwnerUsername() { return ownerUsername; }
    public void setOwnerUsername(String ownerUsername) { this.ownerUsername = ownerUsername; }

    public WorkspaceRole getCurrentUserRole() { return currentUserRole; }
    public void setCurrentUserRole(WorkspaceRole currentUserRole) { this.currentUserRole = currentUserRole; }

    public long getMemberCount() { return memberCount; }
    public void setMemberCount(long memberCount) { this.memberCount = memberCount; }

    public long getLinkCount() { return linkCount; }
    public void setLinkCount(long linkCount) { this.linkCount = linkCount; }

    public Long getCreatedAt() { return createdAt; }
    public void setCreatedAt(Long createdAt) { this.createdAt = createdAt; }

    public List<WorkspaceMemberResponse> getMembers() { return members; }
    public void setMembers(List<WorkspaceMemberResponse> members) { this.members = members; }
}
