package com.airesolver.repository;

import com.airesolver.model.entity.BugAnalysis;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BugAnalysisRepository extends JpaRepository<BugAnalysis, Long> {
    Optional<BugAnalysis> findByJiraTicketId(String ticketId);
    List<BugAnalysis> findAllByOrderByCreatedAtDesc();
    List<BugAnalysis> findByConfigId(Long configId);
}
