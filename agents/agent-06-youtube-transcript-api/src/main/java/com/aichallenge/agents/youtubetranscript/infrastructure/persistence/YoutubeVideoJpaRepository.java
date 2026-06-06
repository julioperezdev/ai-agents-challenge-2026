package com.aichallenge.agents.youtubetranscript.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface YoutubeVideoJpaRepository extends JpaRepository<YoutubeVideoEntity, String> {
    List<YoutubeVideoEntity> findAllByOrderByUpdatedAtDesc();
}
