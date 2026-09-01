package com.urlshortener.exception;

public class MaliciousUrlException extends RuntimeException {
    public MaliciousUrlException(String message) {
        super(message);
    }
}
