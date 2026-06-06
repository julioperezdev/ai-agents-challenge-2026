package com.aichallenge.agents.youtubetranscript.application;

import java.util.List;

public record TranscriptInsightResponse(
        String contextLanguage,
        String outputLanguage,
        String languageDetectionMethod,
        boolean languageFallbackUsed,
        String llmContextPreview,
        String llmInstructions,
        String spanishExplanation,
        List<String> keyPoints
) {
}
