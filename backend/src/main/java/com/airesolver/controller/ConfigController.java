package com.airesolver.controller;

import com.airesolver.model.dto.ProjectConfigRequest;
import com.airesolver.model.dto.ProjectConfigResponse;
import com.airesolver.service.ProjectConfigService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/v1/config")
@Tag(name = "Configuration")
@RequiredArgsConstructor
@Slf4j
public class ConfigController {

    private final ProjectConfigService projectConfigService;

    @PostMapping("/")
    @ResponseStatus(HttpStatus.CREATED)
    public ProjectConfigResponse createConfig(@RequestBody @Valid ProjectConfigRequest request) {
        log.info("Received request to create config");
        return projectConfigService.createConfig(request);
    }

    @PutMapping("/{id}")
    public ProjectConfigResponse updateConfig(@PathVariable Long id, @RequestBody @Valid ProjectConfigRequest request) {
        log.info("Received request to update config ID: {}", id);
        return projectConfigService.updateConfig(id, request);
    }

    @GetMapping("/")
    public List<ProjectConfigResponse> getAllConfigs() {
        log.info("Received request to get all configs");
        return projectConfigService.getAllConfigs();
    }

    @GetMapping("/{id}")
    public ProjectConfigResponse getConfig(@PathVariable Long id) {
        log.info("Received request to get config ID: {}", id);
        return projectConfigService.getConfig(id);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteConfig(@PathVariable Long id) {
        log.info("Received request to delete config ID: {}", id);
        projectConfigService.deleteConfig(id);
    }
}
