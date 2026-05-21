package com.aichallenge.agents.voicecompanion.infrastructure.ai;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "openai")
public record OpenAIRealtimeProperties(
    String apiKey,
    Realtime realtime
) {
  public record Realtime(String model, String voice, String reasoningEffort) {}

  public String requiredApiKey() {
    if (apiKey == null || apiKey.isBlank()) {
      throw new IllegalStateException("OPENAI_API_KEY is required to start a Realtime session.");
    }
    return apiKey;
  }

  public String model() {
    return realtime != null && realtime.model() != null && !realtime.model().isBlank()
        ? realtime.model()
        : "gpt-realtime-2";
  }

  public String voice() {
    return realtime != null && realtime.voice() != null && !realtime.voice().isBlank()
        ? realtime.voice()
        : "marin";
  }

  public String reasoningEffort() {
    return realtime != null && realtime.reasoningEffort() != null && !realtime.reasoningEffort().isBlank()
        ? realtime.reasoningEffort()
        : "low";
  }
}
