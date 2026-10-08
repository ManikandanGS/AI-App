package com.airesolver.repository;

import com.airesolver.model.entity.FeedbackRating;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FeedbackRatingRepository extends JpaRepository<FeedbackRating, Long> {
    List<FeedbackRating> findByResolutionId(Long resolutionId);
    
    List<FeedbackRating> findByRatingGreaterThanEqual(int minRating);

    @Query("SELECT AVG(f.rating) FROM FeedbackRating f")
    Double findAverageRating();

    @Query("SELECT COUNT(f) FROM FeedbackRating f WHERE f.rating = :rating")
    Long countByRatingValue(int rating);
}
