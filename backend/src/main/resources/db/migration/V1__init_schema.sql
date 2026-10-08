CREATE TABLE project_config (
    id BIGSERIAL PRIMARY KEY,
    project_key VARCHAR(255) NOT NULL UNIQUE,
    jira_base_url VARCHAR(255) NOT NULL,
    jira_username VARCHAR(255),
    jira_api_token VARCHAR(255),
    bitbucket_base_url VARCHAR(255),
    bitbucket_username VARCHAR(255),
    bitbucket_app_password VARCHAR(255),
    bitbucket_workspace VARCHAR(255),
    bitbucket_repo_slug VARCHAR(255),
    db_url VARCHAR(255),
    db_username VARCHAR(255),
    db_password_encrypted VARCHAR(255),
    created_at TIMESTAMP,
    updated_at TIMESTAMP
);

CREATE TABLE bug_analysis (
    id BIGSERIAL PRIMARY KEY,
    jira_ticket_id VARCHAR(255) NOT NULL,
    ticket_summary VARCHAR(255),
    ticket_description TEXT,
    full_context TEXT,
    status VARCHAR(50),
    severity VARCHAR(50),
    config_id BIGINT REFERENCES project_config(id),
    created_at TIMESTAMP
);

CREATE TABLE resolution (
    id BIGSERIAL PRIMARY KEY,
    analysis_id BIGINT REFERENCES bug_analysis(id),
    root_cause TEXT,
    suggested_fix TEXT,
    code_diff TEXT,
    files_affected TEXT,
    ai_model_used VARCHAR(255),
    confidence_score DOUBLE PRECISION,
    created_at TIMESTAMP
);

CREATE TABLE feedback_rating (
    id BIGSERIAL PRIMARY KEY,
    resolution_id BIGINT REFERENCES resolution(id),
    rating INT,
    feedback_text TEXT,
    was_fix_applied BOOLEAN,
    created_at TIMESTAMP
);

-- Indexes for query performance
CREATE INDEX idx_bug_analysis_jira_ticket_id ON bug_analysis(jira_ticket_id);
CREATE INDEX idx_bug_analysis_config_id ON bug_analysis(config_id);
CREATE INDEX idx_bug_analysis_status ON bug_analysis(status);
CREATE INDEX idx_resolution_analysis_id ON resolution(analysis_id);
CREATE INDEX idx_feedback_rating_resolution_id ON feedback_rating(resolution_id);
CREATE INDEX idx_feedback_rating_rating ON feedback_rating(rating);
