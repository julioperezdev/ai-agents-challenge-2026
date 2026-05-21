package com.aichallenge.agents.voicecompanion.application;

import static org.assertj.core.api.Assertions.assertThat;

import com.aichallenge.agents.voicecompanion.domain.OlderAdultProfile;
import com.aichallenge.agents.voicecompanion.domain.Reminder;
import java.util.List;
import org.junit.jupiter.api.Test;

class LocalCompanionServiceTest {
  @Test
  void runsDemoAndRegistersReminderLookup() {
    OlderAdultProfile profile = new OlderAdultProfile(
        new OlderAdultProfile.User("Roberto", "Don Roberto", "es-AR"),
        List.of(),
        List.of(new Reminder("r1", "Llamar a Laura", "2026-05-15", "19:00", "family_call"))
    );

    var conversation = new LocalCompanionService().run(profile, "Persona: Que tengo que recordar hoy?");

    assertThat(conversation.transcript()).hasSize(2);
    assertThat(conversation.remindersConsulted()).extracting(Reminder::title).containsExactly("Llamar a Laura");
    assertThat(conversation.transcript().get(1).text()).contains("Llamar a Laura");
  }
}
