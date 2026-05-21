package com.aichallenge.agents.voicecompanion.infrastructure.input.web;

import com.aichallenge.agents.voicecompanion.application.RealtimeSessionService;
import com.aichallenge.agents.voicecompanion.infrastructure.ai.OpenAIRealtimeProperties;
import java.io.IOException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/realtime")
public class RealtimeController {
  private final RealtimeSessionService realtimeSessionService;
  private final OpenAIRealtimeProperties properties;

  public RealtimeController(RealtimeSessionService realtimeSessionService, OpenAIRealtimeProperties properties) {
    this.realtimeSessionService = realtimeSessionService;
    this.properties = properties;
  }

  @GetMapping("/config")
  public RealtimeConfig config() {
    return new RealtimeConfig(
        properties.model(),
        properties.voice(),
        properties.reasoningEffort(),
        properties.apiKey() != null && !properties.apiKey().isBlank()
    );
  }

  @PostMapping(
      value = "/session",
      consumes = { "application/sdp", MediaType.TEXT_PLAIN_VALUE },
      produces = "application/sdp"
  )
  public String createSession(@RequestBody String offerSdp) throws IOException {
    return realtimeSessionService.createDefaultCall(offerSdp);
  }

  public record RealtimeConfig(String model, String voice, String reasoningEffort, boolean apiKeyConfigured) {}
}
