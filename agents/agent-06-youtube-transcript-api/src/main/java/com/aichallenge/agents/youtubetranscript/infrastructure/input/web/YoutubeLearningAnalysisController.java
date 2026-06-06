package com.aichallenge.agents.youtubetranscript.infrastructure.input.web;

import com.aichallenge.agents.youtubetranscript.application.AnalyzeYoutubeLearningVideoRequest;
import com.aichallenge.agents.youtubetranscript.application.AnalyzeYoutubeLearningVideoUseCase;
import com.aichallenge.agents.youtubetranscript.application.VideoLearningAnalysisResponse;
import com.aichallenge.agents.youtubetranscript.application.VideoTranscriptNotFoundException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/learning/youtube/videos/{videoId}/analysis")
@Tag(name = "YouTube Learning Analysis", description = "Analyze ingested YouTube videos for personal learning")
public class YoutubeLearningAnalysisController {

    private final AnalyzeYoutubeLearningVideoUseCase analyzeYoutubeLearningVideoUseCase;

    public YoutubeLearningAnalysisController(AnalyzeYoutubeLearningVideoUseCase analyzeYoutubeLearningVideoUseCase) {
        this.analyzeYoutubeLearningVideoUseCase = analyzeYoutubeLearningVideoUseCase;
    }

    @Operation(summary = "Create or retrieve a learning analysis for an ingested YouTube video")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Analysis created or retrieved"),
            @ApiResponse(responseCode = "404", description = "Transcript not found for video")
    })
    @PostMapping
    public VideoLearningAnalysisResponse analyzeVideo(
            @PathVariable String videoId,
            @RequestBody(required = false) AnalyzeYoutubeLearningVideoRequest request
    ) {
        boolean forceRefresh = request != null && request.shouldForceRefresh();
        return analyzeYoutubeLearningVideoUseCase.execute(videoId, forceRefresh);
    }

    @ExceptionHandler(VideoTranscriptNotFoundException.class)
    ResponseEntity<Map<String, String>> handleTranscriptNotFound(VideoTranscriptNotFoundException exception) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Map.of(
                        "status", "TRANSCRIPT_NOT_FOUND",
                        "reason", exception.getMessage()
                ));
    }
}
