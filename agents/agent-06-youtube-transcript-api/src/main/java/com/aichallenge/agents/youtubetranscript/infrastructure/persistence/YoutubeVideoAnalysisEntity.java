package com.aichallenge.agents.youtubetranscript.infrastructure.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import org.hibernate.annotations.ColumnTransformer;

import java.time.LocalDateTime;

@Entity
@Table(name = "youtube_video_analysis")
public class YoutubeVideoAnalysisEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "video_id", nullable = false)
    private YoutubeVideoEntity video;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "transcript_id", nullable = false)
    private YoutubeTranscriptEntity transcript;

    @Column(name = "source_language", length = 10, nullable = false)
    private String sourceLanguage;

    @Column(name = "output_language", length = 10, nullable = false)
    private String outputLanguage;

    @Column(name = "provider", length = 40, nullable = false)
    private String provider;

    @Column(name = "model", length = 120, nullable = false)
    private String model;

    @Column(name = "summary", nullable = false, columnDefinition = "TEXT")
    private String summary;

    @Column(name = "key_ideas_json", nullable = false, columnDefinition = "jsonb")
    @ColumnTransformer(write = "?::jsonb")
    private String keyIdeasJson;

    @Column(name = "project_applications_json", nullable = false, columnDefinition = "jsonb")
    @ColumnTransformer(write = "?::jsonb")
    private String projectApplicationsJson;

    @Column(name = "important_segments_json", nullable = false, columnDefinition = "jsonb")
    @ColumnTransformer(write = "?::jsonb")
    private String importantSegmentsJson;

    @Column(name = "personal_learning_notes_json", nullable = false, columnDefinition = "jsonb")
    @ColumnTransformer(write = "?::jsonb")
    private String personalLearningNotesJson;

    @Column(name = "suggested_actions_json", nullable = false, columnDefinition = "jsonb")
    @ColumnTransformer(write = "?::jsonb")
    private String suggestedActionsJson;

    @Column(name = "prompt_version", length = 40, nullable = false)
    private String promptVersion;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    protected YoutubeVideoAnalysisEntity() {
    }

    public YoutubeVideoAnalysisEntity(
            YoutubeVideoEntity video,
            YoutubeTranscriptEntity transcript,
            String sourceLanguage,
            String outputLanguage,
            String provider,
            String model,
            String summary,
            String keyIdeasJson,
            String projectApplicationsJson,
            String importantSegmentsJson,
            String personalLearningNotesJson,
            String suggestedActionsJson,
            String promptVersion,
            LocalDateTime createdAt
    ) {
        this.video = video;
        this.transcript = transcript;
        this.sourceLanguage = sourceLanguage;
        this.outputLanguage = outputLanguage;
        this.provider = provider;
        this.model = model;
        this.summary = summary;
        this.keyIdeasJson = keyIdeasJson;
        this.projectApplicationsJson = projectApplicationsJson;
        this.importantSegmentsJson = importantSegmentsJson;
        this.personalLearningNotesJson = personalLearningNotesJson;
        this.suggestedActionsJson = suggestedActionsJson;
        this.promptVersion = promptVersion;
        this.createdAt = createdAt;
    }

    public Long getId() {
        return id;
    }

    public YoutubeVideoEntity getVideo() {
        return video;
    }

    public YoutubeTranscriptEntity getTranscript() {
        return transcript;
    }

    public String getSourceLanguage() {
        return sourceLanguage;
    }

    public String getOutputLanguage() {
        return outputLanguage;
    }

    public String getProvider() {
        return provider;
    }

    public String getModel() {
        return model;
    }

    public String getSummary() {
        return summary;
    }

    public String getKeyIdeasJson() {
        return keyIdeasJson;
    }

    public String getProjectApplicationsJson() {
        return projectApplicationsJson;
    }

    public String getImportantSegmentsJson() {
        return importantSegmentsJson;
    }

    public String getPersonalLearningNotesJson() {
        return personalLearningNotesJson;
    }

    public String getSuggestedActionsJson() {
        return suggestedActionsJson;
    }

    public String getPromptVersion() {
        return promptVersion;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}
