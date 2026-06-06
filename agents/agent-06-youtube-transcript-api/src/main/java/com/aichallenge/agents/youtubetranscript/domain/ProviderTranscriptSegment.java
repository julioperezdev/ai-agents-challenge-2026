package com.aichallenge.agents.youtubetranscript.domain;

public record ProviderTranscriptSegment(
        double start,
        double duration,
        String text
) {
}
