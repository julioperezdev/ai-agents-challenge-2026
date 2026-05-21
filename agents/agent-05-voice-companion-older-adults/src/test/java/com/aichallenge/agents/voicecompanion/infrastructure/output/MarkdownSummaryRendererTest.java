package com.aichallenge.agents.voicecompanion.infrastructure.output;

import static org.assertj.core.api.Assertions.assertThat;

import com.aichallenge.agents.voicecompanion.domain.ConversationSummary;
import com.aichallenge.agents.voicecompanion.domain.Reminder;
import com.aichallenge.agents.voicecompanion.domain.TranscriptTurn;
import java.util.List;
import org.junit.jupiter.api.Test;

class MarkdownSummaryRendererTest {
  @Test
  void rendersSafetyDisclaimerAndReminderSections() {
    var summary = new ConversationSummary(
        "Don Roberto",
        "2026-05-15",
        "La persona converso de forma tranquila y participativa.",
        List.of("humor"),
        List.of(new Reminder("r1", "Llamar a Laura", "2026-05-15", "19:00", "family_call")),
        List.of(),
        List.of("Mantener los recordatorios actualizados."),
        true,
        List.of(new TranscriptTurn("user", "Hola"))
    );

    String markdown = new MarkdownSummaryRenderer().render(summary);

    assertThat(markdown).contains("# Resumen de conversacion");
    assertThat(markdown).contains("## Recordatorios consultados");
    assertThat(markdown).contains("Llamar a Laura");
    assertThat(markdown).contains("no constituye evaluacion medica");
  }
}
