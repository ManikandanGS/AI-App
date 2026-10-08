package com.airesolver.model.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "project_config")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProjectConfig {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Column(unique = true, nullable = false)
    private String projectKey;

    @NotBlank
    @Column(nullable = false)
    private String jiraBaseUrl;

    private String jiraUsername;

    private String jiraApiToken;

    private String bitbucketBaseUrl;

    private String bitbucketUsername;

    private String bitbucketAppPassword;

    private String bitbucketWorkspace;

    private String bitbucketRepoSlug;

    private String dbUrl;

    private String dbUsername;

    private String dbPasswordEncrypted;

    @Column(updatable = false)
    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
