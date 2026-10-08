package com.airesolver.service;

import com.airesolver.model.dto.BitbucketFileDto;
import com.airesolver.model.dto.JiraTicketDto;
import com.airesolver.model.entity.FeedbackRating;
import com.airesolver.model.entity.Resolution;
import com.airesolver.repository.FeedbackRatingRepository;
import com.airesolver.repository.ResolutionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class PromptBuilderService {

    private final FeedbackRatingRepository feedbackRatingRepository;
    private final ResolutionRepository resolutionRepository;

    public String buildAnalysisPrompt(JiraTicketDto ticket, List<BitbucketFileDto> codeFiles, List<FeedbackRating> pastFeedback) {
        log.info("Building analysis prompt for ticket {}", ticket.key());
        StringBuilder prompt = new StringBuilder();

        // System instructions
        prompt.append("You are an expert software engineer analyzing a bug report. Provide your analysis in the following JSON format: { \"rootCause\": \"...\", \"suggestedFix\": \"...\", \"codeDiff\": \"...\", \"filesAffected\": \"...\", \"confidenceScore\": 0.0, \"severity\": \"...\" }\n\n");

        // Bug report section
        prompt.append("--- BUG REPORT ---\n");
        prompt.append("Ticket: ").append(ticket.key()).append("\n");
        prompt.append("Summary: ").append(ticket.summary()).append("\n");
        prompt.append("Description: ").append(ticket.description()).append("\n");
        if (ticket.comments() != null && !ticket.comments().isEmpty()) {
            prompt.append("Comments:\n");
            ticket.comments().forEach(c -> prompt.append("- ").append(c).append("\n"));
        }
        prompt.append("\n");

        // Code context section
        prompt.append("--- CODE CONTEXT ---\n");
        if (codeFiles != null && !codeFiles.isEmpty()) {
            for (BitbucketFileDto file : codeFiles) {
                prompt.append("File: ").append(file.filePath()).append("\n");
                prompt.append("```\n").append(file.content()).append("\n```\n\n");
            }
        } else {
            prompt.append("No relevant code files found.\n\n");
        }

        // Past feedback section
        List<FeedbackRating> goodFeedback = pastFeedback.stream().filter(f -> f.getRating() != null && f.getRating() >= 4).collect(Collectors.toList());
        List<FeedbackRating> badFeedback = pastFeedback.stream().filter(f -> f.getRating() != null && f.getRating() <= 2).collect(Collectors.toList());

        if (!goodFeedback.isEmpty()) {
            prompt.append("--- PAST SUCCESSFUL FIXES (EXAMPLES) ---\n");
            for (FeedbackRating f : goodFeedback) {
                Resolution res = f.getResolution();
                if (res != null) {
                    prompt.append("Bug: ").append(res.getAnalysis() != null ? res.getAnalysis().getTicketSummary() : "Unknown").append("\n");
                    prompt.append("Fix: ").append(res.getSuggestedFix()).append("\n");
                    prompt.append("Rating: ").append(f.getRating()).append("\n\n");
                }
            }
        }

        if (!badFeedback.isEmpty()) {
            prompt.append("--- PAST UNSUCCESSFUL FIXES (ANTI-PATTERNS TO AVOID) ---\n");
            for (FeedbackRating f : badFeedback) {
                Resolution res = f.getResolution();
                if (res != null) {
                    prompt.append("Bug: ").append(res.getAnalysis() != null ? res.getAnalysis().getTicketSummary() : "Unknown").append("\n");
                    prompt.append("Bad Fix: ").append(res.getSuggestedFix()).append("\n");
                    prompt.append("Rating: ").append(f.getRating()).append("\n\n");
                }
            }
        }

        // Output instructions
        prompt.append("--- OUTPUT INSTRUCTIONS ---\n");
        prompt.append("Provide ONLY valid JSON as requested above, with no extra text or markdown wrappers.");

        return prompt.toString();
    }

    public List<String> extractKeywords(JiraTicketDto ticket) {
        log.debug("Extracting keywords from ticket {}", ticket.key());
        Set<String> keywords = new HashSet<>();
        String combinedText = ticket.summary() + " " + ticket.description();
        
        // Simple extraction: words starting with uppercase, containing camelCase, or ending in Exception
        String[] words = combinedText.split("\\s+");
        for (String word : words) {
            String cleanWord = word.replaceAll("[^a-zA-Z0-9_.$]", "");
            if (cleanWord.length() > 3) {
                if (cleanWord.endsWith("Exception") || cleanWord.endsWith("Error") || 
                    cleanWord.contains(".") || cleanWord.matches(".*[A-Z].*")) {
                    keywords.add(cleanWord);
                }
            }
        }
        return new ArrayList<>(keywords);
    }

    public List<Resolution> getHighRatedResolutions() {
        return feedbackRatingRepository.findByRatingGreaterThanEqual(4).stream()
                .map(FeedbackRating::getResolution)
                .collect(Collectors.toList());
    }

    public List<Resolution> getPoorlyRatedResolutions() {
        // Find ratings <= 2
        return feedbackRatingRepository.findAll().stream()
                .filter(f -> f.getRating() != null && f.getRating() <= 2)
                .map(FeedbackRating::getResolution)
                .collect(Collectors.toList());
    }
}
