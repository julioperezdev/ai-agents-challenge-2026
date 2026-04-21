package com.aichallenge.agents.commitsummarizer.application;

import com.aichallenge.agents.commitsummarizer.domain.model.Commit;
import com.aichallenge.agents.commitsummarizer.domain.model.DailySummary;
import com.aichallenge.agents.commitsummarizer.domain.model.SummaryResult;
import com.aichallenge.agents.commitsummarizer.domain.service.CommitSummaryService;
import com.aichallenge.agents.commitsummarizer.infrastructure.GitCommitReader;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class GenerateSummaryUseCase {
    private final GitCommitReader gitCommitReader;
    private final CommitSummaryService commitSummaryService;
    private final DailyWorkSummarizer dailyWorkSummarizer;

    public GenerateSummaryUseCase(
        GitCommitReader gitCommitReader,
        CommitSummaryService commitSummaryService,
        DailyWorkSummarizer dailyWorkSummarizer
    ) {
        this.gitCommitReader = gitCommitReader;
        this.commitSummaryService = commitSummaryService;
        this.dailyWorkSummarizer = dailyWorkSummarizer;
    }

    public SummaryResult execute(GenerateSummaryRequest request) throws IOException, InterruptedException {
        if (request.dateFrom().isAfter(request.dateTo())) {
            throw new IllegalArgumentException("La fecha inicial no puede ser posterior a la fecha final.");
        }

        Path repoPath = request.repoPath().toAbsolutePath().normalize();
        String author = request.author();
        if (author == null || author.isBlank()) {
            author = gitCommitReader.detectAuthor(repoPath);
        }
        if (author == null || author.isBlank()) {
            throw new IllegalArgumentException("No se pudo resolver el autor. Usa --author o configura git user.name/user.email.");
        }

        List<Commit> commits = gitCommitReader.readCommits(repoPath, author, request.dateFrom(), request.dateTo());
        Map<java.time.LocalDate, List<Commit>> grouped = commitSummaryService.groupByDay(commits);

        List<DailySummary> dailySummaries = new ArrayList<>();
        for (Map.Entry<java.time.LocalDate, List<Commit>> entry : grouped.entrySet()) {
            List<String> summaryLines = "raw".equals(request.outputFormat())
                ? List.of()
                : dailyWorkSummarizer.summarize(new DailyWorkSummaryRequest(
                    entry.getKey(),
                    List.of(),
                    entry.getValue().stream()
                        .map(commit -> commitSummaryService.cleanCommitMessage(commit.message()))
                        .toList(),
                    false
                ));

            dailySummaries.add(new DailySummary(
                entry.getKey(),
                entry.getValue(),
                summaryLines
            ));
        }

        return new SummaryResult(author, repoPath, request.dateFrom(), request.dateTo(), dailySummaries);
    }
}
