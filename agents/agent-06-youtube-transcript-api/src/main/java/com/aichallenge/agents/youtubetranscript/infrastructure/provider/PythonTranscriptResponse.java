package com.aichallenge.agents.youtubetranscript.infrastructure.provider;

import com.aichallenge.agents.youtubetranscript.domain.TranscriptStatus;

import java.util.List;

public record PythonTranscriptResponse(
        TranscriptStatus status,
        String videoId,
        String language,
        Boolean isGenerated,
        String languageDetectionMethod,
        Boolean languageFallbackUsed,
        List<PythonTranscriptSegmentResponse> segments,
        PythonProxyUsageResponse proxyUsage,
        String reason
) {
}
