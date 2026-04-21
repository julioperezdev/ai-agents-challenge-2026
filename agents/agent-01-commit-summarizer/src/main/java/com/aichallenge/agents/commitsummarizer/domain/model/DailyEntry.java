package com.aichallenge.agents.commitsummarizer.domain.model;

import java.time.LocalDate;
import java.util.List;

/**
 * Represents a single working day with its meetings and commits.
 */
public record DailyEntry(
    LocalDate date,
    List<String> meetings,
    List<RepoCommitLine> commitLines,
    List<String> summaryLines
) {

    public boolean hasContent() {
        return !meetings.isEmpty() || !commitLines.isEmpty();
    }

    /**
     * A commit line optionally tagged with a repo name.
     */
    public record RepoCommitLine(String repoName, String commitMessage) {
    }
}
