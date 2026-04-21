package com.aichallenge.agents.commitsummarizer.application;

import java.time.LocalDate;
import java.util.List;

public record DailyWorkSummaryRequest(
    LocalDate date,
    List<String> meetings,
    List<String> activityLines,
    boolean multiRepo
) {
}
