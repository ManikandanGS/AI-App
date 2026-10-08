package com.airesolver.repository;

import com.airesolver.model.entity.Resolution;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ResolutionRepository extends JpaRepository<Resolution, Long> {
    List<Resolution> findByAnalysisId(Long analysisId);
}
