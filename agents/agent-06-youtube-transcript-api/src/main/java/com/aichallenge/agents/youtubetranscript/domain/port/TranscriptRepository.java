package com.aichallenge.agents.youtubetranscript.domain.port;

import com.aichallenge.agents.youtubetranscript.domain.Transcript;

import java.util.List;
import java.util.Optional;

public interface TranscriptRepository {
    Optional<Transcript> findFirstByVideoIdAndPreferredLanguages(String videoId, List<String> preferredLanguages);

    Optional<Transcript> findFirstByVideoId(String videoId);

    Transcript save(String videoId, String originalUrl, Transcript transcript);
}
