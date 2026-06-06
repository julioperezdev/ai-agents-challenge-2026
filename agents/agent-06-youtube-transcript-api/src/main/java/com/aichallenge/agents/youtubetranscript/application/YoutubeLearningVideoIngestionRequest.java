package com.aichallenge.agents.youtubetranscript.application;

import jakarta.validation.constraints.NotBlank;

import java.util.List;

public record YoutubeLearningVideoIngestionRequest(
        @NotBlank(message = "url is required")
        String url,
        List<String> preferredLanguages,
        Boolean forceRefresh
) {
    YoutubeTranscriptRequest toTranscriptRequest() {
        return new YoutubeTranscriptRequest(url, preferredLanguages, forceRefresh);
    }
}
