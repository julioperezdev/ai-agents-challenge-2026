package com.aichallenge.agents.voicecompanion.application;

import com.aichallenge.agents.voicecompanion.domain.Reminder;
import java.text.Normalizer;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

public class ReminderResolver {
  private static final Set<String> INTENT_WORDS = Set.of(
      "recordatorio", "recordar", "tengo", "viernes", "visita", "llamar", "turno"
  );

  public List<Reminder> findRelevant(String text, List<Reminder> reminders) {
    String normalized = normalize(text);
    boolean hasIntent = INTENT_WORDS.stream().anyMatch(normalized::contains);
    if (!hasIntent) {
      return List.of();
    }

    List<Reminder> directMatches = reminders.stream()
        .filter(reminder -> normalize(reminder.title() + " " + reminder.type() + " " + reminder.date() + " " + reminder.time())
            .lines()
            .flatMap(line -> List.of(line.split("\\s+")).stream())
            .anyMatch(word -> word.length() > 3 && normalized.contains(word)))
        .sorted(Comparator.comparing(reminder -> reminder.date() + "T" + reminder.time()))
        .toList();

    if (!directMatches.isEmpty()) {
      return directMatches;
    }

    return reminders.stream()
        .sorted(Comparator.comparing(reminder -> reminder.date() + "T" + reminder.time()))
        .limit(2)
        .toList();
  }

  private static String normalize(String value) {
    String withoutAccents = Normalizer.normalize(value, Normalizer.Form.NFD).replaceAll("\\p{M}", "");
    return withoutAccents.toLowerCase();
  }
}
