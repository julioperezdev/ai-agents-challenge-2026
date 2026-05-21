package com.aichallenge.agents.voicecompanion.application;

import com.aichallenge.agents.voicecompanion.domain.OlderAdultProfile;
import com.aichallenge.agents.voicecompanion.domain.Reminder;
import com.aichallenge.agents.voicecompanion.domain.TranscriptTurn;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;

@Service
public class LocalCompanionService {
  private final ReminderResolver reminderResolver = new ReminderResolver();

  public DemoConversation run(OlderAdultProfile profile, String demoScript) {
    List<TranscriptTurn> userTurns = new DemoScriptParser().parse(demoScript);
    List<TranscriptTurn> transcript = new ArrayList<>();
    Map<String, Reminder> remindersConsulted = new LinkedHashMap<>();

    for (TranscriptTurn userTurn : userTurns) {
      transcript.add(userTurn);
      List<Reminder> relevantReminders = reminderResolver.findRelevant(userTurn.text(), profile.reminders());
      if (!relevantReminders.isEmpty()) {
        relevantReminders.forEach(reminder -> remindersConsulted.put(reminder.id(), reminder));
      }
      transcript.add(new TranscriptTurn("assistant", respond(profile, userTurn.text(), relevantReminders)));
    }

    return new DemoConversation(transcript, List.copyOf(remindersConsulted.values()));
  }

  private String respond(OlderAdultProfile profile, String text, List<Reminder> relevantReminders) {
    String preferredName = profile.user().preferredName();
    if (!relevantReminders.isEmpty()) {
      String reminderText = relevantReminders.stream()
          .map(reminder -> reminder.title() + " a las " + reminder.time())
          .reduce((left, right) -> left + "; " + right)
          .orElse("");
      return preferredName + ", tiene esto anotado: " + reminderText + ". Se lo digo con calma para que lo tenga presente.";
    }

    String normalized = normalize(text);
    if (normalized.contains("chiste") || normalized.contains("rio") || normalized.contains("risa")) {
      return "Que lindo eso. A veces un chiste sencillo alcanza para alegrar un ratito.";
    }
    if (normalized.contains("gracias") || normalized.contains("tranquil")) {
      return "Me alegra acompanarlo. Estoy aca para conversar y ayudar con recordatorios simples cuando lo necesite.";
    }
    return "Lo escucho, " + preferredName + ". Es lindo recordar esas historias y compartirlas con alguien de confianza.";
  }

  private static String normalize(String value) {
    return Normalizer.normalize(value, Normalizer.Form.NFD).replaceAll("\\p{M}", "").toLowerCase();
  }

  public record DemoConversation(List<TranscriptTurn> transcript, List<Reminder> remindersConsulted) {}
}
