package com.urlshortener.dto;

import com.urlshortener.model.WorkspaceRole;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class AddWorkspaceMemberRequest {

    @NotBlank(message = "E-posta adresi boş olamaz.")
    @Email(message = "Geçerli bir e-posta adresi giriniz.")
    private String email;

    @NotNull(message = "Çalışma alanı rolü belirtilmelidir.")
    private WorkspaceRole role = WorkspaceRole.MEMBER;

    public AddWorkspaceMemberRequest() {}

    public AddWorkspaceMemberRequest(String email, WorkspaceRole role) {
        this.email = email;
        this.role = role != null ? role : WorkspaceRole.MEMBER;
    }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public WorkspaceRole getRole() { return role; }
    public void setRole(WorkspaceRole role) { this.role = role; }
}
