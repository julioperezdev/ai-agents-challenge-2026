package com.aichallenge.agents.youtubetranscript.application;

public record TranscriptSegmentResponse(
        int position,
        double start,
        double duration,
        String text
) {
}
