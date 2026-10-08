package com.airesolver.model.dto;

import jakarta.validation.constraints.NotBlank;

public record ProjectConfigRequest(
    @NotBlank String projectKey,
    @NotBlank String jiraBaseUrl,
    String jiraUsername,
    String jiraApiToken,
    String bitbucketBaseUrl,
    String bitbucketUsername,
    String bitbucketAppPassword,
    String bitbucketWorkspace,
    String bitbucketRepoSlug,
    String dbUrl,
    String dbUsername,
    String dbPassword
) {}
