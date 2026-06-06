package com.aichallenge.agents.youtubetranscript.application;

public record AnalyzeYoutubeLearningVideoRequest(
        Boolean forceRefresh
) {
    public boolean shouldForceRefresh() {
        return Boolean.TRUE.equals(forceRefresh);
    }
}
