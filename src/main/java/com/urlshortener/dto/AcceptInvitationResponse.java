package com.urlshortener.dto;

import com.urlshortener.model.WorkspaceRole;

import java.util.UUID;

public class AcceptInvitationResponse {

    private UUID workspaceId;
    private String workspaceName;
    private WorkspaceRole role;

    public AcceptInvitationResponse() {}

    public AcceptInvitationResponse(UUID workspaceId, String workspaceName, WorkspaceRole role) {
        this.workspaceId = workspaceId;
        this.workspaceName = workspaceName;
        this.role = role;
    }

    public UUID getWorkspaceId() { return workspaceId; }
    public String getWorkspaceName() { return workspaceName; }
    public WorkspaceRole getRole() { return role; }
}
