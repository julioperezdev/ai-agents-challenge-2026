package com.aichallenge.agents.youtubetranscript.infrastructure.persistence;

import com.aichallenge.agents.youtubetranscript.domain.ImportantSegment;
import com.aichallenge.agents.youtubetranscript.domain.ProjectApplication;
import com.aichallenge.agents.youtubetranscript.domain.VideoLearningAnalysis;
import com.aichallenge.agents.youtubetranscript.domain.port.VideoLearningAnalysisRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class JpaVideoLearningAnalysisRepository implements VideoLearningAnalysisRepository {

    private static final TypeReference<List<String>> STRING_LIST = new TypeReference<>() {
    };
    private static final TypeReference<List<ProjectApplication>> PROJECT_APPLICATION_LIST = new TypeReference<>() {
    };
    private static final TypeReference<List<ImportantSegment>> IMPORTANT_SEGMENT_LIST = new TypeReference<>() {
    };

    private final YoutubeVideoAnalysisJpaRepository analysisJpaRepository;
    private final YoutubeVideoJpaRepository videoJpaRepository;
    private final YoutubeTranscriptJpaRepository transcriptJpaRepository;
    private final ObjectMapper objectMapper;

    public JpaVideoLearningAnalysisRepository(
            YoutubeVideoAnalysisJpaRepository analysisJpaRepository,
            YoutubeVideoJpaRepository videoJpaRepository,
            YoutubeTranscriptJpaRepository transcriptJpaRepository,
            ObjectMapper objectMapper
    ) {
        this.analysisJpaRepository = analysisJpaRepository;
        this.videoJpaRepository = videoJpaRepository;
        this.transcriptJpaRepository = transcriptJpaRepository;
        this.objectMapper = objectMapper;
    }

    @Override
    public Optional<VideoLearningAnalysis> findLatestByVideoId(String videoId) {
        return analysisJpaRepository.findFirstByVideoVideoIdOrderByCreatedAtDesc(videoId).map(this::toDomain);
    }

    @Override
    public VideoLearningAnalysis save(VideoLearningAnalysis analysis) {
        YoutubeVideoEntity video = videoJpaRepository.findById(analysis.videoId())
                .orElseThrow(() -> new IllegalStateException("Video not found: " + analysis.videoId()));
        YoutubeTranscriptEntity transcript = transcriptJpaRepository.findById(analysis.transcriptId())
                .orElseThrow(() -> new IllegalStateException("Transcript not found: " + analysis.transcriptId()));

        YoutubeVideoAnalysisEntity entity = new YoutubeVideoAnalysisEntity(
                video,
                transcript,
                analysis.sourceLanguage(),
                analysis.outputLanguage(),
                analysis.provider(),
                analysis.model(),
                analysis.summary(),
                toJson(analysis.keyIdeas()),
                toJson(analysis.projectApplications()),
                toJson(analysis.importantSegments()),
                toJson(analysis.personalLearningNotes()),
                toJson(analysis.suggestedActions()),
                analysis.promptVersion(),
                analysis.createdAt()
        );

        return toDomain(analysisJpaRepository.save(entity));
    }

    private VideoLearningAnalysis toDomain(YoutubeVideoAnalysisEntity entity) {
        return new VideoLearningAnalysis(
                entity.getId(),
                entity.getVideo().getVideoId(),
                entity.getTranscript().getId(),
                entity.getSourceLanguage(),
                entity.getOutputLanguage(),
                entity.getProvider(),
                entity.getModel(),
                entity.getSummary(),
                fromJson(entity.getKeyIdeasJson(), STRING_LIST),
                fromJson(entity.getProjectApplicationsJson(), PROJECT_APPLICATION_LIST),
                fromJson(entity.getImportantSegmentsJson(), IMPORTANT_SEGMENT_LIST),
                fromJson(entity.getPersonalLearningNotesJson(), STRING_LIST),
                fromJson(entity.getSuggestedActionsJson(), STRING_LIST),
                entity.getPromptVersion(),
                entity.getCreatedAt()
        );
    }

    private String toJson(Object value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (JsonProcessingException ex) {
            throw new IllegalStateException("Could not serialize learning analysis JSON", ex);
        }
    }

    private <T> T fromJson(String json, TypeReference<T> typeReference) {
        try {
            return objectMapper.readValue(json, typeReference);
        } catch (JsonProcessingException ex) {
            throw new IllegalStateException("Could not deserialize learning analysis JSON", ex);
        }
    }
}
