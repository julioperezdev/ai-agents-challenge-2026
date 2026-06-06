package com.aichallenge.agents.youtubetranscript.domain;

public record TranscriptSegment(
        int position,
        double start,
        double duration,
        String text
) {
}
