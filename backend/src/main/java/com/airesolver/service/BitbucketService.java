package com.airesolver.service;

import com.airesolver.exception.BitbucketIntegrationException;
import com.airesolver.model.dto.BitbucketFileDto;
import com.airesolver.model.entity.ProjectConfig;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class BitbucketService {

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    @Value("${bitbucket.base-url:https://api.bitbucket.org/2.0}")
    private String defaultBaseUrl;

    @Value("${bitbucket.username:}")
    private String defaultUsername;

    @Value("${bitbucket.app-password:}")
    private String defaultAppPassword;

    public BitbucketFileDto fetchFileContent(String workspace, String repoSlug, String filePath, String branch, ProjectConfig config) {
        log.info("Fetching file content for {} in {}/{}", filePath, workspace, repoSlug);
        String baseUrl = resolveBaseUrl(config);
        String url = String.format("%s/repositories/%s/%s/src/%s/%s", baseUrl, workspace, repoSlug, branch, filePath);
        
        HttpHeaders headers = createHeaders(config);
        HttpEntity<String> entity = new HttpEntity<>(headers);

        try {
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);
            return new BitbucketFileDto(filePath, response.getBody(), determineLanguage(filePath), null);
        } catch (Exception e) {
            log.error("Error fetching file content from Bitbucket: {}", filePath, e);
            throw new BitbucketIntegrationException("Error fetching file content from Bitbucket: " + e.getMessage());
        }
    }

    public List<String> fetchRepoTree(String workspace, String repoSlug, String branch, ProjectConfig config) {
        log.info("Fetching repo tree for {}/{} on branch {}", workspace, repoSlug, branch);
        String baseUrl = resolveBaseUrl(config);
        String url = String.format("%s/repositories/%s/%s/src/%s/?pagelen=100", baseUrl, workspace, repoSlug, branch);
        
        HttpHeaders headers = createHeaders(config);
        HttpEntity<String> entity = new HttpEntity<>(headers);
        List<String> filePaths = new ArrayList<>();

        try {
            // Simplified: just fetching one page for now
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);
            JsonNode root = objectMapper.readTree(response.getBody());
            
            root.path("values").forEach(node -> {
                if ("commit_file".equals(node.path("type").asText())) {
                    filePaths.add(node.path("path").asText());
                }
            });
            return filePaths;
        } catch (Exception e) {
            log.error("Error fetching repo tree from Bitbucket", e);
            throw new BitbucketIntegrationException("Error fetching repo tree from Bitbucket: " + e.getMessage());
        }
    }

    public List<BitbucketFileDto> searchCodeInRepo(String workspace, String repoSlug, String query, ProjectConfig config) {
        // Bitbucket Server/Cloud search API could be used here. For simplicity we'll mock or leave basic implementation.
        log.info("Searching code in repo for query: {}", query);
        return new ArrayList<>(); // Placeholder for actual code search implementation
    }

    public List<BitbucketFileDto> fetchRelevantFiles(String workspace, String repoSlug, List<String> keywords, ProjectConfig config) {
        log.info("Fetching relevant files for keywords: {}", keywords);
        List<BitbucketFileDto> relevantFiles = new ArrayList<>();
        try {
            // In a real scenario, this would use Bitbucket search API or iterate over the tree
            // Since Bitbucket Cloud code search is complex, we will just simulate getting some files
            // For now, let's fetch the tree and attempt basic string matching on paths, or use search api if available
            List<String> allFiles = fetchRepoTree(workspace, repoSlug, "main", config);
            int count = 0;
            for (String file : allFiles) {
                if (count >= 10) break;
                boolean matches = keywords.stream().anyMatch(kw -> file.toLowerCase().contains(kw.toLowerCase()));
                if (matches) {
                    BitbucketFileDto fileDto = fetchFileContent(workspace, repoSlug, file, "main", config);
                    // limit to first 500 lines
                    String content = fileDto.content();
                    String[] lines = content.split("\n");
                    if (lines.length > 500) {
                        StringBuilder sb = new StringBuilder();
                        for (int i = 0; i < 500; i++) {
                            sb.append(lines[i]).append("\n");
                        }
                        content = sb.toString();
                    }
                    relevantFiles.add(new BitbucketFileDto(fileDto.filePath(), content, fileDto.language(), fileDto.lastModified()));
                    count++;
                }
            }
        } catch (Exception e) {
            log.warn("Could not fetch relevant files: {}", e.getMessage());
        }
        return relevantFiles;
    }

    private HttpHeaders createHeaders(ProjectConfig config) {
        String username = config != null && StringUtils.hasText(config.getBitbucketUsername()) ? config.getBitbucketUsername() : defaultUsername;
        String appPassword = config != null && StringUtils.hasText(config.getBitbucketAppPassword()) ? config.getBitbucketAppPassword() : defaultAppPassword;
        
        String auth = username + ":" + appPassword;
        byte[] encodedAuth = Base64.getEncoder().encode(auth.getBytes(StandardCharsets.US_ASCII));
        String authHeader = "Basic " + new String(encodedAuth);
        
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", authHeader);
        headers.set("Accept", MediaType.APPLICATION_JSON_VALUE);
        return headers;
    }

    private String resolveBaseUrl(ProjectConfig config) {
        String baseUrl = config != null && StringUtils.hasText(config.getBitbucketBaseUrl()) ? config.getBitbucketBaseUrl() : defaultBaseUrl;
        return baseUrl.endsWith("/") ? baseUrl.substring(0, baseUrl.length() - 1) : baseUrl;
    }
    
    private String determineLanguage(String filePath) {
        if (filePath.endsWith(".java")) return "Java";
        if (filePath.endsWith(".js")) return "JavaScript";
        if (filePath.endsWith(".ts")) return "TypeScript";
        if (filePath.endsWith(".py")) return "Python";
        return "Unknown";
    }
}
