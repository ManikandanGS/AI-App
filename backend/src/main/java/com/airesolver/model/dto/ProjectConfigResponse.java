package com.airesolver.model.dto;

import java.time.LocalDateTime;

public record ProjectConfigResponse(
    Long id,
    String projectKey,
    String jiraBaseUrl,
    String bitbucketBaseUrl,
    String bitbucketWorkspace,
    String bitbucketRepoSlug,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {}
