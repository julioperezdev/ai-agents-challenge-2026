package com.aichallenge.agents.youtubetranscript.infrastructure.input.web;

import com.aichallenge.agents.youtubetranscript.application.IngestYoutubeLearningVideoUseCase;
import com.aichallenge.agents.youtubetranscript.application.ListYoutubeLearningVideosUseCase;
import com.aichallenge.agents.youtubetranscript.application.YoutubeLearningVideoIngestionRequest;
import com.aichallenge.agents.youtubetranscript.application.YoutubeLearningVideoIngestionResponse;
import com.aichallenge.agents.youtubetranscript.application.YoutubeLearningVideoListItemResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/learning/youtube/videos")
@Tag(name = "YouTube Learning Videos", description = "Ingest YouTube videos into a personal learning library")
public class YoutubeLearningVideoController {

    private final IngestYoutubeLearningVideoUseCase ingestYoutubeLearningVideoUseCase;
    private final ListYoutubeLearningVideosUseCase listYoutubeLearningVideosUseCase;

    public YoutubeLearningVideoController(
            IngestYoutubeLearningVideoUseCase ingestYoutubeLearningVideoUseCase,
            ListYoutubeLearningVideosUseCase listYoutubeLearningVideosUseCase
    ) {
        this.ingestYoutubeLearningVideoUseCase = ingestYoutubeLearningVideoUseCase;
        this.listYoutubeLearningVideosUseCase = listYoutubeLearningVideosUseCase;
    }

    @Operation(summary = "List videos already stored in the personal learning library")
    @GetMapping
    public List<YoutubeLearningVideoListItemResponse> listVideos() {
        return listYoutubeLearningVideosUseCase.execute();
    }

    @Operation(summary = "Ingest a YouTube video for later learning analysis")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Video ingested or already available"),
            @ApiResponse(responseCode = "400", description = "Invalid YouTube URL"),
            @ApiResponse(responseCode = "422", description = "Transcript not available or video unavailable"),
            @ApiResponse(responseCode = "429", description = "Transcript provider blocked requests"),
            @ApiResponse(responseCode = "502", description = "Transcript provider error")
    })
    @PostMapping
    public ResponseEntity<YoutubeLearningVideoIngestionResponse> ingestVideo(
            @Valid @RequestBody YoutubeLearningVideoIngestionRequest request
    ) {
        YoutubeLearningVideoIngestionResponse response = ingestYoutubeLearningVideoUseCase.execute(request);
        return ResponseEntity.status(statusFor(response.status())).body(response);
    }

    private HttpStatus statusFor(String status) {
        return switch (status) {
            case "VIDEO_INGESTED", "VIDEO_ALREADY_INGESTED" -> HttpStatus.OK;
            case "INVALID_YOUTUBE_URL" -> HttpStatus.BAD_REQUEST;
            case "TRANSCRIPT_NOT_AVAILABLE", "VIDEO_UNAVAILABLE" -> HttpStatus.UNPROCESSABLE_ENTITY;
            case "TRANSCRIPT_PROVIDER_BLOCKED" -> HttpStatus.TOO_MANY_REQUESTS;
            case "PROVIDER_ERROR" -> HttpStatus.BAD_GATEWAY;
            default -> HttpStatus.INTERNAL_SERVER_ERROR;
        };
    }
}
