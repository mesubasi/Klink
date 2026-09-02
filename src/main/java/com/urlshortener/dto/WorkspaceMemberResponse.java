package com.urlshortener.dto;

import com.urlshortener.model.WorkspaceRole;
import java.util.UUID;

public class WorkspaceMemberResponse {

    private UUID userId;
    private String username;
    private String email;
    private WorkspaceRole role;
    private Long joinedAt;

    public WorkspaceMemberResponse() {}

    public WorkspaceMemberResponse(UUID userId, String username, String email, WorkspaceRole role, Long joinedAt) {
        this.userId = userId;
        this.username = username;
        this.email = email;
        this.role = role;
        this.joinedAt = joinedAt;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private UUID userId;
        private String username;
        private String email;
        private WorkspaceRole role;
        private Long joinedAt;

        public Builder userId(UUID userId) { this.userId = userId; return this; }
        public Builder username(String username) { this.username = username; return this; }
        public Builder email(String email) { this.email = email; return this; }
        public Builder role(WorkspaceRole role) { this.role = role; return this; }
        public Builder joinedAt(Long joinedAt) { this.joinedAt = joinedAt; return this; }

        public WorkspaceMemberResponse build() {
            return new WorkspaceMemberResponse(userId, username, email, role, joinedAt);
        }
    }

    public UUID getUserId() { return userId; }
    public void setUserId(UUID userId) { this.userId = userId; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public WorkspaceRole getRole() { return role; }
    public void setRole(WorkspaceRole role) { this.role = role; }

    public Long getJoinedAt() { return joinedAt; }
    public void setJoinedAt(Long joinedAt) { this.joinedAt = joinedAt; }
}
