package com.airesolver.exception;

public class BitbucketIntegrationException extends RuntimeException {
    public BitbucketIntegrationException(String message) {
        super(message);
    }
    public BitbucketIntegrationException(String message, Throwable cause) {
        super(message, cause);
    }
}
