package com.aichallenge.agents.youtubetranscript.domain;

import java.time.LocalDateTime;

public record LearningVideoLibraryItem(
        String videoId,
        String url,
        String language,
        boolean generated,
        boolean transcriptStored,
        int segmentsStored,
        TranscriptProxyUsage proxyUsage,
        boolean analysisAvailable,
        Long latestAnalysisId,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
