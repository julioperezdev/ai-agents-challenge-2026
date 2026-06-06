CREATE TABLE youtube_video_analysis (
    id BIGSERIAL PRIMARY KEY,
    video_id VARCHAR(50) NOT NULL,
    transcript_id BIGINT NOT NULL,
    source_language VARCHAR(10) NOT NULL,
    output_language VARCHAR(10) NOT NULL,
    provider VARCHAR(40) NOT NULL,
    model VARCHAR(120) NOT NULL,
    summary TEXT NOT NULL,
    key_ideas_json JSONB NOT NULL,
    project_applications_json JSONB NOT NULL,
    important_segments_json JSONB NOT NULL,
    personal_learning_notes_json JSONB NOT NULL,
    suggested_actions_json JSONB NOT NULL,
    prompt_version VARCHAR(40) NOT NULL,
    created_at TIMESTAMP NOT NULL,
    CONSTRAINT fk_youtube_video_analysis_video
        FOREIGN KEY (video_id)
        REFERENCES youtube_video(video_id)
        ON DELETE CASCADE,
    CONSTRAINT fk_youtube_video_analysis_transcript
        FOREIGN KEY (transcript_id)
        REFERENCES youtube_transcript(id)
        ON DELETE CASCADE
);

CREATE INDEX idx_youtube_video_analysis_video_id
ON youtube_video_analysis(video_id);

CREATE INDEX idx_youtube_video_analysis_created_at
ON youtube_video_analysis(created_at);
