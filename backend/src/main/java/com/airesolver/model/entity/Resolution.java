package com.airesolver.model.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "resolution")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Resolution {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "analysis_id")
    private BugAnalysis analysis;

    @Column(columnDefinition = "TEXT")
    private String rootCause;

    @Column(columnDefinition = "TEXT")
    private String suggestedFix;

    @Column(columnDefinition = "TEXT")
    private String codeDiff;

    @Column(columnDefinition = "TEXT")
    private String filesAffected;

    private String aiModelUsed;

    private Double confidenceScore;

    @Transient
    private String severity;

    @OneToMany(mappedBy = "resolution", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<FeedbackRating> feedbackRatings = new ArrayList<>();

    @Column(updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
