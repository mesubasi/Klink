package com.urlshortener.dto;

import com.urlshortener.model.WorkspaceRole;
import jakarta.validation.constraints.NotNull;

public class UpdateMemberRoleRequest {

    @NotNull(message = "Yeni rol belirtilmelidir.")
    private WorkspaceRole role;

    public UpdateMemberRoleRequest() {}

    public UpdateMemberRoleRequest(WorkspaceRole role) {
        this.role = role;
    }

    public WorkspaceRole getRole() { return role; }
    public void setRole(WorkspaceRole role) { this.role = role; }
}
