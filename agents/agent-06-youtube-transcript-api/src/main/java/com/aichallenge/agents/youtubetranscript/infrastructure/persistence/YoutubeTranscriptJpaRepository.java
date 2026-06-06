package com.aichallenge.agents.youtubetranscript.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface YoutubeTranscriptJpaRepository extends JpaRepository<YoutubeTranscriptEntity, Long> {
    Optional<YoutubeTranscriptEntity> findByVideoVideoIdAndLanguage(String videoId, String language);

    Optional<YoutubeTranscriptEntity> findFirstByVideoVideoIdOrderByIdAsc(String videoId);

    List<YoutubeTranscriptEntity> findByVideoVideoIdOrderByIdAsc(String videoId);
}
