package com.airesolver.exception;

public class JiraIntegrationException extends RuntimeException {
    public JiraIntegrationException(String message) {
        super(message);
    }
    public JiraIntegrationException(String message, Throwable cause) {
        super(message, cause);
    }
}
