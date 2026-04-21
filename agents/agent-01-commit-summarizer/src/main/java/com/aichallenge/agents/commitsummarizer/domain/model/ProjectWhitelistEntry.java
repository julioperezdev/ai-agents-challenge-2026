package com.aichallenge.agents.commitsummarizer.domain.model;

import java.nio.file.Path;

public record ProjectWhitelistEntry(
    String alias,
    Path repoPath,
    boolean enabled,
    String notes
) {
}
