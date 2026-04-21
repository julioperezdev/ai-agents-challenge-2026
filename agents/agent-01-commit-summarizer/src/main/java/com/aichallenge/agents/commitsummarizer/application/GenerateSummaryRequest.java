package com.aichallenge.agents.commitsummarizer.application;

import java.nio.file.Path;
import java.time.LocalDate;

public record GenerateSummaryRequest(
    String author,
    LocalDate dateFrom,
    LocalDate dateTo,
    Path repoPath,
    String outputFormat
) {
}
