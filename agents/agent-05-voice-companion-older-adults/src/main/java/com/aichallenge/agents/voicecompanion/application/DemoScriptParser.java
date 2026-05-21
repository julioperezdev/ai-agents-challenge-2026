package com.aichallenge.agents.voicecompanion.application;

import com.aichallenge.agents.voicecompanion.domain.TranscriptTurn;
import java.util.List;
import java.util.regex.Pattern;

public class DemoScriptParser {
  private static final Pattern USER_LINE = Pattern.compile("^(Persona|Usuario|User):\\s*", Pattern.CASE_INSENSITIVE);

  public List<TranscriptTurn> parse(String markdown) {
    List<TranscriptTurn> turns = markdown.lines()
        .map(String::trim)
        .filter(line -> USER_LINE.matcher(line).find())
        .map(line -> new TranscriptTurn("user", USER_LINE.matcher(line).replaceFirst("").trim()))
        .filter(turn -> !turn.text().isBlank())
        .toList();

    if (turns.isEmpty()) {
      throw new IllegalArgumentException("Demo script must include at least one line starting with 'Persona:'.");
    }
    return turns;
  }
}
