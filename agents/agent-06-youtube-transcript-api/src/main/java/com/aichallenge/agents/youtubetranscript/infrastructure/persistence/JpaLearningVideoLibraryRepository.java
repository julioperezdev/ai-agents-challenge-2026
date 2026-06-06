package com.aichallenge.agents.youtubetranscript.infrastructure.persistence;

import com.aichallenge.agents.youtubetranscript.domain.LearningVideoLibraryItem;
import com.aichallenge.agents.youtubetranscript.domain.TranscriptProxyUsage;
import com.aichallenge.agents.youtubetranscript.domain.port.LearningVideoLibraryRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class JpaLearningVideoLibraryRepository implements LearningVideoLibraryRepository {

    private final YoutubeVideoJpaRepository videoJpaRepository;
    private final YoutubeTranscriptJpaRepository transcriptJpaRepository;
    private final YoutubeVideoAnalysisJpaRepository analysisJpaRepository;

    public JpaLearningVideoLibraryRepository(
            YoutubeVideoJpaRepository videoJpaRepository,
            YoutubeTranscriptJpaRepository transcriptJpaRepository,
            YoutubeVideoAnalysisJpaRepository analysisJpaRepository
    ) {
        this.videoJpaRepository = videoJpaRepository;
        this.transcriptJpaRepository = transcriptJpaRepository;
        this.analysisJpaRepository = analysisJpaRepository;
    }

    @Override
    public List<LearningVideoLibraryItem> listRecentVideos() {
        return videoJpaRepository.findAllByOrderByUpdatedAtDesc()
                .stream()
                .map(this::toLibraryItem)
                .toList();
    }

    private LearningVideoLibraryItem toLibraryItem(YoutubeVideoEntity video) {
        Optional<YoutubeTranscriptEntity> transcript = transcriptJpaRepository
                .findByVideoVideoIdOrderByIdAsc(video.getVideoId())
                .stream()
                .findFirst();
        Optional<YoutubeVideoAnalysisEntity> latestAnalysis = analysisJpaRepository
                .findFirstByVideoVideoIdOrderByCreatedAtDesc(video.getVideoId());

        return new LearningVideoLibraryItem(
                video.getVideoId(),
                video.getOriginalUrl(),
                transcript.map(YoutubeTranscriptEntity::getLanguage).orElse(null),
                transcript.map(YoutubeTranscriptEntity::getGenerated).orElse(false),
                transcript.isPresent(),
                transcript.map(value -> value.getSegments().size()).orElse(0),
                transcript.map(this::toProxyUsage).orElse(TranscriptProxyUsage.empty()),
                latestAnalysis.isPresent(),
                latestAnalysis.map(YoutubeVideoAnalysisEntity::getId).orElse(null),
                video.getCreatedAt(),
                video.getUpdatedAt()
        );
    }

    private TranscriptProxyUsage toProxyUsage(YoutubeTranscriptEntity entity) {
        return new TranscriptProxyUsage(
                entity.getProxyRoute(),
                entity.getProxyRequestCount(),
                entity.getProxyRequestBytes(),
                entity.getProxyResponseBytes(),
                entity.getProxyTotalBytes(),
                entity.getProxyTotalMb(),
                entity.getProxyPricePerGbUsd(),
                entity.getProxyEstimatedCostUsd(),
                entity.getProxyHttpStatusesJson(),
                entity.getProxyElapsedSeconds()
        );
    }
}
