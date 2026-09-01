package com.urlshortener.model;

import jakarta.persistence.*;

import java.io.Serializable;
import java.util.UUID;

@Entity
@Table(name = "users", indexes = {
    @Index(name = "idx_username", columnList = "username"),
    @Index(name = "idx_email", columnList = "email")
})
public class UserAccount implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, unique = true, length = 50)
    private String username;

    @Column(nullable = false, unique = true, length = 100)
    private String email;

    @Column(nullable = false, length = 255)
    private String password;

    @Column(nullable = false, length = 20)
    private String role = "ROLE_USER";

    @Column(nullable = false)
    private boolean twoFactorEnabled = false;

    @Column(length = 64)
    private String twoFactorSecret;

    @Column(nullable = false)
    private Long createdAt;

    public UserAccount() {}

    public UserAccount(UUID id, String username, String email, String password, String role, boolean twoFactorEnabled, String twoFactorSecret, Long createdAt) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.password = password;
        this.role = role != null ? role : "ROLE_USER";
        this.twoFactorEnabled = twoFactorEnabled;
        this.twoFactorSecret = twoFactorSecret;
        this.createdAt = createdAt != null ? createdAt : System.currentTimeMillis();
    }

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = System.currentTimeMillis();
        }
        if (role == null) {
            role = "ROLE_USER";
        }
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private UUID id;
        private String username;
        private String email;
        private String password;
        private String role = "ROLE_USER";
        private boolean twoFactorEnabled = false;
        private String twoFactorSecret;
        private Long createdAt;

        public Builder id(UUID id) { this.id = id; return this; }
        public Builder username(String username) { this.username = username; return this; }
        public Builder email(String email) { this.email = email; return this; }
        public Builder password(String password) { this.password = password; return this; }
        public Builder role(String role) { this.role = role; return this; }
        public Builder twoFactorEnabled(boolean twoFactorEnabled) { this.twoFactorEnabled = twoFactorEnabled; return this; }
        public Builder twoFactorSecret(String twoFactorSecret) { this.twoFactorSecret = twoFactorSecret; return this; }
        public Builder createdAt(Long createdAt) { this.createdAt = createdAt; return this; }

        public UserAccount build() {
            return new UserAccount(id, username, email, password, role, twoFactorEnabled, twoFactorSecret, createdAt);
        }
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
    public boolean isTwoFactorEnabled() { return twoFactorEnabled; }
    public void setTwoFactorEnabled(boolean twoFactorEnabled) { this.twoFactorEnabled = twoFactorEnabled; }
    public String getTwoFactorSecret() { return twoFactorSecret; }
    public void setTwoFactorSecret(String twoFactorSecret) { this.twoFactorSecret = twoFactorSecret; }
    public Long getCreatedAt() { return createdAt; }
    public void setCreatedAt(Long createdAt) { this.createdAt = createdAt; }
}
