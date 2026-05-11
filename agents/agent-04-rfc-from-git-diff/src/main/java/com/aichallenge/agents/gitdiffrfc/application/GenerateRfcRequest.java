package com.aichallenge.agents.gitdiffrfc.application;

import java.nio.file.Path;
import java.util.Optional;

public record GenerateRfcRequest(
        Optional<String> source,
        Optional<String> target,
        Optional<String> range,
        Path repoPath,
        Optional<Path> output,
        boolean ai,
        int maxDiffLines,
        boolean includeFullFiles,
        boolean includeRelatedContext
) {

    public String comparisonRange() {
        return range.orElseGet(() -> target.orElseThrow() + ".." + source.orElseThrow());
    }
}
