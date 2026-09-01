package com.urlshortener.dto;

public class TotpSetupResponse {

    private String secretKey;
    private String qrCodeUrl;
    private String otpAuthUrl;

    public TotpSetupResponse() {}

    public TotpSetupResponse(String secretKey, String qrCodeUrl, String otpAuthUrl) {
        this.secretKey = secretKey;
        this.qrCodeUrl = qrCodeUrl;
        this.otpAuthUrl = otpAuthUrl;
    }

    public String getSecretKey() {
        return secretKey;
    }

    public void setSecretKey(String secretKey) {
        this.secretKey = secretKey;
    }

    public String getQrCodeUrl() {
        return qrCodeUrl;
    }

    public void setQrCodeUrl(String qrCodeUrl) {
        this.qrCodeUrl = qrCodeUrl;
    }

    public String getOtpAuthUrl() {
        return otpAuthUrl;
    }

    public void setOtpAuthUrl(String otpAuthUrl) {
        this.otpAuthUrl = otpAuthUrl;
    }
}
