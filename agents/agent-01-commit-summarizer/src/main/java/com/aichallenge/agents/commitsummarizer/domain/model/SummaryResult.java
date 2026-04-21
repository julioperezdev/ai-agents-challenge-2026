package com.aichallenge.agents.commitsummarizer.domain.model;

import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;

public record SummaryResult(
    String author,
    Path repoPath,
    LocalDate dateFrom,
    LocalDate dateTo,
    List<DailySummary> days
) {
}
