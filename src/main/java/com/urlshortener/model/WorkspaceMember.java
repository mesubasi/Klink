package com.urlshortener.model;

import jakarta.persistence.*;
import java.io.Serializable;
import java.util.UUID;

@Entity
@Table(name = "workspace_members", uniqueConstraints = {
    @UniqueConstraint(name = "uk_workspace_user", columnNames = {"workspace_id", "user_id"})
}, indexes = {
    @Index(name = "idx_wm_workspace", columnList = "workspace_id"),
    @Index(name = "idx_wm_user", columnList = "user_id")
})
public class WorkspaceMember implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "workspace_id", nullable = false)
    private Workspace workspace;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private UserAccount user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private WorkspaceRole role = WorkspaceRole.MEMBER;

    @Column(nullable = false)
    private Long joinedAt;

    public WorkspaceMember() {}

    public WorkspaceMember(UUID id, Workspace workspace, UserAccount user, WorkspaceRole role, Long joinedAt) {
        this.id = id;
        this.workspace = workspace;
        this.user = user;
        this.role = role != null ? role : WorkspaceRole.MEMBER;
        this.joinedAt = joinedAt != null ? joinedAt : System.currentTimeMillis();
    }

    @PrePersist
    protected void onCreate() {
        if (joinedAt == null) {
            joinedAt = System.currentTimeMillis();
        }
        if (role == null) {
            role = WorkspaceRole.MEMBER;
        }
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private UUID id;
        private Workspace workspace;
        private UserAccount user;
        private WorkspaceRole role = WorkspaceRole.MEMBER;
        private Long joinedAt;

        public Builder id(UUID id) { this.id = id; return this; }
        public Builder workspace(Workspace workspace) { this.workspace = workspace; return this; }
        public Builder user(UserAccount user) { this.user = user; return this; }
        public Builder role(WorkspaceRole role) { this.role = role; return this; }
        public Builder joinedAt(Long joinedAt) { this.joinedAt = joinedAt; return this; }

        public WorkspaceMember build() {
            return new WorkspaceMember(id, workspace, user, role, joinedAt);
        }
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public Workspace getWorkspace() { return workspace; }
    public void setWorkspace(Workspace workspace) { this.workspace = workspace; }

    public UserAccount getUser() { return user; }
    public void setUser(UserAccount user) { this.user = user; }

    public WorkspaceRole getRole() { return role; }
    public void setRole(WorkspaceRole role) { this.role = role; }

    public Long getJoinedAt() { return joinedAt; }
    public void setJoinedAt(Long joinedAt) { this.joinedAt = joinedAt; }
}
