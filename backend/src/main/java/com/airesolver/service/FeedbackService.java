package com.airesolver.service;

import com.airesolver.exception.ResourceNotFoundException;
import com.airesolver.model.dto.FeedbackRequest;
import com.airesolver.model.dto.FeedbackResponse;
import com.airesolver.model.entity.FeedbackRating;
import com.airesolver.model.entity.Resolution;
import com.airesolver.repository.FeedbackRatingRepository;
import com.airesolver.repository.ResolutionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class FeedbackService {

    private final FeedbackRatingRepository feedbackRatingRepository;
    private final ResolutionRepository resolutionRepository;

    public FeedbackResponse submitFeedback(FeedbackRequest request) {
        log.info("Submitting feedback for resolution ID: {}", request.resolutionId());
        Resolution resolution = resolutionRepository.findById(request.resolutionId())
                .orElseThrow(() -> new ResourceNotFoundException("Resolution not found with ID: " + request.resolutionId()));

        FeedbackRating feedback = new FeedbackRating();
        feedback.setResolution(resolution);
        feedback.setRating(request.rating());
        feedback.setFeedbackText(request.feedbackText());
        feedback.setWasFixApplied(request.wasFixApplied());
        feedback.setCreatedAt(LocalDateTime.now());

        feedback = feedbackRatingRepository.save(feedback);

        return new FeedbackResponse(
                feedback.getId(),
                resolution.getId(),
                feedback.getRating(),
                feedback.getFeedbackText(),
                feedback.getWasFixApplied(),
                feedback.getCreatedAt()
        );
    }

    public List<FeedbackResponse> getFeedbackForResolution(Long resolutionId) {
        log.info("Fetching feedback for resolution ID: {}", resolutionId);
        return feedbackRatingRepository.findByResolutionId(resolutionId).stream()
                .map(f -> new FeedbackResponse(
                        f.getId(),
                        f.getResolution().getId(),
                        f.getRating(),
                        f.getFeedbackText(),
                        f.getWasFixApplied(),
                        f.getCreatedAt()
                ))
                .collect(Collectors.toList());
    }

    public Map<String, Object> getFeedbackStats() {
        log.info("Fetching feedback stats");
        Map<String, Object> stats = new HashMap<>();
        
        long total = feedbackRatingRepository.count();
        stats.put("totalFeedbacks", total);
        
        if (total > 0) {
            Double avg = feedbackRatingRepository.findAverageRating();
            stats.put("averageRating", avg != null ? avg : 0.0);
            
            Map<Integer, Long> distribution = new HashMap<>();
            for (int i = 1; i <= 5; i++) {
                distribution.put(i, feedbackRatingRepository.countByRatingValue(i));
            }
            stats.put("ratingDistribution", distribution);
            
            long appliedCount = feedbackRatingRepository.findAll().stream()
                    .filter(f -> Boolean.TRUE.equals(f.getWasFixApplied()))
                    .count();
            stats.put("percentApplied", ((double) appliedCount / total) * 100);
        } else {
            stats.put("averageRating", 0.0);
            stats.put("ratingDistribution", new HashMap<>());
            stats.put("percentApplied", 0.0);
        }
        
        return stats;
    }
}
