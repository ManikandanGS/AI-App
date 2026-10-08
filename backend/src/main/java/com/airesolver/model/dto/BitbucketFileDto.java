package com.airesolver.model.dto;

import java.time.LocalDateTime;

public record BitbucketFileDto(
    String filePath,
    String content,
    String language,
    LocalDateTime lastModified
) {}
