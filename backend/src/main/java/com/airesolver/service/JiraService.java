package com.airesolver.service;

import com.airesolver.exception.JiraIntegrationException;
import com.airesolver.model.dto.JiraTicketDto;
import com.airesolver.model.entity.ProjectConfig;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class JiraService {

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    @Value("${jira.base-url:}")
    private String defaultBaseUrl;

    @Value("${jira.username:}")
    private String defaultUsername;

    @Value("${jira.api-token:}")
    private String defaultApiToken;

    public JiraTicketDto getFullTicketInfo(String ticketId, ProjectConfig config) {
        log.info("Fetching full ticket info for ticket: {}", ticketId);
        try {
            JiraTicketDto ticket = fetchTicket(ticketId, config);
            List<String> comments = fetchTicketComments(ticketId, config);
            return new JiraTicketDto(
                    ticket.key(),
                    ticket.summary(),
                    ticket.description(),
                    ticket.status(),
                    ticket.priority(),
                    ticket.issueType(),
                    ticket.labels(),
                    ticket.components(),
                    comments
            );
        } catch (Exception e) {
            log.error("Failed to fetch full ticket info for {}", ticketId, e);
            throw new JiraIntegrationException("Failed to fetch full ticket info for " + ticketId + ": " + e.getMessage());
        }
    }

    private JiraTicketDto fetchTicket(String ticketId, ProjectConfig config) {
        String baseUrl = resolveBaseUrl(config);
        String url = baseUrl + "/rest/api/3/issue/" + ticketId;
        
        HttpHeaders headers = createHeaders(config);
        HttpEntity<String> entity = new HttpEntity<>(headers);

        try {
            log.debug("Calling Jira API to fetch ticket: {}", url);
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);
            JsonNode root = objectMapper.readTree(response.getBody());
            
            String key = root.path("key").asText();
            JsonNode fields = root.path("fields");
            String summary = fields.path("summary").asText();
            String description = extractAdfText(fields.path("description"));
            String status = fields.path("status").path("name").asText();
            String priority = fields.path("priority").path("name").asText();
            String issueType = fields.path("issuetype").path("name").asText();
            
            List<String> labels = new ArrayList<>();
            fields.path("labels").forEach(node -> labels.add(node.asText()));
            
            List<String> components = new ArrayList<>();
            fields.path("components").forEach(node -> components.add(node.path("name").asText()));
            
            return new JiraTicketDto(key, summary, description, status, priority, issueType, labels, components, new ArrayList<>());
        } catch (Exception e) {
            log.error("Error fetching ticket from Jira: {}", ticketId, e);
            throw new JiraIntegrationException("Error fetching ticket from Jira: " + e.getMessage());
        }
    }

    private List<String> fetchTicketComments(String ticketId, ProjectConfig config) {
        String baseUrl = resolveBaseUrl(config);
        String url = baseUrl + "/rest/api/3/issue/" + ticketId + "/comment";
        
        HttpHeaders headers = createHeaders(config);
        HttpEntity<String> entity = new HttpEntity<>(headers);

        try {
            log.debug("Calling Jira API to fetch comments for ticket: {}", url);
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);
            JsonNode root = objectMapper.readTree(response.getBody());
            
            List<String> comments = new ArrayList<>();
            root.path("comments").forEach(commentNode -> {
                String commentText = extractAdfText(commentNode.path("body"));
                if (StringUtils.hasText(commentText)) {
                    comments.add(commentText);
                }
            });
            return comments;
        } catch (Exception e) {
            log.error("Error fetching comments from Jira: {}", ticketId, e);
            throw new JiraIntegrationException("Error fetching comments from Jira: " + e.getMessage());
        }
    }

    private String extractAdfText(JsonNode node) {
        if (node == null || node.isMissingNode()) {
            return "";
        }
        if (node.has("type") && "text".equals(node.get("type").asText()) && node.has("text")) {
            return node.get("text").asText();
        }
        
        StringBuilder sb = new StringBuilder();
        if (node.has("content")) {
            for (JsonNode child : node.get("content")) {
                String text = extractAdfText(child);
                if (StringUtils.hasText(text)) {
                    sb.append(text).append(" ");
                }
            }
        }
        return sb.toString().trim();
    }

    private HttpHeaders createHeaders(ProjectConfig config) {
        String username = config != null && StringUtils.hasText(config.getJiraUsername()) ? config.getJiraUsername() : defaultUsername;
        String apiToken = config != null && StringUtils.hasText(config.getJiraApiToken()) ? config.getJiraApiToken() : defaultApiToken;
        
        String auth = username + ":" + apiToken;
        byte[] encodedAuth = Base64.getEncoder().encode(auth.getBytes(StandardCharsets.US_ASCII));
        String authHeader = "Basic " + new String(encodedAuth);
        
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", authHeader);
        headers.set("Accept", MediaType.APPLICATION_JSON_VALUE);
        return headers;
    }

    private String resolveBaseUrl(ProjectConfig config) {
        String baseUrl = config != null && StringUtils.hasText(config.getJiraBaseUrl()) ? config.getJiraBaseUrl() : defaultBaseUrl;
        if (!StringUtils.hasText(baseUrl)) {
            throw new JiraIntegrationException("Jira base URL is not configured.");
        }
        return baseUrl.endsWith("/") ? baseUrl.substring(0, baseUrl.length() - 1) : baseUrl;
    }
}
