package com.urlshortener.util;

import java.security.SecureRandom;

public class Base62Encoder {

    private static final String BASE62 = "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private static final int BASE = BASE62.length();
    private static final SecureRandom RANDOM = new SecureRandom();

    public static String encode(long value) {
        if (value == 0) {
            return String.valueOf(BASE62.charAt(0));
        }
        StringBuilder sb = new StringBuilder();
        while (value > 0) {
            sb.append(BASE62.charAt((int) (value % BASE)));
            value /= BASE;
        }
        return sb.reverse().toString();
    }

    public static String generateRandomCode(int length) {
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            sb.append(BASE62.charAt(RANDOM.nextInt(BASE)));
        }
        return sb.toString();
    }
}
