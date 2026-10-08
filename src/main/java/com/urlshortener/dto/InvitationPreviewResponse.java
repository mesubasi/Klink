package com.urlshortener.dto;

import com.urlshortener.model.WorkspaceRole;

public class InvitationPreviewResponse {

    private String workspaceName;
    private String email;
    private WorkspaceRole role;
    private String invitedBy;
    private Long expiresAt;

    public InvitationPreviewResponse() {}

    public InvitationPreviewResponse(String workspaceName, String email, WorkspaceRole role, String invitedBy, Long expiresAt) {
        this.workspaceName = workspaceName;
        this.email = email;
        this.role = role;
        this.invitedBy = invitedBy;
        this.expiresAt = expiresAt;
    }

    public String getWorkspaceName() { return workspaceName; }
    public String getEmail() { return email; }
    public WorkspaceRole getRole() { return role; }
    public String getInvitedBy() { return invitedBy; }
    public Long getExpiresAt() { return expiresAt; }
}
