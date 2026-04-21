package com.aichallenge.agents.commitsummarizer.domain.model;

import java.nio.file.Path;

public record RepositoryEntry(
    String name,
    Path path
) {
}
