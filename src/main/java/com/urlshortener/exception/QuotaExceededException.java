package com.urlshortener.exception;

/** A workspace or account hit a plan limit (members, links or number of workspaces). */
public class QuotaExceededException extends RuntimeException {
    public QuotaExceededException(String message) {
        super(message);
    }
}
