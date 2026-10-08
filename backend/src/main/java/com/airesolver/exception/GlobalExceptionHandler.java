package com.airesolver.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErrorResponse handleResourceNotFoundException(ResourceNotFoundException ex) {
        return new ErrorResponse(LocalDateTime.now(), HttpStatus.NOT_FOUND.value(), ex.getMessage(), "Resource not found");
    }

    @ExceptionHandler({JiraIntegrationException.class, BitbucketIntegrationException.class})
    @ResponseStatus(HttpStatus.BAD_GATEWAY)
    public ErrorResponse handleIntegrationException(RuntimeException ex) {
        return new ErrorResponse(LocalDateTime.now(), HttpStatus.BAD_GATEWAY.value(), ex.getMessage(), "Third-party integration error");
    }

    @ExceptionHandler(AiServiceException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ErrorResponse handleAiServiceException(AiServiceException ex) {
        return new ErrorResponse(LocalDateTime.now(), HttpStatus.INTERNAL_SERVER_ERROR.value(), ex.getMessage(), "AI service error");
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleValidationException(MethodArgumentNotValidException ex) {
        return new ErrorResponse(LocalDateTime.now(), HttpStatus.BAD_REQUEST.value(), "Validation Error", ex.getBindingResult().toString());
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ErrorResponse handleGenericException(Exception ex) {
        return new ErrorResponse(LocalDateTime.now(), HttpStatus.INTERNAL_SERVER_ERROR.value(), "An unexpected error occurred", ex.getMessage());
    }
}
