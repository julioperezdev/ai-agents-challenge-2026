package com.aichallenge.agents.youtubetranscript.application;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class IngestYoutubeLearningVideoUseCase {

    private final GetYoutubeTranscriptUseCase getYoutubeTranscriptUseCase;

    public IngestYoutubeLearningVideoUseCase(GetYoutubeTranscriptUseCase getYoutubeTranscriptUseCase) {
        this.getYoutubeTranscriptUseCase = getYoutubeTranscriptUseCase;
    }

    @Transactional
    public YoutubeLearningVideoIngestionResponse execute(YoutubeLearningVideoIngestionRequest request) {
        YoutubeTranscriptResponse transcriptResponse = getYoutubeTranscriptUseCase.execute(request.toTranscriptRequest());
        return YoutubeLearningVideoIngestionResponse.fromTranscript(transcriptResponse, request.url());
    }
}
