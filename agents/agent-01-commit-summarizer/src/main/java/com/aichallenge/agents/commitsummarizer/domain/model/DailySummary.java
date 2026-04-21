package com.aichallenge.agents.commitsummarizer.domain.model;

import java.time.LocalDate;
import java.util.List;

public record DailySummary(
    LocalDate day,
    List<Commit> commits,
    List<String> summaryLines
) {
}
