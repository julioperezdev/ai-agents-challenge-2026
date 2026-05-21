package com.aichallenge.agents.voicecompanion.application;

import static org.assertj.core.api.Assertions.assertThat;

import com.aichallenge.agents.voicecompanion.domain.OlderAdultProfile;
import com.aichallenge.agents.voicecompanion.domain.Reminder;
import com.aichallenge.agents.voicecompanion.infrastructure.ai.OpenAIRealtimeGateway;
import com.aichallenge.agents.voicecompanion.infrastructure.ai.OpenAIRealtimeProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import org.junit.jupiter.api.Test;

class RealtimeSessionServiceTest {
  @Test
  void createsRealtimeCallThroughGatewayWithSessionConfig() {
    CapturingGateway gateway = new CapturingGateway();
    var service = new RealtimeSessionService(
        gateway,
        new OpenAIRealtimeProperties("test-key", new OpenAIRealtimeProperties.Realtime("gpt-realtime-2", "marin", "low")),
        new RealtimePromptBuilder(),
        new ObjectMapper()
    );
    OlderAdultProfile profile = new OlderAdultProfile(
        new OlderAdultProfile.User("Roberto", "Don Roberto", "es-AR"),
        List.of(),
        List.of(new Reminder("r1", "Visita con el medico", "2026-05-15", "16:00", "appointment"))
    );

    String answer = service.createCall("v=0", profile);

    assertThat(answer).isEqualTo("answer-sdp");
    assertThat(gateway.offerSdp).isEqualTo("v=0");
    assertThat(gateway.sessionConfigJson).contains("\"model\":\"gpt-realtime-2\"");
    assertThat(gateway.sessionConfigJson).contains("\"voice\":\"marin\"");
    assertThat(gateway.sessionConfigJson).contains("\"reasoning\":{\"effort\":\"low\"}");
    assertThat(gateway.sessionConfigJson).contains("Visita con el medico");
  }

  private static class CapturingGateway implements OpenAIRealtimeGateway {
    private String offerSdp;
    private String sessionConfigJson;

    @Override
    public String createCall(String offerSdp, String sessionConfigJson) {
      this.offerSdp = offerSdp;
      this.sessionConfigJson = sessionConfigJson;
      return "answer-sdp";
    }
  }
}
