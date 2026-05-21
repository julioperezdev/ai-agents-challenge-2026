package com.aichallenge.agents.voicecompanion.application;

import com.aichallenge.agents.voicecompanion.domain.SafetySignal;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class SafetySignalDetector {
  private static final Map<String, String> URGENT = Map.of(
      "caida", "Contactar servicios de emergencia o una persona de confianza inmediatamente.",
      "dolor de pecho", "Contactar servicios de emergencia o una persona de confianza inmediatamente.",
      "dificultad para respirar", "Contactar servicios de emergencia o una persona de confianza inmediatamente.",
      "emergencia", "Contactar servicios de emergencia o una persona de confianza inmediatamente."
  );

  private static final Map<String, String> WATCH = Map.of(
      "triste", "Responder con empatia y sugerir contacto con una persona de confianza.",
      "solo", "Responder con empatia y sugerir contacto con una persona de confianza.",
      "angustiado", "Responder con empatia y sugerir contacto con una persona de confianza."
  );

  public List<SafetySignal> detect(String text) {
    String normalized = normalize(text);
    List<SafetySignal> signals = new ArrayList<>();
    URGENT.forEach((phrase, guidance) -> {
      if (normalized.contains(phrase)) {
        signals.add(new SafetySignal("urgent", phrase, guidance));
      }
    });
    WATCH.forEach((phrase, guidance) -> {
      if (normalized.contains(phrase)) {
        signals.add(new SafetySignal("watch", phrase, guidance));
      }
    });
    return signals;
  }

  private static String normalize(String value) {
    return Normalizer.normalize(value, Normalizer.Form.NFD).replaceAll("\\p{M}", "").toLowerCase();
  }
}
