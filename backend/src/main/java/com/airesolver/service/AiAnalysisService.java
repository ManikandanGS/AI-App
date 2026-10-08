package com.airesolver.service;

import com.airesolver.exception.AiServiceException;
import com.airesolver.model.dto.*;
import com.airesolver.model.entity.*;
import com.airesolver.model.enums.AnalysisStatus;
import com.airesolver.model.enums.Severity;
import com.airesolver.repository.BugAnalysisRepository;
import com.airesolver.repository.FeedbackRatingRepository;
import com.airesolver.repository.ResolutionRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class AiAnalysisService {

    private final RestTemplate restTemplate;
    private final PromptBuilderService promptBuilderService;
    private final JiraService jiraService;
    private final BitbucketService bitbucketService;
    private final BugAnalysisRepository bugAnalysisRepository;
    private final ResolutionRepository resolutionRepository;
    private final FeedbackRatingRepository feedbackRatingRepository;
    private final ObjectMapper objectMapper;

    @Value("${ai.provider:openai}")
    private String aiProvider;

    @Value("${ai.api-key:}")
    private String aiApiKey;

    @Value("${ai.model:gpt-4}")
    private String aiModel;

    @Value("${ai.max-tokens:2000}")
    private int aiMaxTokens;

    @Value("${ai.temperature:0.2}")
    private double aiTemperature;

    public AnalysisResponse analyzeTicket(String jiraTicketId, ProjectConfig config) {
        log.info("Starting analysis for ticket: {}", jiraTicketId);
        
        BugAnalysis analysis = new BugAnalysis();
        analysis.setJiraTicketId(jiraTicketId);
        analysis.setStatus(AnalysisStatus.PENDING);
        analysis.setConfig(config);
        analysis.setCreatedAt(LocalDateTime.now());
        analysis = bugAnalysisRepository.save(analysis);

        try {
            analysis.setStatus(AnalysisStatus.IN_PROGRESS);
            bugAnalysisRepository.save(analysis);

            JiraTicketDto ticket = jiraService.getFullTicketInfo(jiraTicketId, config);
            analysis.setTicketSummary(ticket.summary());
            analysis.setTicketDescription(ticket.description());
            analysis.setFullContext(ticket.toString());
            bugAnalysisRepository.save(analysis);

            List<String> keywords = promptBuilderService.extractKeywords(ticket);
            
            String workspace = config != null ? config.getBitbucketWorkspace() : "default-workspace";
            String repoSlug = config != null ? config.getBitbucketRepoSlug() : "default-repo";
            
            List<BitbucketFileDto> relevantFiles = bitbucketService.fetchRelevantFiles(workspace, repoSlug, keywords, config);
            
            List<FeedbackRating> pastFeedback = feedbackRatingRepository.findAll();
            
            String prompt = promptBuilderService.buildAnalysisPrompt(ticket, relevantFiles, pastFeedback);
            
            String aiResponseStr = callAiModel(prompt);
            
            Resolution resolution = parseAiResponse(aiResponseStr);
            resolution.setAnalysis(analysis);
            resolution.setAiModelUsed(aiModel);
            resolution.setCreatedAt(LocalDateTime.now());
            resolution = resolutionRepository.save(resolution);
            
            try {
                analysis.setSeverity(Severity.valueOf(resolution.getSeverity() != null ? resolution.getSeverity().toUpperCase() : "UNKNOWN"));
            } catch (Exception e) {
                analysis.setSeverity(Severity.UNKNOWN);
            }
            
            analysis.setStatus(AnalysisStatus.COMPLETED);
            analysis = bugAnalysisRepository.save(analysis);
            
            return buildAnalysisResponse(analysis, resolution);
            
        } catch (Exception e) {
            log.error("Analysis failed for ticket: {}", jiraTicketId, e);
            analysis.setStatus(AnalysisStatus.FAILED);
            bugAnalysisRepository.save(analysis);
            throw new AiServiceException("Analysis failed: " + e.getMessage());
        }
    }

    private String callAiModel(String prompt) {
        log.info("Calling AI Model: {}", aiModel);
        if (!"openai".equalsIgnoreCase(aiProvider)) {
            throw new AiServiceException("Unsupported AI provider: " + aiProvider);
        }

        String url = "https://api.openai.com/v1/chat/completions";
        
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + aiApiKey);
        headers.set("Content-Type", "application/json");
        
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", aiModel);
        requestBody.put("max_tokens", aiMaxTokens);
        requestBody.put("temperature", aiTemperature);
        
        Map<String, String> systemMessage = new HashMap<>();
        systemMessage.put("role", "system");
        systemMessage.put("content", "You are an AI assistant analyzing bugs.");
        
        Map<String, String> userMessage = new HashMap<>();
        userMessage.put("role", "user");
        userMessage.put("content", prompt);
        
        requestBody.put("messages", List.of(systemMessage, userMessage));

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

        try {
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, entity, String.class);
            JsonNode root = objectMapper.readTree(response.getBody());
            return root.path("choices").get(0).path("message").path("content").asText();
        } catch (Exception e) {
            log.error("Error calling AI model", e);
            throw new AiServiceException("Error calling AI model: " + e.getMessage());
        }
    }

    private Resolution parseAiResponse(String jsonResponse) {
        try {
            String cleanJson = jsonResponse.trim();
            if (cleanJson.startsWith("```json")) {
                cleanJson = cleanJson.substring(7);
            }
            if (cleanJson.startsWith("```")) {
                cleanJson = cleanJson.substring(3);
            }
            if (cleanJson.endsWith("```")) {
                cleanJson = cleanJson.substring(0, cleanJson.length() - 3);
            }
            cleanJson = cleanJson.trim();

            JsonNode node = objectMapper.readTree(cleanJson);
            Resolution res = new Resolution();
            res.setRootCause(node.path("rootCause").asText());
            res.setSuggestedFix(node.path("suggestedFix").asText());
            res.setCodeDiff(node.path("codeDiff").asText());
            res.setFilesAffected(node.path("filesAffected").asText());
            res.setConfidenceScore(node.path("confidenceScore").asDouble(0.0));
            // Setting a transient severity field if needed, or we just extract it for analysis
            String severity = node.path("severity").asText();
            res.setSeverity(severity); // Add transient setter or similar if not in entity
            return res;
        } catch (Exception e) {
            log.error("Failed to parse AI response: {}", jsonResponse, e);
            throw new AiServiceException("Failed to parse AI response: " + e.getMessage());
        }
    }

    private AnalysisResponse buildAnalysisResponse(BugAnalysis analysis, Resolution resolution) {
        return new AnalysisResponse(
                analysis.getId(),
                resolution.getId(),
                analysis.getJiraTicketId(),
                analysis.getTicketSummary(),
                analysis.getStatus().name(),
                analysis.getSeverity() != null ? analysis.getSeverity().name() : "UNKNOWN",
                resolution.getRootCause(),
                resolution.getSuggestedFix(),
                resolution.getCodeDiff(),
                resolution.getFilesAffected(),
                resolution.getAiModelUsed(),
                resolution.getConfidenceScore(),
                analysis.getCreatedAt()
        );
    }
}
