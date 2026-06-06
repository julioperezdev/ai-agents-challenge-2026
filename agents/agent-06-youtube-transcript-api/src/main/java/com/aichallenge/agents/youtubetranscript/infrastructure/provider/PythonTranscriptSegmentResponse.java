package com.aichallenge.agents.youtubetranscript.infrastructure.provider;

public record PythonTranscriptSegmentResponse(
        double start,
        double duration,
        String text
) {
}
