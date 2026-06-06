package com.aichallenge.agents.youtubetranscript.application;

public record NextActionResponse(
        String type,
        String method,
        String href
) {
}
