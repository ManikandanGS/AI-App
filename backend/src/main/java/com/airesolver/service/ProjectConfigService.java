package com.airesolver.service;

import com.airesolver.exception.ResourceNotFoundException;
import com.airesolver.model.dto.ProjectConfigRequest;
import com.airesolver.model.dto.ProjectConfigResponse;
import com.airesolver.model.entity.ProjectConfig;
import com.airesolver.repository.ProjectConfigRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProjectConfigService {

    private final ProjectConfigRepository projectConfigRepository;

    public ProjectConfigResponse createConfig(ProjectConfigRequest request) {
        log.info("Creating project config for key: {}", request.projectKey());
        if (projectConfigRepository.findByProjectKey(request.projectKey()).isPresent()) {
            throw new IllegalArgumentException("Config with projectKey " + request.projectKey() + " already exists.");
        }

        ProjectConfig config = new ProjectConfig();
        updateEntityFromRequest(config, request);
        config.setCreatedAt(LocalDateTime.now());
        config.setUpdatedAt(LocalDateTime.now());

        config = projectConfigRepository.save(config);
        return toResponse(config);
    }

    public ProjectConfigResponse updateConfig(Long id, ProjectConfigRequest request) {
        log.info("Updating project config with ID: {}", id);
        ProjectConfig config = projectConfigRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Config not found with ID: " + id));

        updateEntityFromRequest(config, request);
        config.setUpdatedAt(LocalDateTime.now());

        config = projectConfigRepository.save(config);
        return toResponse(config);
    }

    public ProjectConfigResponse getConfig(Long id) {
        log.info("Fetching project config with ID: {}", id);
        ProjectConfig config = projectConfigRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Config not found with ID: " + id));
        return toResponse(config);
    }

    public List<ProjectConfigResponse> getAllConfigs() {
        log.info("Fetching all project configs");
        return projectConfigRepository.findAll().stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public void deleteConfig(Long id) {
        log.info("Deleting project config with ID: {}", id);
        if (!projectConfigRepository.existsById(id)) {
            throw new ResourceNotFoundException("Config not found with ID: " + id);
        }
        projectConfigRepository.deleteById(id);
    }

    private void updateEntityFromRequest(ProjectConfig config, ProjectConfigRequest request) {
        config.setProjectKey(request.projectKey());
        config.setJiraBaseUrl(request.jiraBaseUrl());
        config.setJiraUsername(request.jiraUsername());
        config.setJiraApiToken(request.jiraApiToken());
        config.setBitbucketBaseUrl(request.bitbucketBaseUrl());
        config.setBitbucketUsername(request.bitbucketUsername());
        config.setBitbucketAppPassword(request.bitbucketAppPassword());
        config.setBitbucketWorkspace(request.bitbucketWorkspace());
        config.setBitbucketRepoSlug(request.bitbucketRepoSlug());
        config.setDbUrl(request.dbUrl());
        config.setDbUsername(request.dbUsername());
        config.setDbPasswordEncrypted(request.dbPassword()); // Simple mapping for now
    }

    private ProjectConfigResponse toResponse(ProjectConfig entity) {
        return new ProjectConfigResponse(
                entity.getId(),
                entity.getProjectKey(),
                entity.getJiraBaseUrl(),
                entity.getBitbucketBaseUrl(),
                entity.getBitbucketWorkspace(),
                entity.getBitbucketRepoSlug(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }
}
