CREATE TABLE youtube_video (
    video_id VARCHAR(50) PRIMARY KEY,
    original_url TEXT NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL
);

CREATE TABLE youtube_transcript (
    id BIGSERIAL PRIMARY KEY,
    video_id VARCHAR(50) NOT NULL,
    language VARCHAR(10) NOT NULL,
    source VARCHAR(50) NOT NULL,
    generated BOOLEAN NOT NULL,
    language_detection_method VARCHAR(80) NOT NULL DEFAULT 'YOUTUBE_TRANSCRIPT_METADATA',
    language_fallback_used BOOLEAN NOT NULL DEFAULT FALSE,
    full_text TEXT NOT NULL,
    proxy_route VARCHAR(40) NOT NULL DEFAULT 'unknown',
    proxy_request_count INTEGER NOT NULL DEFAULT 0,
    proxy_request_bytes BIGINT NOT NULL DEFAULT 0,
    proxy_response_bytes BIGINT NOT NULL DEFAULT 0,
    proxy_total_bytes BIGINT NOT NULL DEFAULT 0,
    proxy_total_mb DOUBLE PRECISION NOT NULL DEFAULT 0,
    proxy_price_per_gb_usd DOUBLE PRECISION NOT NULL DEFAULT 0,
    proxy_estimated_cost_usd DOUBLE PRECISION NOT NULL DEFAULT 0,
    proxy_http_statuses_json JSONB NOT NULL DEFAULT '{}'::jsonb,
    proxy_elapsed_seconds DOUBLE PRECISION NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    CONSTRAINT fk_youtube_transcript_video
        FOREIGN KEY (video_id)
        REFERENCES youtube_video(video_id),
    CONSTRAINT uk_youtube_transcript_video_language
        UNIQUE (video_id, language)
);

CREATE TABLE youtube_transcript_segment (
    id BIGSERIAL PRIMARY KEY,
    transcript_id BIGINT NOT NULL,
    position INTEGER NOT NULL,
    start_time DOUBLE PRECISION NOT NULL,
    duration DOUBLE PRECISION NOT NULL,
    text TEXT NOT NULL,
    CONSTRAINT fk_youtube_transcript_segment_transcript
        FOREIGN KEY (transcript_id)
        REFERENCES youtube_transcript(id)
        ON DELETE CASCADE,
    CONSTRAINT uk_youtube_transcript_segment_position
        UNIQUE (transcript_id, position)
);

CREATE INDEX idx_youtube_transcript_video_id
ON youtube_transcript(video_id);

CREATE INDEX idx_youtube_transcript_language
ON youtube_transcript(language);

CREATE INDEX idx_youtube_transcript_segment_transcript_id
ON youtube_transcript_segment(transcript_id);
