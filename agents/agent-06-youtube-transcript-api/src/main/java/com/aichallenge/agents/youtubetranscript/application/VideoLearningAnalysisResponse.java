package com.aichallenge.agents.youtubetranscript.application;

import com.aichallenge.agents.youtubetranscript.domain.ImportantSegment;
import com.aichallenge.agents.youtubetranscript.domain.ProjectApplication;
import com.aichallenge.agents.youtubetranscript.domain.VideoLearningAnalysis;

import java.time.LocalDateTime;
import java.util.List;

public record VideoLearningAnalysisResponse(
        String status,
        String videoId,
        Long analysisId,
        String analysisLanguage,
        String sourceLanguage,
        String provider,
        String model,
        String summary,
        List<String> keyIdeas,
        List<ProjectApplication> projectApplications,
        List<ImportantSegment> importantSegments,
        List<String> personalLearningNotes,
        List<String> suggestedActions,
        String promptVersion,
        boolean fromCache,
        LocalDateTime createdAt
) {
    public static VideoLearningAnalysisResponse from(VideoLearningAnalysis analysis, boolean fromCache) {
        return new VideoLearningAnalysisResponse(
                fromCache ? "ANALYSIS_ALREADY_EXISTS" : "ANALYSIS_CREATED",
                analysis.videoId(),
                analysis.id(),
                analysis.outputLanguage(),
                analysis.sourceLanguage(),
                analysis.provider(),
                analysis.model(),
                analysis.summary(),
                analysis.keyIdeas(),
                analysis.projectApplications(),
                analysis.importantSegments(),
                analysis.personalLearningNotes(),
                analysis.suggestedActions(),
                analysis.promptVersion(),
                fromCache,
                analysis.createdAt()
        );
    }
}
