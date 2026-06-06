package com.aichallenge.agents.youtubetranscript.application;

import com.aichallenge.agents.youtubetranscript.domain.LearningVideoLibraryItem;

import java.time.LocalDateTime;

public record YoutubeLearningVideoListItemResponse(
        String videoId,
        String url,
        String language,
        boolean isGenerated,
        boolean transcriptStored,
        int segmentsStored,
        TranscriptProxyUsageResponse proxyUsage,
        boolean analysisAvailable,
        Long latestAnalysisId,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public static YoutubeLearningVideoListItemResponse fromLibraryItem(LearningVideoLibraryItem item) {
        return new YoutubeLearningVideoListItemResponse(
                item.videoId(),
                item.url(),
                item.language(),
                item.generated(),
                item.transcriptStored(),
                item.segmentsStored(),
                TranscriptProxyUsageResponse.from(item.proxyUsage()),
                item.analysisAvailable(),
                item.latestAnalysisId(),
                item.createdAt(),
                item.updatedAt()
        );
    }
}
