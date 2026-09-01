package com.urlshortener.dto;

import java.util.UUID;

public class UserDto {
    private UUID id;
    private String username;
    private String email;
    private String role;
    private boolean twoFactorEnabled;
    private Long createdAt;

    public UserDto() {}

    public UserDto(UUID id, String username, String email, String role, boolean twoFactorEnabled, Long createdAt) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.role = role;
        this.twoFactorEnabled = twoFactorEnabled;
        this.createdAt = createdAt;
    }

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private UUID id;
        private String username;
        private String email;
        private String role;
        private boolean twoFactorEnabled;
        private Long createdAt;

        public Builder id(UUID id) { this.id = id; return this; }
        public Builder username(String username) { this.username = username; return this; }
        public Builder email(String email) { this.email = email; return this; }
        public Builder role(String role) { this.role = role; return this; }
        public Builder twoFactorEnabled(boolean twoFactorEnabled) { this.twoFactorEnabled = twoFactorEnabled; return this; }
        public Builder createdAt(Long createdAt) { this.createdAt = createdAt; return this; }

        public UserDto build() {
            return new UserDto(id, username, email, role, twoFactorEnabled, createdAt);
        }
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
    public boolean isTwoFactorEnabled() { return twoFactorEnabled; }
    public void setTwoFactorEnabled(boolean twoFactorEnabled) { this.twoFactorEnabled = twoFactorEnabled; }
    public Long getCreatedAt() { return createdAt; }
    public void setCreatedAt(Long createdAt) { this.createdAt = createdAt; }
}
