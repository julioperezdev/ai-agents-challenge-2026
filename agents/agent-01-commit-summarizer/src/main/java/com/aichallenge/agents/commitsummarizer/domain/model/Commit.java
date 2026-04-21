package com.aichallenge.agents.commitsummarizer.domain.model;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneId;

public record Commit(
    String fullHash,
    String shortHash,
    OffsetDateTime committedAt,
    String message
) {
    public LocalDate localDay() {
        return committedAt.atZoneSameInstant(ZoneId.systemDefault()).toLocalDate();
    }
}
