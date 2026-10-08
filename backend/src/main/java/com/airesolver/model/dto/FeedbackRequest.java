package com.airesolver.model.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record FeedbackRequest(
    @NotNull Long resolutionId,
    @NotNull @Min(1) @Max(5) Integer rating,
    String feedbackText,
    Boolean wasFixApplied
) {}
