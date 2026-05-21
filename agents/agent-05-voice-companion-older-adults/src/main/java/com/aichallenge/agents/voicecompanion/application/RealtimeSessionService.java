package com.aichallenge.agents.voicecompanion.application;

import com.aichallenge.agents.voicecompanion.domain.OlderAdultProfile;
import com.aichallenge.agents.voicecompanion.infrastructure.ai.OpenAIRealtimeGateway;
import com.aichallenge.agents.voicecompanion.infrastructure.ai.OpenAIRealtimeProperties;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;

@Service
public class RealtimeSessionService {
  private final OpenAIRealtimeGateway gateway;
  private final OpenAIRealtimeProperties properties;
  private final RealtimePromptBuilder promptBuilder;
  private final ObjectMapper objectMapper;

  public RealtimeSessionService(
      OpenAIRealtimeGateway gateway,
      OpenAIRealtimeProperties properties,
      RealtimePromptBuilder promptBuilder,
      ObjectMapper objectMapper
  ) {
    this.gateway = gateway;
    this.properties = properties;
    this.promptBuilder = promptBuilder;
    this.objectMapper = objectMapper;
  }

  public String createDefaultCall(String offerSdp) throws IOException {
    OlderAdultProfile profile = objectMapper.readValue(
        Files.readString(Path.of("examples/older-adult-profile.example.json")),
        OlderAdultProfile.class
    );
    return createCall(offerSdp, profile);
  }

  public String createCall(String offerSdp, OlderAdultProfile profile) {
    if (offerSdp == null || offerSdp.isBlank()) {
      throw new IllegalArgumentException("SDP offer must not be blank.");
    }
    return gateway.createCall(offerSdp, sessionConfig(profile));
  }

  String sessionConfig(OlderAdultProfile profile) {
    Map<String, Object> session = Map.of(
        "type", "realtime",
        "model", properties.model(),
        "instructions", promptBuilder.build(profile),
        "reasoning", Map.of(
            "effort", properties.reasoningEffort()
        ),
        "output_modalities", List.of("audio"),
        "max_output_tokens", 800,
        "audio", Map.of(
            "input", Map.of(
                "turn_detection", Map.of(
                    "type", "server_vad",
                    "threshold", 0.45,
                    "prefix_padding_ms", 300,
                    "silence_duration_ms", 700,
                    "idle_timeout_ms", 6000,
                    "create_response", true,
                    "interrupt_response", true
                ),
                "noise_reduction", Map.of(
                    "type", "near_field"
                ),
                "transcription", Map.of(
                    "model", "gpt-4o-mini-transcribe",
                    "language", "es"
                )
            ),
            "output", Map.of(
                "voice", properties.voice()
            )
        )
    );

    try {
      return objectMapper.writeValueAsString(session);
    } catch (JsonProcessingException exception) {
      throw new IllegalStateException("Could not serialize Realtime session config.", exception);
    }
  }
}
