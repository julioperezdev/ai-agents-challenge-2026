package com.aichallenge.agents.commitsummarizer.infrastructure;

import com.aichallenge.agents.commitsummarizer.domain.model.Commit;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class GitCommitReaderTest {
    private final GitCommitReader reader = new GitCommitReader();

    @Test
    void returnsEmptyListForRepositoryWithoutCommits(@TempDir Path tempDir) throws Exception {
        run(tempDir, "git", "init");
        run(tempDir, "git", "config", "user.name", "Julio Perez");
        run(tempDir, "git", "config", "user.email", "julio@example.com");

        List<Commit> commits = reader.readCommits(
            tempDir,
            "Julio Perez",
            LocalDate.parse("2026-04-13"),
            LocalDate.parse("2026-04-18")
        );

        assertEquals(0, commits.size());
    }

    @Test
    void readsCommitsByAuthorAndDateRange(@TempDir Path tempDir) throws Exception {
        run(tempDir, "git", "init");
        run(tempDir, "git", "config", "user.name", "Julio Perez");
        run(tempDir, "git", "config", "user.email", "julio@example.com");

        Path file = tempDir.resolve("work.txt");
        Files.writeString(file, "one\n", StandardCharsets.UTF_8);
        run(tempDir, "git", "add", "work.txt");
        commit(tempDir, "feat: update office sync logic", "2026-04-13T09:00:00-03:00");

        Files.writeString(file, "two\n", StandardCharsets.UTF_8);
        run(tempDir, "git", "add", "work.txt");
        commit(tempDir, "fix: cesco payload validation", "2026-04-13T14:00:00-03:00");

        List<Commit> commits = reader.readCommits(tempDir, "Julio Perez", LocalDate.parse("2026-04-13"), LocalDate.parse("2026-04-13"));

        assertEquals(2, commits.size());
        assertEquals("feat: update office sync logic", commits.get(0).message());
        assertEquals("fix: cesco payload validation", commits.get(1).message());
    }

    private void commit(Path directory, String message, String commitDate) throws Exception {
        ProcessBuilder builder = new ProcessBuilder("git", "commit", "-m", message);
        builder.directory(directory.toFile());
        builder.environment().put("GIT_AUTHOR_NAME", "Julio Perez");
        builder.environment().put("GIT_AUTHOR_EMAIL", "julio@example.com");
        builder.environment().put("GIT_AUTHOR_DATE", commitDate);
        builder.environment().put("GIT_COMMITTER_NAME", "Julio Perez");
        builder.environment().put("GIT_COMMITTER_EMAIL", "julio@example.com");
        builder.environment().put("GIT_COMMITTER_DATE", commitDate);
        Process process = builder.start();
        int exitCode = process.waitFor();
        if (exitCode != 0) {
            throw new IllegalStateException(new String(process.getErrorStream().readAllBytes(), StandardCharsets.UTF_8));
        }
    }

    private void run(Path directory, String... command) throws IOException, InterruptedException {
        Process process = new ProcessBuilder(command)
            .directory(directory.toFile())
            .start();
        int exitCode = process.waitFor();
        if (exitCode != 0) {
            throw new IllegalStateException(new String(process.getErrorStream().readAllBytes(), StandardCharsets.UTF_8));
        }
    }
}
