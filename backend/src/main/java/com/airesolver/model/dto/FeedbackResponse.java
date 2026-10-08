package com.airesolver.model.dto;

import java.time.LocalDateTime;

public record FeedbackResponse(
    Long id,
    Long resolutionId,
    Integer rating,
    String feedbackText,
    Boolean wasFixApplied,
    LocalDateTime createdAt
) {}
