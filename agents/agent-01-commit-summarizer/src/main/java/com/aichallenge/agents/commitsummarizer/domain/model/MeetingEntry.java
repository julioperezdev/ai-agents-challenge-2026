package com.aichallenge.agents.commitsummarizer.domain.model;

import java.time.DayOfWeek;

public record MeetingEntry(
    DayOfWeek dayOfWeek,
    String name
) {
}
