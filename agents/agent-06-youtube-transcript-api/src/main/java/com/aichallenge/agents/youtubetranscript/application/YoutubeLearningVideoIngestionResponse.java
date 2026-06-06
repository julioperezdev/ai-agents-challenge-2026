package com.aichallenge.agents.youtubetranscript.application;

import com.aichallenge.agents.youtubetranscript.domain.TranscriptSource;
import com.aichallenge.agents.youtubetranscript.domain.TranscriptStatus;

import java.util.List;

public record YoutubeLearningVideoIngestionResponse(
        String status,
        String videoId,
        String url,
        String language,
        TranscriptSource source,
        boolean isGenerated,
        boolean fromCache,
        boolean transcriptStored,
        int segmentsStored,
        boolean readyForAnalysis,
        TranscriptProxyUsageResponse proxyUsage,
        List<NextActionResponse> nextActions,
        String reason
) {
    public static YoutubeLearningVideoIngestionResponse fromTranscript(
            YoutubeTranscriptResponse transcriptResponse,
            String url
    ) {
        boolean found = transcriptResponse.status() == TranscriptStatus.TRANSCRIPT_FOUND;
        String status = transcriptResponse.fromCache() ? "VIDEO_ALREADY_INGESTED" : "VIDEO_INGESTED";

        return new YoutubeLearningVideoIngestionResponse(
                found ? status : transcriptResponse.status().name(),
                transcriptResponse.videoId(),
                url,
                transcriptResponse.language(),
                transcriptResponse.source(),
                transcriptResponse.isGenerated(),
                transcriptResponse.fromCache(),
                found,
                transcriptResponse.segments().size(),
                found,
                transcriptResponse.proxyUsage(),
                found ? List.of(new NextActionResponse(
                        "ANALYZE_FOR_LEARNING",
                        "POST",
                        "/api/v1/learning/youtube/videos/" + transcriptResponse.videoId() + "/analysis"
                )) : List.of(),
                transcriptResponse.reason()
        );
    }
}
