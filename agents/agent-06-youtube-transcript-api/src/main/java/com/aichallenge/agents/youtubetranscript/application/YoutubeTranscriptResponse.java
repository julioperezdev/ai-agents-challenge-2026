package com.aichallenge.agents.youtubetranscript.application;

import com.aichallenge.agents.youtubetranscript.domain.Transcript;
import com.aichallenge.agents.youtubetranscript.domain.TranscriptSource;
import com.aichallenge.agents.youtubetranscript.domain.TranscriptStatus;

import java.util.List;

public record YoutubeTranscriptResponse(
        TranscriptStatus status,
        String videoId,
        TranscriptSource source,
        String language,
        boolean isGenerated,
        boolean fromCache,
        String fullText,
        List<TranscriptSegmentResponse> segments,
        TranscriptInsightResponse insight,
        TranscriptProxyUsageResponse proxyUsage,
        String reason
) {
    public static YoutubeTranscriptResponse found(Transcript transcript, boolean fromCache, TranscriptInsightResponse insight) {
        return new YoutubeTranscriptResponse(
                TranscriptStatus.TRANSCRIPT_FOUND,
                transcript.videoId(),
                transcript.source(),
                transcript.language(),
                transcript.generated(),
                fromCache,
                transcript.fullText(),
                transcript.segments().stream()
                        .map(segment -> new TranscriptSegmentResponse(
                                segment.position(),
                                segment.start(),
                                segment.duration(),
                                segment.text()
                        ))
                        .toList(),
                insight,
                TranscriptProxyUsageResponse.from(transcript.proxyUsage()),
                null
        );
    }

    public static YoutubeTranscriptResponse failed(TranscriptStatus status, String videoId, String reason) {
        return new YoutubeTranscriptResponse(status, videoId, null, null, false, false, null, List.of(), null, null, reason);
    }
}
