package com.aichallenge.agents.youtubetranscript.domain.port;

import com.aichallenge.agents.youtubetranscript.domain.Transcript;
import com.aichallenge.agents.youtubetranscript.domain.VideoLearningAnalysis;

public interface VideoLearningAnalysisGenerator {
    VideoLearningAnalysis analyze(Transcript transcript);
}
