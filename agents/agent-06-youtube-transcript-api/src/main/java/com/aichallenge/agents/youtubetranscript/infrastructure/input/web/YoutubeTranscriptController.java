package com.aichallenge.agents.youtubetranscript.infrastructure.input.web;

import com.aichallenge.agents.youtubetranscript.application.GetYoutubeTranscriptUseCase;
import com.aichallenge.agents.youtubetranscript.application.YoutubeTranscriptRequest;
import com.aichallenge.agents.youtubetranscript.application.YoutubeTranscriptResponse;
import com.aichallenge.agents.youtubetranscript.domain.TranscriptStatus;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/youtube/transcripts")
@Tag(name = "YouTube Transcripts", description = "Operations to fetch and store YouTube video transcripts")
public class YoutubeTranscriptController {

    private final GetYoutubeTranscriptUseCase getYoutubeTranscriptUseCase;

    public YoutubeTranscriptController(GetYoutubeTranscriptUseCase getYoutubeTranscriptUseCase) {
        this.getYoutubeTranscriptUseCase = getYoutubeTranscriptUseCase;
    }

    @Operation(summary = "Get YouTube video transcript")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Transcript found"),
            @ApiResponse(responseCode = "400", description = "Invalid YouTube URL"),
            @ApiResponse(responseCode = "422", description = "Transcript not available or video unavailable"),
            @ApiResponse(responseCode = "429", description = "Transcript provider blocked requests"),
            @ApiResponse(responseCode = "502", description = "Transcript provider error")
    })
    @PostMapping
    public ResponseEntity<YoutubeTranscriptResponse> getTranscript(@Valid @RequestBody YoutubeTranscriptRequest request) {
        YoutubeTranscriptResponse response = getYoutubeTranscriptUseCase.execute(request);
        return ResponseEntity.status(statusFor(response.status())).body(response);
    }

    private HttpStatus statusFor(TranscriptStatus status) {
        return switch (status) {
            case TRANSCRIPT_FOUND -> HttpStatus.OK;
            case INVALID_YOUTUBE_URL -> HttpStatus.BAD_REQUEST;
            case TRANSCRIPT_NOT_AVAILABLE, VIDEO_UNAVAILABLE -> HttpStatus.UNPROCESSABLE_ENTITY;
            case TRANSCRIPT_PROVIDER_BLOCKED -> HttpStatus.TOO_MANY_REQUESTS;
            case PROVIDER_ERROR -> HttpStatus.BAD_GATEWAY;
        };
    }
}
