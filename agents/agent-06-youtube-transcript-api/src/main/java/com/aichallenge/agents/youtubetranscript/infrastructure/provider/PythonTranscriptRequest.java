package com.aichallenge.agents.youtubetranscript.infrastructure.provider;

import java.util.List;

public record PythonTranscriptRequest(
        String videoId,
        List<String> preferredLanguages
) {
}
