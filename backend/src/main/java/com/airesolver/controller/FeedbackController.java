package com.airesolver.controller;

import com.airesolver.model.dto.FeedbackRequest;
import com.airesolver.model.dto.FeedbackResponse;
import com.airesolver.service.FeedbackService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/v1/feedback")
@Tag(name = "Feedback")
@RequiredArgsConstructor
@Slf4j
public class FeedbackController {

    private final FeedbackService feedbackService;

    @PostMapping("/")
    @ResponseStatus(HttpStatus.CREATED)
    public FeedbackResponse submitFeedback(@RequestBody @Valid FeedbackRequest request) {
        log.info("Received request to submit feedback for resolution ID: {}", request.resolutionId());
        return feedbackService.submitFeedback(request);
    }

    @GetMapping("/resolution/{resolutionId}")
    public List<FeedbackResponse> getFeedbackForResolution(@PathVariable Long resolutionId) {
        log.info("Received request to get feedback for resolution ID: {}", resolutionId);
        return feedbackService.getFeedbackForResolution(resolutionId);
    }

    @GetMapping("/stats")
    public Map<String, Object> getFeedbackStats() {
        log.info("Received request to get feedback stats");
        return feedbackService.getFeedbackStats();
    }
}
