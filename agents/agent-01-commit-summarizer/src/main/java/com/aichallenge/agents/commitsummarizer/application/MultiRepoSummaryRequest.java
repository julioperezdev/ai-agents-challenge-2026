package com.aichallenge.agents.commitsummarizer.application;

import com.aichallenge.agents.commitsummarizer.domain.model.DistributionMode;
import com.aichallenge.agents.commitsummarizer.domain.model.RepositoryEntry;

import java.time.LocalDate;
import java.util.List;

public record MultiRepoSummaryRequest(
    LocalDate dateFrom,
    LocalDate dateTo,
    List<RepositoryEntry> repositories,
    boolean includeMeetings,
    DistributionMode distributionMode
) {
}
