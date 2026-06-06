package com.aichallenge.agents.youtubetranscript.domain;

import java.time.LocalDateTime;
import java.util.List;

public record VideoLearningAnalysis(
        Long id,
        String videoId,
        Long transcriptId,
        String sourceLanguage,
        String outputLanguage,
        String provider,
        String model,
        String summary,
        List<String> keyIdeas,
        List<ProjectApplication> projectApplications,
        List<ImportantSegment> importantSegments,
        List<String> personalLearningNotes,
        List<String> suggestedActions,
        String promptVersion,
        LocalDateTime createdAt
) {
    public VideoLearningAnalysis {
        keyIdeas = List.copyOf(keyIdeas);
        projectApplications = List.copyOf(projectApplications);
        importantSegments = List.copyOf(importantSegments);
        personalLearningNotes = List.copyOf(personalLearningNotes);
        suggestedActions = List.copyOf(suggestedActions);
    }
}
