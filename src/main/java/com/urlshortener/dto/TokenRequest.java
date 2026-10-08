package com.urlshortener.dto;

import jakarta.validation.constraints.NotBlank;

public class TokenRequest {

    @NotBlank(message = "Token boş olamaz.")
    private String token;

    public TokenRequest() {}

    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }
}
