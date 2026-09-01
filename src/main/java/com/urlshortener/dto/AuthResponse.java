package com.urlshortener.dto;

public class AuthResponse {

    private String username;
    private String email;
    private String role;
    private String message;
    private String accessToken;
    private String tokenType = "Bearer";
    private boolean twoFactorRequired = false;

    public AuthResponse() {}

    public AuthResponse(String username, String email, String role, String message, String accessToken, String tokenType, boolean twoFactorRequired) {
        this.username = username;
        this.email = email;
        this.role = role;
        this.message = message;
        this.accessToken = accessToken;
        if (tokenType != null) {
            this.tokenType = tokenType;
        }
        this.twoFactorRequired = twoFactorRequired;
    }

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private String username;
        private String email;
        private String role;
        private String message;
        private String accessToken;
        private String tokenType = "Bearer";
        private boolean twoFactorRequired = false;

        public Builder username(String username) { this.username = username; return this; }
        public Builder email(String email) { this.email = email; return this; }
        public Builder role(String role) { this.role = role; return this; }
        public Builder message(String message) { this.message = message; return this; }
        public Builder accessToken(String accessToken) { this.accessToken = accessToken; return this; }
        public Builder tokenType(String tokenType) { this.tokenType = tokenType; return this; }
        public Builder twoFactorRequired(boolean twoFactorRequired) { this.twoFactorRequired = twoFactorRequired; return this; }

        public AuthResponse build() {
            return new AuthResponse(username, email, role, message, accessToken, tokenType, twoFactorRequired);
        }
    }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    public String getAccessToken() { return accessToken; }
    public void setAccessToken(String accessToken) { this.accessToken = accessToken; }
    public String getTokenType() { return tokenType; }
    public void setTokenType(String tokenType) { this.tokenType = tokenType; }
    public boolean isTwoFactorRequired() { return twoFactorRequired; }
    public void setTwoFactorRequired(boolean twoFactorRequired) { this.twoFactorRequired = twoFactorRequired; }
}
