package com.airesolver.model.dto;

import java.util.List;

public record JiraTicketDto(
    String key,
    String summary,
    String description,
    String status,
    String priority,
    String issueType,
    List<String> labels,
    List<String> components,
    List<String> comments
) {}
