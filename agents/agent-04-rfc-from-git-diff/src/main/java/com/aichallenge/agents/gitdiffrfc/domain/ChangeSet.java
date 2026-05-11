package com.aichallenge.agents.gitdiffrfc.domain;

import java.util.List;
import java.util.Optional;

public record ChangeSet(
        Optional<String> source,
        Optional<String> target,
        String range,
        List<ChangedFile> files,
        String diffStat,
        List<FileDiff> diffs,
        List<CodeContextFile> fullFiles,
        List<CodeContextFile> relatedContextFiles,
        int maxDiffLines,
        boolean diffTruncated
) {

    public int filesChanged() {
        return files.size();
    }

    public int additions() {
        return parseStatToken("insertion");
    }

    public int deletions() {
        return parseStatToken("deletion");
    }

    private int parseStatToken(String token) {
        if (diffStat == null || diffStat.isBlank()) {
            return 0;
        }
        String[] parts = diffStat.replace("\n", " ").split(",");
        for (String part : parts) {
            String trimmed = part.trim();
            if (trimmed.contains(token)) {
                String number = trimmed.split(" ")[0];
                try {
                    return Integer.parseInt(number);
                } catch (NumberFormatException ignored) {
                    return 0;
                }
            }
        }
        return 0;
    }
}
