package com.urlshortener;

import com.urlshortener.service.TotpService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class TotpServiceTest {

    private TotpService totpService;

    @BeforeEach
    public void setup() {
        totpService = new TotpService();
    }

    @Test
    public void testGenerateSecretKey() {
        String secret = totpService.generateSecretKey();
        assertNotNull(secret);
        assertTrue(secret.length() >= 16);
    }

    @Test
    public void testGetOtpAuthUrlAndQrCode() {
        String secret = totpService.generateSecretKey();
        String url = totpService.getOtpAuthUrl("testuser", secret);
        assertTrue(url.contains("otpauth://totp/SwiftLink:testuser"));
        assertTrue(url.contains("secret=" + secret));

        String qrBase64 = totpService.generateQrCodeBase64(url);
        assertNotNull(qrBase64);
        assertTrue(qrBase64.startsWith("data:image/png;base64,"));
    }

    @Test
    public void testVerifyCodeWithInvalidInput() {
        String secret = totpService.generateSecretKey();
        assertFalse(totpService.verifyCode(secret, null));
        assertFalse(totpService.verifyCode(secret, "123"));
        assertFalse(totpService.verifyCode(secret, "abcdef"));
        assertFalse(totpService.verifyCode(null, "123456"));
    }
}
