package com.aichallenge.agents.youtubetranscript.infrastructure.provider;

import com.aichallenge.agents.youtubetranscript.domain.ProviderTranscript;
import com.aichallenge.agents.youtubetranscript.domain.ProviderTranscriptSegment;
import com.aichallenge.agents.youtubetranscript.domain.TranscriptProxyUsage;
import com.aichallenge.agents.youtubetranscript.domain.TranscriptStatus;
import com.aichallenge.agents.youtubetranscript.domain.port.TranscriptProvider;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Optional;

@Component
public class PythonTranscriptProvider implements TranscriptProvider {

    private final RestTemplate restTemplate;
    private final TranscriptProviderProperties properties;
    private final ObjectMapper objectMapper;

    public PythonTranscriptProvider(
            RestTemplate transcriptProviderRestTemplate,
            TranscriptProviderProperties properties,
            ObjectMapper objectMapper
    ) {
        this.restTemplate = transcriptProviderRestTemplate;
        this.properties = properties;
        this.objectMapper = objectMapper;
    }

    @Override
    public ProviderTranscript getTranscript(String videoId, List<String> preferredLanguages) {
        try {
            PythonTranscriptResponse response = restTemplate.postForObject(
                    properties.baseUrl() + "/internal/youtube/transcripts",
                    new PythonTranscriptRequest(videoId, preferredLanguages),
                    PythonTranscriptResponse.class
            );

            if (response == null) {
                return ProviderTranscript.failed(TranscriptStatus.PROVIDER_ERROR, videoId, TranscriptProxyUsage.empty(), "Transcript provider returned an empty response");
            }

            if (response.status() != TranscriptStatus.TRANSCRIPT_FOUND) {
                return ProviderTranscript.failed(response.status(), videoId, toProxyUsage(response.proxyUsage()), response.reason());
            }

            return ProviderTranscript.found(
                    response.videoId(),
                    response.language(),
                    Boolean.TRUE.equals(response.isGenerated()),
                    response.languageDetectionMethod(),
                    Boolean.TRUE.equals(response.languageFallbackUsed()),
                    Optional.ofNullable(response.segments()).orElse(List.of()).stream()
                            .map(segment -> new ProviderTranscriptSegment(segment.start(), segment.duration(), segment.text()))
                            .toList(),
                    toProxyUsage(response.proxyUsage())
            );
        } catch (RestClientException ex) {
            return ProviderTranscript.failed(TranscriptStatus.PROVIDER_ERROR, videoId, TranscriptProxyUsage.empty(), ex.getMessage());
        }
    }

    private TranscriptProxyUsage toProxyUsage(PythonProxyUsageResponse response) {
        if (response == null) {
            return TranscriptProxyUsage.empty();
        }
        return new TranscriptProxyUsage(
                response.route() == null ? "unknown" : response.route(),
                response.requestCount() == null ? 0 : response.requestCount(),
                response.requestBytes() == null ? 0L : response.requestBytes(),
                response.responseBytes() == null ? 0L : response.responseBytes(),
                response.totalBytes() == null ? 0L : response.totalBytes(),
                response.totalMb() == null ? 0.0 : response.totalMb(),
                response.proxyPricePerGbUsd() == null ? 0.0 : response.proxyPricePerGbUsd(),
                response.estimatedProxyCostUsd() == null ? 0.0 : response.estimatedProxyCostUsd(),
                toJson(response.httpStatuses()),
                response.elapsedSeconds() == null ? 0.0 : response.elapsedSeconds()
        );
    }

    private String toJson(Object value) {
        try {
            return objectMapper.writeValueAsString(value == null ? java.util.Map.of() : value);
        } catch (JsonProcessingException ex) {
            return "{}";
        }
    }
}
