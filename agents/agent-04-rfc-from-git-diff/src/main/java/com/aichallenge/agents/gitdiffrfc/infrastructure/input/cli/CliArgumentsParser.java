package com.aichallenge.agents.gitdiffrfc.infrastructure.input.cli;

import com.aichallenge.agents.gitdiffrfc.application.GenerateRfcRequest;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import org.springframework.stereotype.Component;

@Component
public class CliArgumentsParser {

    private static final int UNLIMITED_DIFF_LINES = 0;

    public GenerateRfcRequest parse(String[] args) {
        Map<String, String> values = new LinkedHashMap<>();
        boolean ai = false;
        boolean includeFullFiles = false;
        boolean includeRelatedContext = false;

        for (int i = 0; i < args.length; i++) {
            String arg = args[i];
            if ("--ai".equals(arg)) {
                ai = true;
                continue;
            }
            if ("--include-full-files".equals(arg)) {
                includeFullFiles = true;
                continue;
            }
            if ("--include-related-context".equals(arg)) {
                includeRelatedContext = true;
                includeFullFiles = true;
                continue;
            }
            if (!arg.startsWith("--")) {
                throw new IllegalArgumentException("Unexpected argument: " + arg);
            }
            if (i + 1 >= args.length || args[i + 1].startsWith("--")) {
                throw new IllegalArgumentException("Missing value for " + arg);
            }
            values.put(arg, args[++i]);
        }

        Optional<String> source = Optional.ofNullable(values.get("--source"));
        Optional<String> target = Optional.ofNullable(values.get("--target"));
        Optional<String> range = Optional.ofNullable(values.get("--range"));

        if (range.isPresent() && (source.isPresent() || target.isPresent())) {
            throw new IllegalArgumentException("Use either --range or --source with --target, not both.");
        }
        if (range.isEmpty() && (source.isEmpty() || target.isEmpty())) {
            throw new IllegalArgumentException("Provide --range or both --source and --target.");
        }

        int maxDiffLines = parseMaxDiffLines(values.get("--max-diff-lines"));
        Path repoPath = Path.of(values.getOrDefault("--repo-path", ".")).toAbsolutePath().normalize();
        Optional<Path> output = Optional.ofNullable(values.get("--output")).map(value -> Path.of(value).toAbsolutePath().normalize());

        return new GenerateRfcRequest(source, target, range, repoPath, output, ai, maxDiffLines, includeFullFiles, includeRelatedContext);
    }

    public String usage() {
        return """
                Usage:
                  ./run.sh --source feature/foo --target main [--ai] [--output RFC.md]
                  ./run.sh --range main..feature/foo [--ai] [--repo-path /path/to/repo]

                Options:
                  --source            Source branch for the change.
                  --target            Target branch to compare against.
                  --range             Explicit Git range, for example main..feature/foo.
                  --repo-path         Local repository path. Defaults to current directory.
                  --output            Markdown output file. Defaults to stdout.
                  --ai                Use AWS Bedrock instead of the deterministic local writer.
                  --max-diff-lines    Maximum diff lines sent to writers. Use 0 for full diff. Defaults to 0.
                  --include-full-files Include full content of changed text files from the source side.
                  --include-related-context Include full changed files plus lightweight related code context.
                """;
    }

    private int parseMaxDiffLines(String value) {
        if (value == null) {
            return UNLIMITED_DIFF_LINES;
        }
        try {
            int parsed = Integer.parseInt(value);
            if (parsed < 0) {
                throw new IllegalArgumentException("--max-diff-lines must be zero or greater.");
            }
            return parsed;
        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException("--max-diff-lines must be a number.");
        }
    }
}
