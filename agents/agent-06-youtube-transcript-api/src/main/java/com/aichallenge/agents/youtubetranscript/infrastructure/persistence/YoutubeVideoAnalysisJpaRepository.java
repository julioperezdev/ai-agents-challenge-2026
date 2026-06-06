package com.aichallenge.agents.youtubetranscript.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface YoutubeVideoAnalysisJpaRepository extends JpaRepository<YoutubeVideoAnalysisEntity, Long> {
    Optional<YoutubeVideoAnalysisEntity> findFirstByVideoVideoIdOrderByCreatedAtDesc(String videoId);
}
