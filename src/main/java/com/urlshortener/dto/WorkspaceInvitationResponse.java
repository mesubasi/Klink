package com.urlshortener.dto;

import com.urlshortener.model.WorkspaceRole;

import java.util.UUID;

public class WorkspaceInvitationResponse {

    private UUID id;
    private String email;
    private WorkspaceRole role;
    private String invitedBy;
    private Long createdAt;
    private Long expiresAt;

    public WorkspaceInvitationResponse() {}

    public WorkspaceInvitationResponse(UUID id, String email, WorkspaceRole role, String invitedBy, Long createdAt, Long expiresAt) {
        this.id = id;
        this.email = email;
        this.role = role;
        this.invitedBy = invitedBy;
        this.createdAt = createdAt;
        this.expiresAt = expiresAt;
    }

    public UUID getId() { return id; }
    public String getEmail() { return email; }
    public WorkspaceRole getRole() { return role; }
    public String getInvitedBy() { return invitedBy; }
    public Long getCreatedAt() { return createdAt; }
    public Long getExpiresAt() { return expiresAt; }
}
