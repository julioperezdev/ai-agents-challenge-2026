package com.aichallenge.agents.commitsummarizer.presentation;

import com.aichallenge.agents.commitsummarizer.domain.model.Commit;
import com.aichallenge.agents.commitsummarizer.domain.model.DailySummary;
import com.aichallenge.agents.commitsummarizer.domain.model.SummaryResult;
import com.aichallenge.agents.commitsummarizer.domain.service.CommitSummaryService;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;

class SummaryRendererTest {
    @Test
    void rendersTimeTrackerOutput() {
        SummaryRenderer renderer = new SummaryRenderer(new CommitSummaryService());
        Commit commit = new Commit(
            "1234567890abcdef",
            "1234567",
            OffsetDateTime.parse("2026-04-13T09:00:00-03:00"),
            "feat: office sync logic"
        );
        SummaryResult result = new SummaryResult(
            "Julio Perez",
            Path.of("."),
            LocalDate.of(2026, 4, 13),
            LocalDate.of(2026, 4, 13),
            List.of(new DailySummary(
                LocalDate.of(2026, 4, 13),
                List.of(commit),
                List.of("Implementación de mejoras en la lógica de sincronización de oficinas.")
            ))
        );

        String output = renderer.render(result, "time-tracker");

        assertTrue(output.contains("13 de abril de 2026"));
        assertTrue(output.contains("- Implementación de mejoras en la lógica de sincronización de oficinas."));
    }
}
