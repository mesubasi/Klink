package com.urlshortener.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class TotpLoginRequest {

    @NotBlank(message = "Kullanıcı adı boş olamaz")
    private String username;

    @NotBlank(message = "Şifre boş olamaz")
    private String password;

    @NotBlank(message = "2FA doğrulama kodu boş olamaz")
    @Size(min = 6, max = 6, message = "2FA doğrulama kodu 6 haneli olmalıdır")
    private String code;

    public TotpLoginRequest() {}

    public TotpLoginRequest(String username, String password, String code) {
        this.username = username;
        this.password = password;
        this.code = code;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }
}
