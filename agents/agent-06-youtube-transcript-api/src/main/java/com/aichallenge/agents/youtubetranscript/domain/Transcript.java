package com.aichallenge.agents.youtubetranscript.domain;

import java.util.List;

public record Transcript(
        Long id,
        String videoId,
        String language,
        TranscriptSource source,
        boolean generated,
        String languageDetectionMethod,
        boolean languageFallbackUsed,
        String fullText,
        List<TranscriptSegment> segments,
        TranscriptProxyUsage proxyUsage
) {
    public Transcript {
        segments = List.copyOf(segments);
        proxyUsage = proxyUsage == null ? TranscriptProxyUsage.empty() : proxyUsage;
    }
}
