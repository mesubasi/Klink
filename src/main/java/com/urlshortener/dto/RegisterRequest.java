package com.urlshortener.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class RegisterRequest {

    @NotBlank(message = "{validation.username.notblank}")
    @Size(min = 3, max = 30, message = "{validation.username.size}")
    private String username;

    @NotBlank(message = "{validation.email.notblank}")
    @Email(message = "{validation.email.invalid}")
    private String email;

    @NotBlank(message = "{validation.password.notblank}")
    @Size(min = 6, max = 100, message = "{validation.password.size}")
    private String password;

    public RegisterRequest() {}

    public RegisterRequest(String username, String email, String password) {
        this.username = username;
        this.email = email;
        this.password = password;
    }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
}
