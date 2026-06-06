package com.aichallenge.agents.youtubetranscript.domain;

public record ImportantSegment(
        double start,
        double duration,
        String reason
) {
}
