package com.airesolver.model.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record AnalysisRequest(
    @NotBlank String jiraTicketId,
    @NotNull Long configId
) {}
