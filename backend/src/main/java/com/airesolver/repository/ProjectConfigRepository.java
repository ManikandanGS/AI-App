package com.airesolver.repository;

import com.airesolver.model.entity.ProjectConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ProjectConfigRepository extends JpaRepository<ProjectConfig, Long> {
    Optional<ProjectConfig> findByProjectKey(String projectKey);
}
