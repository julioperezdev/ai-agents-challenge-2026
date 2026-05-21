package com.aichallenge.agents.voicecompanion.application;

import static org.assertj.core.api.Assertions.assertThat;

import com.aichallenge.agents.voicecompanion.domain.OlderAdultProfile;
import com.aichallenge.agents.voicecompanion.domain.Reminder;
import java.util.List;
import org.junit.jupiter.api.Test;

class RealtimePromptBuilderTest {
  @Test
  void includesSafetyRulesAndConfiguredReminders() {
    OlderAdultProfile profile = new OlderAdultProfile(
        new OlderAdultProfile.User("Roberto", "Don Roberto", "es-AR"),
        List.of(),
        List.of(new Reminder("r1", "Llamar a Laura", "2026-05-15", "19:00", "family_call"))
    );

    String prompt = new RealtimePromptBuilder().build(profile);

    assertThat(prompt).contains("Don Roberto");
    assertThat(prompt).contains("Do not provide medical diagnosis");
    assertThat(prompt).contains("Llamar a Laura");
    assertThat(prompt).contains("Do not invent reminders");
  }
}
