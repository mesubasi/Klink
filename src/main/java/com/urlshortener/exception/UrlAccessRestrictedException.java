package com.urlshortener.exception;

public class UrlAccessRestrictedException extends RuntimeException {
    public UrlAccessRestrictedException(String message) {
        super(message);
    }
}
