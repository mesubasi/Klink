package com.urlshortener.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class TotpVerifyRequest {

    @NotBlank(message = "Doğrulama kodu boş olamaz")
    @Size(min = 6, max = 6, message = "Doğrulama kodu 6 haneli olmalıdır")
    private String code;

    private String secretKey;

    public TotpVerifyRequest() {}

    public TotpVerifyRequest(String code, String secretKey) {
        this.code = code;
        this.secretKey = secretKey;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getSecretKey() {
        return secretKey;
    }

    public void setSecretKey(String secretKey) {
        this.secretKey = secretKey;
    }
}
