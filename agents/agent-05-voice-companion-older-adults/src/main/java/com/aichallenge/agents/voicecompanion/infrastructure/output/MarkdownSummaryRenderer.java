package com.aichallenge.agents.voicecompanion.infrastructure.output;

import com.aichallenge.agents.voicecompanion.domain.ConversationSummary;
import com.aichallenge.agents.voicecompanion.domain.Reminder;
import com.aichallenge.agents.voicecompanion.domain.SafetySignal;
import com.aichallenge.agents.voicecompanion.domain.TranscriptTurn;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;

@Component
public class MarkdownSummaryRenderer {
  public String render(ConversationSummary summary) {
    return """
        # Resumen de conversacion

        ## Usuario
        %s

        ## Fecha
        %s

        ## Estado general observado
        %s

        ## Temas mencionados
        %s

        ## Recordatorios consultados
        %s

        ## Senales relevantes
        %s

        ## Posibles acciones sugeridas
        %s

        ## Transcripcion demo
        %s

        ## Nota de seguridad
        Este resumen no constituye evaluacion medica, diagnostico ni recomendacion clinica.
        """.formatted(
        summary.user(),
        summary.date(),
        summary.generalState(),
        summary.topics().stream().map(topic -> "- " + topic + ".").collect(Collectors.joining("\n")),
        renderReminders(summary),
        renderSafety(summary),
        summary.suggestedActions().stream().map(action -> "- " + action).collect(Collectors.joining("\n")),
        summary.transcript().stream().map(this::renderTurn).collect(Collectors.joining("\n"))
    );
  }

  private String renderReminders(ConversationSummary summary) {
    if (summary.remindersConsulted().isEmpty()) {
      return "No se consultaron recordatorios configurados.";
    }
    return summary.remindersConsulted().stream().map(this::renderReminder).collect(Collectors.joining("\n"));
  }

  private String renderReminder(Reminder reminder) {
    return "- " + reminder.title() + " el " + reminder.date() + " a las " + reminder.time() + ".";
  }

  private String renderSafety(ConversationSummary summary) {
    if (summary.safetySignals().isEmpty()) {
      return "No se detectaron menciones explicitas de emergencia o riesgo.";
    }
    return summary.safetySignals().stream().map(this::renderSignal).collect(Collectors.joining("\n"));
  }

  private String renderSignal(SafetySignal signal) {
    return "- " + signal.phrase() + ": " + signal.guidance();
  }

  private String renderTurn(TranscriptTurn turn) {
    String speaker = "user".equals(turn.speaker()) ? "Persona" : "Agente";
    return "- **" + speaker + ":** " + turn.text();
  }
}
