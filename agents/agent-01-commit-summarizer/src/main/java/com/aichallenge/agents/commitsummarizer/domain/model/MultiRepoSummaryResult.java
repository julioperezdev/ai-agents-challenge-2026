package com.aichallenge.agents.commitsummarizer.domain.model;

import java.time.LocalDate;
import java.util.List;

public record MultiRepoSummaryResult(
    LocalDate dateFrom,
    LocalDate dateTo,
    List<DailyEntry> days,
    boolean multiRepo
) {
}
