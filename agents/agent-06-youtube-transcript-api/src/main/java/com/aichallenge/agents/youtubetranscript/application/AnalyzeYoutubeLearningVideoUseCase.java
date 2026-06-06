package com.aichallenge.agents.youtubetranscript.application;

import com.aichallenge.agents.youtubetranscript.domain.Transcript;
import com.aichallenge.agents.youtubetranscript.domain.VideoLearningAnalysis;
import com.aichallenge.agents.youtubetranscript.domain.port.TranscriptRepository;
import com.aichallenge.agents.youtubetranscript.domain.port.VideoLearningAnalysisGenerator;
import com.aichallenge.agents.youtubetranscript.domain.port.VideoLearningAnalysisRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AnalyzeYoutubeLearningVideoUseCase {

    private final TranscriptRepository transcriptRepository;
    private final VideoLearningAnalysisRepository analysisRepository;
    private final VideoLearningAnalysisGenerator analysisGenerator;

    public AnalyzeYoutubeLearningVideoUseCase(
            TranscriptRepository transcriptRepository,
            VideoLearningAnalysisRepository analysisRepository,
            VideoLearningAnalysisGenerator analysisGenerator
    ) {
        this.transcriptRepository = transcriptRepository;
        this.analysisRepository = analysisRepository;
        this.analysisGenerator = analysisGenerator;
    }

    @Transactional
    public VideoLearningAnalysisResponse execute(String videoId, boolean forceRefresh) {
        if (!forceRefresh) {
            var existing = analysisRepository.findLatestByVideoId(videoId);
            if (existing.isPresent()) {
                return VideoLearningAnalysisResponse.from(existing.get(), true);
            }
        }

        Transcript transcript = transcriptRepository.findFirstByVideoId(videoId)
                .orElseThrow(() -> new VideoTranscriptNotFoundException(videoId));

        VideoLearningAnalysis analysis = analysisRepository.save(analysisGenerator.analyze(transcript));
        return VideoLearningAnalysisResponse.from(analysis, false);
    }
}
