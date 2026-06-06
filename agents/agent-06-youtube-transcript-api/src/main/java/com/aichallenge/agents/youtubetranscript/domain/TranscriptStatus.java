package com.aichallenge.agents.youtubetranscript.domain;

public enum TranscriptStatus {
    TRANSCRIPT_FOUND,
    TRANSCRIPT_NOT_AVAILABLE,
    VIDEO_UNAVAILABLE,
    INVALID_YOUTUBE_URL,
    TRANSCRIPT_PROVIDER_BLOCKED,
    PROVIDER_ERROR
}
