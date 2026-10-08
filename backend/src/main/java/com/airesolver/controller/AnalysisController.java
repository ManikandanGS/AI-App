package com.airesolver.controller;

import com.airesolver.exception.ResourceNotFoundException;
import com.airesolver.model.dto.AnalysisRequest;
import com.airesolver.model.dto.AnalysisResponse;
import com.airesolver.model.entity.BugAnalysis;
import com.airesolver.model.entity.ProjectConfig;
import com.airesolver.model.entity.Resolution;
import com.airesolver.repository.BugAnalysisRepository;
import com.airesolver.repository.ProjectConfigRepository;
import com.airesolver.repository.ResolutionRepository;
import com.airesolver.service.AiAnalysisService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/v1/analyses")
@Tag(name = "Bug Analysis")
@RequiredArgsConstructor
@Slf4j
public class AnalysisController {

    private final AiAnalysisService aiAnalysisService;
    private final BugAnalysisRepository bugAnalysisRepository;
    private final ResolutionRepository resolutionRepository;
    private final ProjectConfigRepository projectConfigRepository;

    @PostMapping("/")
    @ResponseStatus(HttpStatus.CREATED)
    public AnalysisResponse analyzeTicket(@RequestBody @Valid AnalysisRequest request) {
        log.info("Received request to analyze ticket: {}", request.jiraTicketId());
        ProjectConfig config = null;
        if (request.configId() != null) {
            config = projectConfigRepository.findById(request.configId())
                    .orElseThrow(() -> new ResourceNotFoundException("Config not found"));
        }
        return aiAnalysisService.analyzeTicket(request.jiraTicketId(), config);
    }

    @GetMapping("/")
    public List<AnalysisResponse> getAllAnalyses() {
        log.info("Fetching all analyses");
        return bugAnalysisRepository.findAllByOrderByCreatedAtDesc().stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @GetMapping("/{id}")
    public AnalysisResponse getAnalysis(@PathVariable Long id) {
        log.info("Fetching analysis by ID: {}", id);
        BugAnalysis analysis = bugAnalysisRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Analysis not found with ID: " + id));
        return toResponse(analysis);
    }

    private AnalysisResponse toResponse(BugAnalysis analysis) {
        Resolution res = resolutionRepository.findByAnalysisId(analysis.getId()).stream().findFirst().orElse(null);
        return new AnalysisResponse(
                analysis.getId(),
                res != null ? res.getId() : null,
                analysis.getJiraTicketId(),
                analysis.getTicketSummary(),
                analysis.getStatus().name(),
                analysis.getSeverity() != null ? analysis.getSeverity().name() : "UNKNOWN",
                res != null ? res.getRootCause() : null,
                res != null ? res.getSuggestedFix() : null,
                res != null ? res.getCodeDiff() : null,
                res != null ? res.getFilesAffected() : null,
                res != null ? res.getAiModelUsed() : null,
                res != null ? res.getConfidenceScore() : null,
                analysis.getCreatedAt()
        );
    }
}
