package com.urlshortener.model;

import jakarta.persistence.*;
import java.io.Serializable;
import java.util.UUID;

@Entity
@Table(name = "workspace_permission_policies", uniqueConstraints = {
    @UniqueConstraint(name = "uk_ws_policy_role", columnNames = {"workspace_id", "role"})
}, indexes = {
    @Index(name = "idx_wpp_workspace", columnList = "workspace_id"),
    @Index(name = "idx_wpp_role", columnList = "role")
})
public class WorkspacePermissionPolicy implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "workspace_id", nullable = false)
    private Workspace workspace;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private WorkspaceRole role;

    @Column(nullable = false)
    private boolean canCreateLink = true;

    @Column(nullable = false)
    private boolean canDeleteLink = true;

    @Column(nullable = false)
    private boolean canExportReports = true;

    @Column(nullable = false)
    private boolean canCustomizeQr = true;

    @Column(nullable = false)
    private boolean canManageWebhooks = false;

    @Column(nullable = false)
    private boolean canViewAnalytics = true;

    @Column(nullable = false)
    private Long updatedAt;

    public WorkspacePermissionPolicy() {}

    public WorkspacePermissionPolicy(UUID id, Workspace workspace, WorkspaceRole role,
                                   boolean canCreateLink, boolean canDeleteLink,
                                   boolean canExportReports, boolean canCustomizeQr,
                                   boolean canManageWebhooks, boolean canViewAnalytics,
                                   Long updatedAt) {
        this.id = id;
        this.workspace = workspace;
        this.role = role;
        this.canCreateLink = canCreateLink;
        this.canDeleteLink = canDeleteLink;
        this.canExportReports = canExportReports;
        this.canCustomizeQr = canCustomizeQr;
        this.canManageWebhooks = canManageWebhooks;
        this.canViewAnalytics = canViewAnalytics;
        this.updatedAt = updatedAt != null ? updatedAt : System.currentTimeMillis();
    }

    @PrePersist
    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = System.currentTimeMillis();
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private UUID id;
        private Workspace workspace;
        private WorkspaceRole role;
        private boolean canCreateLink = true;
        private boolean canDeleteLink = true;
        private boolean canExportReports = true;
        private boolean canCustomizeQr = true;
        private boolean canManageWebhooks = false;
        private boolean canViewAnalytics = true;
        private Long updatedAt;

        public Builder id(UUID id) { this.id = id; return this; }
        public Builder workspace(Workspace workspace) { this.workspace = workspace; return this; }
        public Builder role(WorkspaceRole role) { this.role = role; return this; }
        public Builder canCreateLink(boolean canCreateLink) { this.canCreateLink = canCreateLink; return this; }
        public Builder canDeleteLink(boolean canDeleteLink) { this.canDeleteLink = canDeleteLink; return this; }
        public Builder canExportReports(boolean canExportReports) { this.canExportReports = canExportReports; return this; }
        public Builder canCustomizeQr(boolean canCustomizeQr) { this.canCustomizeQr = canCustomizeQr; return this; }
        public Builder canManageWebhooks(boolean canManageWebhooks) { this.canManageWebhooks = canManageWebhooks; return this; }
        public Builder canViewAnalytics(boolean canViewAnalytics) { this.canViewAnalytics = canViewAnalytics; return this; }
        public Builder updatedAt(Long updatedAt) { this.updatedAt = updatedAt; return this; }

        public WorkspacePermissionPolicy build() {
            return new WorkspacePermissionPolicy(id, workspace, role, canCreateLink, canDeleteLink, canExportReports, canCustomizeQr, canManageWebhooks, canViewAnalytics, updatedAt);
        }
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public Workspace getWorkspace() { return workspace; }
    public void setWorkspace(Workspace workspace) { this.workspace = workspace; }

    public WorkspaceRole getRole() { return role; }
    public void setRole(WorkspaceRole role) { this.role = role; }

    public boolean isCanCreateLink() { return canCreateLink; }
    public void setCanCreateLink(boolean canCreateLink) { this.canCreateLink = canCreateLink; }

    public boolean isCanDeleteLink() { return canDeleteLink; }
    public void setCanDeleteLink(boolean canDeleteLink) { this.canDeleteLink = canDeleteLink; }

    public boolean isCanExportReports() { return canExportReports; }
    public void setCanExportReports(boolean canExportReports) { this.canExportReports = canExportReports; }

    public boolean isCanCustomizeQr() { return canCustomizeQr; }
    public void setCanCustomizeQr(boolean canCustomizeQr) { this.canCustomizeQr = canCustomizeQr; }

    public boolean isCanManageWebhooks() { return canManageWebhooks; }
    public void setCanManageWebhooks(boolean canManageWebhooks) { this.canManageWebhooks = canManageWebhooks; }

    public boolean isCanViewAnalytics() { return canViewAnalytics; }
    public void setCanViewAnalytics(boolean canViewAnalytics) { this.canViewAnalytics = canViewAnalytics; }

    public Long getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Long updatedAt) { this.updatedAt = updatedAt; }
}
