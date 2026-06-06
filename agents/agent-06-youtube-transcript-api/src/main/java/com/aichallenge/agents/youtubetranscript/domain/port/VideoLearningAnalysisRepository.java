package com.aichallenge.agents.youtubetranscript.domain.port;

import com.aichallenge.agents.youtubetranscript.domain.VideoLearningAnalysis;

import java.util.Optional;

public interface VideoLearningAnalysisRepository {
    Optional<VideoLearningAnalysis> findLatestByVideoId(String videoId);

    VideoLearningAnalysis save(VideoLearningAnalysis analysis);
}
