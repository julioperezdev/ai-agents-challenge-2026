package com.aichallenge.agents.voicecompanion.application;

import com.aichallenge.agents.voicecompanion.domain.ConversationSummary;
import com.aichallenge.agents.voicecompanion.domain.OlderAdultProfile;
import com.aichallenge.agents.voicecompanion.domain.Reminder;
import com.aichallenge.agents.voicecompanion.domain.SafetySignal;
import com.aichallenge.agents.voicecompanion.domain.TranscriptTurn;
import java.text.Normalizer;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import org.springframework.stereotype.Service;

@Service
public class ConversationSummaryService {
  private final SafetySignalDetector safetySignalDetector = new SafetySignalDetector();

  public ConversationSummary generate(
      OlderAdultProfile profile,
      List<TranscriptTurn> transcript,
      List<Reminder> remindersConsulted
  ) {
    String userText = transcript.stream()
        .filter(turn -> "user".equals(turn.speaker()))
        .map(TranscriptTurn::text)
        .reduce("", (left, right) -> left + " " + right);
    List<SafetySignal> safetySignals = safetySignalDetector.detect(userText);

    return new ConversationSummary(
        profile.user().preferredName(),
        LocalDate.now().toString(),
        safetySignals.stream().anyMatch(signal -> "urgent".equals(signal.level()))
            ? "La persona menciono una senal que requiere contactar ayuda inmediata."
            : "La persona converso de forma tranquila y participativa.",
        inferTopics(userText, remindersConsulted),
        remindersConsulted,
        safetySignals,
        suggestedActions(profile, remindersConsulted, safetySignals),
        true,
        transcript
    );
  }

  private List<String> inferTopics(String userText, List<Reminder> remindersConsulted) {
    String normalized = normalize(userText);
    Set<String> topics = new LinkedHashSet<>();
    if (normalized.matches(".*(foto|niet|famil|hij|laura).*")) topics.add("recuerdos familiares");
    if (normalized.matches(".*(chiste|rio|risa|humor).*")) topics.add("humor");
    if (!remindersConsulted.isEmpty() || normalized.matches(".*(visita|turno|recordatorio|tengo).*")) {
      topics.add("consulta sobre recordatorios");
    }
    if (normalized.matches(".*(gracias|tranquil).*")) topics.add("cierre tranquilo");
    if (topics.isEmpty()) topics.add("conversacion cotidiana");
    return List.copyOf(topics);
  }

  private List<String> suggestedActions(
      OlderAdultProfile profile,
      List<Reminder> remindersConsulted,
      List<SafetySignal> safetySignals
  ) {
    if (safetySignals.stream().anyMatch(signal -> "urgent".equals(signal.level()))) {
      return List.of("Contactar servicios de emergencia o una persona de confianza inmediatamente.");
    }

    List<String> actions = new ArrayList<>();
    remindersConsulted.forEach(reminder ->
        actions.add("Confirmar que " + profile.user().preferredName() + " recuerde: " + reminder.title() + "."));
    if (!profile.familyContacts().isEmpty()) {
      actions.add("Mantener una llamada breve con " + profile.familyContacts().get(0).name() + " durante el dia.");
    }
    actions.add("Mantener los recordatorios actualizados.");
    return actions;
  }

  private static String normalize(String value) {
    return Normalizer.normalize(value, Normalizer.Form.NFD).replaceAll("\\p{M}", "").toLowerCase();
  }
}
