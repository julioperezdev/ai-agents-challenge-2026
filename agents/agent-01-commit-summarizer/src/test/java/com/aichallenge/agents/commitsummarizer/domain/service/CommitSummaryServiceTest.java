package com.aichallenge.agents.commitsummarizer.domain.service;

import com.aichallenge.agents.commitsummarizer.domain.model.Commit;
import org.junit.jupiter.api.Test;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CommitSummaryServiceTest {
    private final CommitSummaryService service = new CommitSummaryService();

    @Test
    void groupsCommitsByDayInAscendingOrder() {
        List<Commit> commits = List.of(
            commit("fix: save flow", "2026-04-14T10:15:00-03:00"),
            commit("feat: office sync logic", "2026-04-13T09:00:00-03:00"),
            commit("refactor: save flow checks", "2026-04-13T15:30:00-03:00")
        );

        Map<java.time.LocalDate, List<Commit>> grouped = service.groupByDay(commits);

        assertEquals(List.of("2026-04-13", "2026-04-14"), grouped.keySet().stream().map(Object::toString).toList());
        assertEquals(2, grouped.values().stream().findFirst().orElseThrow().size());
    }

    @Test
    void cleansPrefixesAndTickets() {
        assertEquals("update save flow", service.cleanCommitMessage("fix(PROJ-123): update save flow #88"));
    }

    @Test
    void summarizesInSpanish() {
        assertEquals(
            "Correcciones en la validación del payload de Cesco.",
            service.summarizeCommitMessage("fix: cesco payload validation")
        );
    }

    private Commit commit(String message, String committedAt) {
        return new Commit(
            "1234567890abcdef",
            "1234567",
            OffsetDateTime.parse(committedAt),
            message
        );
    }
}
