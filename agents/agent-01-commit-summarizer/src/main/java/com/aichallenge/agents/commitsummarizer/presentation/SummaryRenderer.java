package com.aichallenge.agents.commitsummarizer.presentation;

import com.aichallenge.agents.commitsummarizer.domain.model.Commit;
import com.aichallenge.agents.commitsummarizer.domain.model.DailySummary;
import com.aichallenge.agents.commitsummarizer.domain.model.SummaryResult;
import com.aichallenge.agents.commitsummarizer.domain.service.CommitSummaryService;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

public class SummaryRenderer {
    private final CommitSummaryService commitSummaryService;
    private final Map<Integer, String> monthNames = new HashMap<>();

    public SummaryRenderer(CommitSummaryService commitSummaryService) {
        this.commitSummaryService = commitSummaryService;
        monthNames.put(1, "enero");
        monthNames.put(2, "febrero");
        monthNames.put(3, "marzo");
        monthNames.put(4, "abril");
        monthNames.put(5, "mayo");
        monthNames.put(6, "junio");
        monthNames.put(7, "julio");
        monthNames.put(8, "agosto");
        monthNames.put(9, "septiembre");
        monthNames.put(10, "octubre");
        monthNames.put(11, "noviembre");
        monthNames.put(12, "diciembre");
    }

    public String render(SummaryResult result, String outputFormat) {
        if (result.days().isEmpty()) {
            return "No se encontraron commits para '" + result.author() + "' entre "
                + result.dateFrom() + " y " + result.dateTo() + ".";
        }

        return switch (outputFormat) {
            case "raw" -> renderRaw(result);
            case "markdown" -> renderMarkdown(result);
            case "time-tracker" -> renderTimeTracker(result);
            default -> throw new IllegalArgumentException("Formato de salida no soportado: " + outputFormat);
        };
    }

    private String renderTimeTracker(SummaryResult result) {
        StringBuilder builder = new StringBuilder();
        for (DailySummary dailySummary : result.days()) {
            builder.append(formatSpanishDate(dailySummary.day())).append("\n");
            for (String line : dailySummary.summaryLines()) {
                builder.append("- ").append(line).append("\n");
            }
            builder.append("\n");
        }
        return builder.toString().trim();
    }

    private String renderMarkdown(SummaryResult result) {
        StringBuilder builder = new StringBuilder();
        for (DailySummary dailySummary : result.days()) {
            builder.append("# ").append(formatSpanishDate(dailySummary.day())).append("\n");
            for (String line : dailySummary.summaryLines()) {
                builder.append("- ").append(line).append("\n");
            }
            builder.append("\n");
        }
        return builder.toString().trim();
    }

    private String renderRaw(SummaryResult result) {
        StringBuilder builder = new StringBuilder();
        for (DailySummary dailySummary : result.days()) {
            builder.append(dailySummary.day()).append("\n");
            for (Commit commit : dailySummary.commits()) {
                builder.append("- ")
                    .append(commit.shortHash())
                    .append(" ")
                    .append(commitSummaryService.cleanCommitMessage(commit.message()))
                    .append("\n");
            }
            builder.append("\n");
        }
        return builder.toString().trim();
    }

    private String formatSpanishDate(LocalDate date) {
        return date.getDayOfMonth() + " de " + monthNames.get(date.getMonthValue()) + " de " + date.getYear();
    }
}
