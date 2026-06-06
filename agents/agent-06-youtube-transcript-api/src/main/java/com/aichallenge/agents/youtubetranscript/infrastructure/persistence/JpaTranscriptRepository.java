package com.aichallenge.agents.youtubetranscript.infrastructure.persistence;

import com.aichallenge.agents.youtubetranscript.domain.Transcript;
import com.aichallenge.agents.youtubetranscript.domain.TranscriptProxyUsage;
import com.aichallenge.agents.youtubetranscript.domain.TranscriptSegment;
import com.aichallenge.agents.youtubetranscript.domain.port.TranscriptRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public class JpaTranscriptRepository implements TranscriptRepository {

    private final YoutubeVideoJpaRepository videoJpaRepository;
    private final YoutubeTranscriptJpaRepository transcriptJpaRepository;

    public JpaTranscriptRepository(
            YoutubeVideoJpaRepository videoJpaRepository,
            YoutubeTranscriptJpaRepository transcriptJpaRepository
    ) {
        this.videoJpaRepository = videoJpaRepository;
        this.transcriptJpaRepository = transcriptJpaRepository;
    }

    @Override
    public Optional<Transcript> findFirstByVideoIdAndPreferredLanguages(String videoId, List<String> preferredLanguages) {
        for (String language : preferredLanguages) {
            Optional<YoutubeTranscriptEntity> transcript = transcriptJpaRepository.findByVideoVideoIdAndLanguage(videoId, language);
            if (transcript.isPresent()) {
                return transcript.map(this::toDomain);
            }
        }
        return findFirstByVideoId(videoId);
    }

    @Override
    public Optional<Transcript> findFirstByVideoId(String videoId) {
        return transcriptJpaRepository.findFirstByVideoVideoIdOrderByIdAsc(videoId).map(this::toDomain);
    }

    @Override
    public Transcript save(String videoId, String originalUrl, Transcript transcript) {
        LocalDateTime now = LocalDateTime.now();
        YoutubeVideoEntity video = videoJpaRepository.findById(videoId)
                .map(existing -> {
                    existing.updateOriginalUrl(originalUrl, now);
                    return existing;
                })
                .orElseGet(() -> new YoutubeVideoEntity(videoId, originalUrl, now));

        YoutubeVideoEntity savedVideo = videoJpaRepository.save(video);

        YoutubeTranscriptEntity entity = transcriptJpaRepository.findByVideoVideoIdAndLanguage(videoId, transcript.language())
                .map(existing -> {
                    existing.replaceWith(
                            transcript.fullText(),
                            transcript.generated(),
                            transcript.source(),
                            transcript.languageDetectionMethod(),
                            transcript.languageFallbackUsed(),
                            transcript.proxyUsage().route(),
                            transcript.proxyUsage().requestCount(),
                            transcript.proxyUsage().requestBytes(),
                            transcript.proxyUsage().responseBytes(),
                            transcript.proxyUsage().totalBytes(),
                            transcript.proxyUsage().totalMb(),
                            transcript.proxyUsage().proxyPricePerGbUsd(),
                            transcript.proxyUsage().estimatedProxyCostUsd(),
                            transcript.proxyUsage().httpStatusesJson(),
                            transcript.proxyUsage().elapsedSeconds(),
                            now
                    );
                    return existing;
                })
                .orElseGet(() -> new YoutubeTranscriptEntity(
                        savedVideo,
                        transcript.language(),
                        transcript.source(),
                        transcript.generated(),
                        transcript.languageDetectionMethod(),
                        transcript.languageFallbackUsed(),
                        transcript.fullText(),
                        now
                ));

        entity.updateProxyUsage(
                transcript.proxyUsage().route(),
                transcript.proxyUsage().requestCount(),
                transcript.proxyUsage().requestBytes(),
                transcript.proxyUsage().responseBytes(),
                transcript.proxyUsage().totalBytes(),
                transcript.proxyUsage().totalMb(),
                transcript.proxyUsage().proxyPricePerGbUsd(),
                transcript.proxyUsage().estimatedProxyCostUsd(),
                transcript.proxyUsage().httpStatusesJson(),
                transcript.proxyUsage().elapsedSeconds()
        );

        transcript.segments().forEach(segment -> entity.addSegment(new YoutubeTranscriptSegmentEntity(
                segment.position(),
                segment.start(),
                segment.duration(),
                segment.text()
        )));

        return toDomain(transcriptJpaRepository.save(entity));
    }

    private Transcript toDomain(YoutubeTranscriptEntity entity) {
        return new Transcript(
                entity.getId(),
                entity.getVideo().getVideoId(),
                entity.getLanguage(),
                entity.getSource(),
                entity.getGenerated(),
                entity.getLanguageDetectionMethod(),
                Boolean.TRUE.equals(entity.getLanguageFallbackUsed()),
                entity.getFullText(),
                entity.getSegments().stream()
                        .map(segment -> new TranscriptSegment(
                                segment.getPosition(),
                                segment.getStart(),
                                segment.getDuration(),
                                segment.getText()
                        ))
                        .toList(),
                new TranscriptProxyUsage(
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
                )
        );
    }
}
