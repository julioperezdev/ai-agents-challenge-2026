package com.aichallenge.agents.commitsummarizer.presentation;

import com.aichallenge.agents.commitsummarizer.domain.model.DailyEntry;
import com.aichallenge.agents.commitsummarizer.domain.model.MultiRepoSummaryResult;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;

class MultiRepoSummaryRendererTest {
    @Test
    void rendersAiSummaryLinesAsBullets() {
        MultiRepoSummaryRenderer renderer = new MultiRepoSummaryRenderer();
        MultiRepoSummaryResult result = new MultiRepoSummaryResult(
            LocalDate.of(2026, 3, 16),
            LocalDate.of(2026, 3, 20),
            List.of(
                new DailyEntry(
                    LocalDate.of(2026, 3, 16),
                    List.of("Daily"),
                    List.of(new DailyEntry.RepoCommitLine("repo-a", "update flow")),
                    List.of("Ajustes en el flujo principal.", "Correcciones en validaciones.")
                )
            ),
            true
        );

        String output = renderer.render(result);

        assertTrue(output.contains("- Ajustes en el flujo principal."));
        assertTrue(output.contains("- Correcciones en validaciones."));
    }
}
