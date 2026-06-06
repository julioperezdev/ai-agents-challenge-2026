package com.aichallenge.agents.youtubetranscript.application;

import jakarta.validation.constraints.NotBlank;

import java.util.List;

public record YoutubeTranscriptRequest(
        @NotBlank(message = "url is required")
        String url,
        List<String> preferredLanguages,
        Boolean forceRefresh
) {
    public List<String> normalizedLanguages() {
        if (preferredLanguages == null || preferredLanguages.isEmpty()) {
            return List.of();
        }

        List<String> languages = preferredLanguages.stream()
                .filter(language -> language != null && !language.isBlank())
                .map(String::trim)
                .toList();
        return languages;
    }

    public boolean shouldForceRefresh() {
        return Boolean.TRUE.equals(forceRefresh);
    }
}
