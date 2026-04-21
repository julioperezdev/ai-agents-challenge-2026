package com.aichallenge.agents.commitsummarizer.infrastructure;

import com.aichallenge.agents.commitsummarizer.domain.model.Commit;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class GitCommitReader {
    public List<Commit> readCommits(Path repoPath, String author, LocalDate dateFrom, LocalDate dateTo)
        throws IOException, InterruptedException {
        ensureGitRepository(repoPath);

        String since = dateFrom.atStartOfDay().toString();
        String until = dateTo.atTime(LocalTime.MAX.withNano(0)).toString();
        String prettyFormat = "%H%x1f%h%x1f%cI%x1f%s%x1e";

        List<String> command = List.of(
            "git", "-C", repoPath.toString(), "log",
            "--no-merges",
            "--author=" + author,
            "--since=" + since,
            "--until=" + until,
            "--pretty=format:" + prettyFormat
        );

        String output = runCommand(command).strip();
        if (output.isBlank()) {
            return List.of();
        }

        List<Commit> commits = new ArrayList<>();
        for (String entry : output.split("\u001e")) {
            String trimmedEntry = entry.trim();
            if (trimmedEntry.isBlank()) {
                continue;
            }
            String[] parts = trimmedEntry.split("\u001f", 4);
            if (parts.length != 4) {
                continue;
            }
            commits.add(new Commit(
                parts[0],
                parts[1],
                OffsetDateTime.parse(parts[2]),
                parts[3].trim()
            ));
        }

        commits.sort(Comparator.comparing(Commit::committedAt));
        return commits;
    }

    public String detectAuthor(Path repoPath) throws IOException, InterruptedException {
        ensureGitRepository(repoPath);

        for (String key : List.of("user.name", "user.email")) {
            ProcessResult result = runCommandWithResult(List.of("git", "-C", repoPath.toString(), "config", "--get", key));
            String value = result.output().trim();
            if (result.exitCode() == 0 && !value.isBlank()) {
                return value;
            }
        }

        return null;
    }

    public void ensureGitRepository(Path repoPath) throws IOException, InterruptedException {
        ProcessResult result = runCommandWithResult(List.of("git", "-C", repoPath.toAbsolutePath().toString(), "rev-parse", "--is-inside-work-tree"));
        if (result.exitCode() != 0 || !"true".equals(result.output().trim())) {
            throw new IllegalArgumentException("La ruta '" + repoPath.toAbsolutePath() + "' no es un repositorio Git válido.");
        }
    }

    private String runCommand(List<String> command) throws IOException, InterruptedException {
        ProcessResult result = runCommandWithResult(command);
        if (result.exitCode() != 0) {
            if (isEmptyHistoryError(result.error())) {
                return "";
            }
            throw new IllegalStateException(result.error().isBlank() ? "Fallo al ejecutar Git." : result.error().trim());
        }
        return result.output();
    }

    private boolean isEmptyHistoryError(String error) {
        String normalized = error == null ? "" : error.toLowerCase();
        return normalized.contains("does not have any commits yet")
            || normalized.contains("your current branch")
            || normalized.contains("ambiguous argument 'head'")
            || normalized.contains("unknown revision or path not in the working tree");
    }

    private ProcessResult runCommandWithResult(List<String> command) throws IOException, InterruptedException {
        Process process = new ProcessBuilder(command).start();
        int exitCode = process.waitFor();
        String output = new String(process.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
        String error = new String(process.getErrorStream().readAllBytes(), StandardCharsets.UTF_8);
        return new ProcessResult(exitCode, output, error);
    }

    private record ProcessResult(int exitCode, String output, String error) {
    }
}
