package com.urlshortener.dto;

import java.io.Serializable;
import java.util.UUID;

public class WorkspacePermissionMatrixResponse implements Serializable {

    private static final long serialVersionUID = 1L;

    private UUID workspaceId;
    private String workspaceName;
    private RolePermissionDto admin;
    private RolePermissionDto member;
    private RolePermissionDto viewer;
    private Long updatedAt;

    public WorkspacePermissionMatrixResponse() {}

    public WorkspacePermissionMatrixResponse(UUID workspaceId, String workspaceName, RolePermissionDto admin, RolePermissionDto member, RolePermissionDto viewer, Long updatedAt) {
        this.workspaceId = workspaceId;
        this.workspaceName = workspaceName;
        this.admin = admin;
        this.member = member;
        this.viewer = viewer;
        this.updatedAt = updatedAt;
    }

    public UUID getWorkspaceId() { return workspaceId; }
    public void setWorkspaceId(UUID workspaceId) { this.workspaceId = workspaceId; }

    public String getWorkspaceName() { return workspaceName; }
    public void setWorkspaceName(String workspaceName) { this.workspaceName = workspaceName; }

    public RolePermissionDto getAdmin() { return admin; }
    public void setAdmin(RolePermissionDto admin) { this.admin = admin; }

    public RolePermissionDto getMember() { return member; }
    public void setMember(RolePermissionDto member) { this.member = member; }

    public RolePermissionDto getViewer() { return viewer; }
    public void setViewer(RolePermissionDto viewer) { this.viewer = viewer; }

    public Long getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Long updatedAt) { this.updatedAt = updatedAt; }
}
