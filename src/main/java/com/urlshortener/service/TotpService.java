package com.urlshortener.service;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.ByteArrayOutputStream;
import java.security.SecureRandom;
import java.util.Base64;

@Service
public class TotpService {

    private static final String BASE32_CHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ234567";
    private static final int SECRET_SIZE = 20; // 160 bits
    private static final int TIME_STEP_SECONDS = 30;
    private static final int CODE_DIGITS = 6;
    private static final int WINDOW_SIZE = 1; // ±30s clock skew tolerance

    private final SecureRandom random = new SecureRandom();

    public String generateSecretKey() {
        byte[] bytes = new byte[SECRET_SIZE];
        random.nextBytes(bytes);
        return encodeBase32(bytes);
    }

    public String getOtpAuthUrl(String username, String secretKey) {
        return String.format("otpauth://totp/Klink:%s?secret=%s&issuer=Klink", username, secretKey);
    }

    public String generateQrCodeBase64(String otpAuthUrl) {
        try {
            QRCodeWriter qrCodeWriter = new QRCodeWriter();
            BitMatrix bitMatrix = qrCodeWriter.encode(otpAuthUrl, BarcodeFormat.QR_CODE, 250, 250);
            ByteArrayOutputStream pngOutputStream = new ByteArrayOutputStream();
            MatrixToImageWriter.writeToStream(bitMatrix, "PNG", pngOutputStream);
            byte[] pngData = pngOutputStream.toByteArray();
            return "data:image/png;base64," + Base64.getEncoder().encodeToString(pngData);
        } catch (Exception e) {
            throw new RuntimeException("QR Kod üretilirken hata oluştu: " + e.getMessage(), e);
        }
    }

    public boolean verifyCode(String secretKey, String code) {
        if (secretKey == null || secretKey.trim().isEmpty() || code == null || code.trim().isEmpty()) {
            return false;
        }

        String sanitizedCode = code.replaceAll("\\s+", "").trim();
        if (!sanitizedCode.matches("\\d{6}")) {
            return false;
        }

        long currentStep = System.currentTimeMillis() / 1000 / TIME_STEP_SECONDS;

        for (int i = -WINDOW_SIZE; i <= WINDOW_SIZE; i++) {
            String calculatedCode = generateTotp(secretKey, currentStep + i);
            if (sanitizedCode.equals(calculatedCode)) {
                return true;
            }
        }
        return false;
    }

    private String generateTotp(String secretKey, long timeStep) {
        try {
            byte[] key = decodeBase32(secretKey);
            byte[] data = new byte[8];
            for (int i = 7; i >= 0; i--) {
                data[i] = (byte) (timeStep & 0xFF);
                timeStep >>= 8;
            }

            SecretKeySpec signKey = new SecretKeySpec(key, "HmacSHA1");
            Mac mac = Mac.getInstance("HmacSHA1");
            mac.init(signKey);
            byte[] hash = mac.doFinal(data);

            int offset = hash[hash.length - 1] & 0xF;
            int binary = ((hash[offset] & 0x7F) << 24)
                    | ((hash[offset + 1] & 0xFF) << 16)
                    | ((hash[offset + 2] & 0xFF) << 8)
                    | (hash[offset + 3] & 0xFF);

            int otp = binary % (int) Math.pow(10, CODE_DIGITS);
            return String.format("%06d", otp);
        } catch (Exception e) {
            return null;
        }
    }

    private String encodeBase32(byte[] data) {
        StringBuilder result = new StringBuilder();
        int buffer = 0;
        int bitsLeft = 0;

        for (byte b : data) {
            buffer = (buffer << 8) | (b & 0xFF);
            bitsLeft += 8;
            while (bitsLeft >= 5) {
                int index = (buffer >> (bitsLeft - 5)) & 0x1F;
                result.append(BASE32_CHARS.charAt(index));
                bitsLeft -= 5;
            }
        }

        if (bitsLeft > 0) {
            int index = (buffer << (5 - bitsLeft)) & 0x1F;
            result.append(BASE32_CHARS.charAt(index));
        }

        return result.toString();
    }

    private byte[] decodeBase32(String secret) {
        String sanitized = secret.trim().toUpperCase().replaceAll("[= ]", "");
        byte[] bytes = new byte[sanitized.length() * 5 / 8];
        int buffer = 0;
        int bitsLeft = 0;
        int count = 0;

        for (int i = 0; i < sanitized.length(); i++) {
            char c = sanitized.charAt(i);
            int val = BASE32_CHARS.indexOf(c);
            if (val < 0) {
                continue;
            }
            buffer = (buffer << 5) | val;
            bitsLeft += 5;
            if (bitsLeft >= 8) {
                bytes[count++] = (byte) ((buffer >> (bitsLeft - 8)) & 0xFF);
                bitsLeft -= 8;
            }
        }
        return bytes;
    }
}
