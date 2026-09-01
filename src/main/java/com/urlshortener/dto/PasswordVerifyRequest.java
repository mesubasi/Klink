package com.urlshortener.dto;

import jakarta.validation.constraints.NotBlank;

public class PasswordVerifyRequest {

    @NotBlank(message = "{validation.password.notblank}")
    private String password;

    public PasswordVerifyRequest() {}

    public PasswordVerifyRequest(String password) {
        this.password = password;
    }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
}
