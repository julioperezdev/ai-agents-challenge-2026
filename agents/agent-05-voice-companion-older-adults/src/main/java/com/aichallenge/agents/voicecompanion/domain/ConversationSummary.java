package com.aichallenge.agents.voicecompanion.domain;

import java.util.List;

public record ConversationSummary(
    String user,
    String date,
    String generalState,
    List<String> topics,
    List<Reminder> remindersConsulted,
    List<SafetySignal> safetySignals,
    List<String> suggestedActions,
    boolean medicalDisclaimer,
    List<TranscriptTurn> transcript
) {}
