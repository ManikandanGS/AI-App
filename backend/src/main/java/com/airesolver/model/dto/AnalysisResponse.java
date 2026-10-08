package com.airesolver.model.dto;

import java.time.LocalDateTime;

public record AnalysisResponse(
    Long id,
    Long resolutionId,
    String jiraTicketId,
    String ticketSummary,
    String status,
    String severity,
    String rootCause,
    String suggestedFix,
    String codeDiff,
    String filesAffected,
    String aiModelUsed,
    Double confidenceScore,
    LocalDateTime createdAt
) {}
