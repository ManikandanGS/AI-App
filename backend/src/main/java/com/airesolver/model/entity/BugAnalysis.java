package com.airesolver.model.entity;

import com.airesolver.model.enums.AnalysisStatus;
import com.airesolver.model.enums.Severity;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "bug_analysis")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BugAnalysis {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Column(nullable = false)
    private String jiraTicketId;

    private String ticketSummary;

    @Column(columnDefinition = "TEXT")
    private String ticketDescription;

    @Column(columnDefinition = "TEXT")
    private String fullContext;

    @Enumerated(EnumType.STRING)
    private AnalysisStatus status;

    @Enumerated(EnumType.STRING)
    private Severity severity;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "config_id")
    private ProjectConfig config;

    @OneToMany(mappedBy = "analysis", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<Resolution> resolutions = new ArrayList<>();

    @Column(updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
