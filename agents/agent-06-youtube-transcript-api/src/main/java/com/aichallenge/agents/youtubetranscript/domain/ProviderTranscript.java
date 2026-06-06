package com.aichallenge.agents.youtubetranscript.domain;

import java.util.List;

public record ProviderTranscript(
        TranscriptStatus status,
        String videoId,
        String language,
        boolean generated,
        TranscriptSource source,
        String languageDetectionMethod,
        boolean languageFallbackUsed,
        List<ProviderTranscriptSegment> segments,
        TranscriptProxyUsage proxyUsage,
        String reason
) {
    public ProviderTranscript {
        segments = segments == null ? List.of() : List.copyOf(segments);
        proxyUsage = proxyUsage == null ? TranscriptProxyUsage.empty() : proxyUsage;
    }

    public static ProviderTranscript found(
            String videoId,
            String language,
            boolean generated,
            String languageDetectionMethod,
            boolean languageFallbackUsed,
            List<ProviderTranscriptSegment> segments,
            TranscriptProxyUsage proxyUsage
    ) {
        return new ProviderTranscript(
                TranscriptStatus.TRANSCRIPT_FOUND,
                videoId,
                language,
                generated,
                TranscriptSource.YOUTUBE_CAPTIONS,
                languageDetectionMethod,
                languageFallbackUsed,
                segments,
                proxyUsage,
                null
        );
    }

    public static ProviderTranscript failed(TranscriptStatus status, String videoId, TranscriptProxyUsage proxyUsage, String reason) {
        return new ProviderTranscript(status, videoId, null, false, TranscriptSource.YOUTUBE_CAPTIONS, null, false, List.of(), proxyUsage, reason);
    }
}
