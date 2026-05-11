package com.aichallenge.agents.gitdiffrfc.infrastructure.git;

import com.aichallenge.agents.gitdiffrfc.application.GenerateRfcRequest;
import com.aichallenge.agents.gitdiffrfc.domain.ChangeSet;
import com.aichallenge.agents.gitdiffrfc.domain.ChangeType;
import com.aichallenge.agents.gitdiffrfc.domain.ChangedFile;
import com.aichallenge.agents.gitdiffrfc.domain.CodeContextFile;
import com.aichallenge.agents.gitdiffrfc.domain.FileDiff;
import com.aichallenge.agents.gitdiffrfc.domain.GitDiffReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.springframework.stereotype.Component;

@Component
public class GitCliDiffReader implements GitDiffReader {

    @Override
    public ChangeSet read(GenerateRfcRequest request) {
        validateRepository(request);
        String range = request.comparisonRange();
        String nameStatus = runGit(request, "diff", "--name-status", range);
        String stat = runGit(request, "diff", "--stat", range);
        String diff = runGit(request, "diff", "--unified=20", range);

        List<ChangedFile> files = parseNameStatus(nameStatus);
        TruncatedDiff truncatedDiff = truncateDiffByFile(diff, request.maxDiffLines());
        List<FileDiff> fileDiffs = splitFileDiffs(truncatedDiff.content(), truncatedDiff.truncated());
        String sourceRef = sourceRef(request);
        List<CodeContextFile> fullFiles = request.includeFullFiles() ? readFullFiles(request, sourceRef, files) : List.of();
        List<CodeContextFile> relatedContextFiles = request.includeRelatedContext() ? readRelatedContextFiles(request, sourceRef, files) : List.of();

        return new ChangeSet(
                request.source(),
                request.target(),
                range,
                files,
                stat,
                fileDiffs,
                fullFiles,
                relatedContextFiles,
                request.maxDiffLines(),
                truncatedDiff.truncated()
        );
    }

    private void validateRepository(GenerateRfcRequest request) {
        if (!Files.isDirectory(request.repoPath())) {
            throw new IllegalArgumentException("Repository path does not exist: " + request.repoPath());
        }
        String result = runGit(request, "rev-parse", "--is-inside-work-tree").trim();
        if (!"true".equals(result)) {
            throw new IllegalArgumentException("Path is not inside a Git work tree: " + request.repoPath());
        }
    }

    private String runGit(GenerateRfcRequest request, String... args) {
        GitCommandResult result = runGitCommand(request, args);
        if (result.exitCode() != 0) {
            List<String> command = new ArrayList<>();
            command.add("git");
            command.addAll(List.of(args));
            throw new IllegalArgumentException("Git command failed: " + String.join(" ", command) + "\n" + result.stderr().trim());
        }
        return result.stdout();
    }

    private Optional<String> runGitOptional(GenerateRfcRequest request, String... args) {
        GitCommandResult result = runGitCommand(request, args);
        if (result.exitCode() != 0) {
            return Optional.empty();
        }
        return Optional.of(result.stdout());
    }

    private GitCommandResult runGitCommand(GenerateRfcRequest request, String... args) {
        List<String> command = new ArrayList<>();
        command.add("git");
        command.addAll(List.of(args));
        ProcessBuilder builder = new ProcessBuilder(command)
                .directory(request.repoPath().toFile())
                .redirectErrorStream(false);
        try {
            Process process = builder.start();
            String stdout = new String(process.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
            String stderr = new String(process.getErrorStream().readAllBytes(), StandardCharsets.UTF_8);
            int exitCode = process.waitFor();
            return new GitCommandResult(exitCode, stdout, stderr);
        } catch (IOException ex) {
            throw new IllegalStateException("Unable to execute Git CLI. Is git installed?", ex);
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Git command interrupted.", ex);
        }
    }

    private String sourceRef(GenerateRfcRequest request) {
        if (request.source().isPresent()) {
            return request.source().get();
        }
        String range = request.comparisonRange();
        if (range.contains("...")) {
            return range.substring(range.indexOf("...") + 3);
        }
        if (range.contains("..")) {
            return range.substring(range.indexOf("..") + 2);
        }
        return "HEAD";
    }

    private List<ChangedFile> parseNameStatus(String nameStatus) {
        if (nameStatus == null || nameStatus.isBlank()) {
            return List.of();
        }
        List<ChangedFile> files = new ArrayList<>();
        for (String line : nameStatus.split("\\R")) {
            if (line.isBlank()) {
                continue;
            }
            String[] parts = line.split("\\t");
            String status = parts[0];
            ChangeType type = mapChangeType(status);
            if (status.startsWith("R") && parts.length >= 3) {
                files.add(new ChangedFile(type, parts[2], Optional.of(parts[1])));
            } else if (parts.length >= 2) {
                files.add(new ChangedFile(type, parts[1], Optional.empty()));
            }
        }
        return files;
    }

    private ChangeType mapChangeType(String status) {
        return switch (status.charAt(0)) {
            case 'A' -> ChangeType.ADDED;
            case 'M' -> ChangeType.MODIFIED;
            case 'D' -> ChangeType.DELETED;
            case 'R' -> ChangeType.RENAMED;
            case 'C' -> ChangeType.COPIED;
            case 'T' -> ChangeType.TYPE_CHANGED;
            case 'U' -> ChangeType.UNMERGED;
            default -> ChangeType.UNKNOWN;
        };
    }

    private TruncatedDiff truncateDiffByFile(String diff, int maxLines) {
        if (diff == null || diff.isBlank()) {
            return new TruncatedDiff(diff, false);
        }
        if (maxLines == 0) {
            return new TruncatedDiff(diff, false);
        }
        List<String> fileDiffs = splitRawFileDiffs(diff);
        int totalLines = diff.split("\\R", -1).length;
        if (totalLines <= maxLines || fileDiffs.isEmpty()) {
            return new TruncatedDiff(diff, false);
        }

        int linesPerFile = Math.max(12, maxLines / fileDiffs.size());
        StringBuilder builder = new StringBuilder();
        for (String fileDiff : fileDiffs) {
            builder.append(truncateFileDiffByHunk(fileDiff, linesPerFile));
        }
        builder.append("\n[Diff truncated across files at ").append(maxLines).append(" total target lines]\n");
        return new TruncatedDiff(builder.toString(), true);
    }

    private String truncateFileDiffByHunk(String fileDiff, int maxLines) {
        String[] lines = fileDiff.split("\\R", -1);
        if (lines.length <= maxLines) {
            return fileDiff.endsWith(System.lineSeparator()) ? fileDiff : fileDiff + System.lineSeparator();
        }

        List<List<String>> sections = new ArrayList<>();
        List<String> current = new ArrayList<>();
        for (String line : lines) {
            if (line.startsWith("@@") && !current.isEmpty()) {
                sections.add(current);
                current = new ArrayList<>();
            }
            current.add(line);
        }
        if (!current.isEmpty()) {
            sections.add(current);
        }

        StringBuilder builder = new StringBuilder();
        List<String> header = sections.get(0);
        int headerLimit = Math.min(header.size(), Math.min(8, maxLines));
        for (int i = 0; i < headerLimit; i++) {
            builder.append(header.get(i)).append(System.lineSeparator());
        }

        int hunkCount = Math.max(1, sections.size() - 1);
        int remaining = Math.max(4, maxLines - headerLimit);
        int linesPerHunk = Math.max(4, remaining / hunkCount);
        for (int sectionIndex = 1; sectionIndex < sections.size(); sectionIndex++) {
            List<String> section = sections.get(sectionIndex);
            int sectionLimit = Math.min(section.size(), linesPerHunk);
            for (int i = 0; i < sectionLimit; i++) {
                builder.append(section.get(i)).append(System.lineSeparator());
            }
            if (section.size() > sectionLimit) {
                builder.append("[Hunk truncated after ").append(sectionLimit).append(" lines]\n");
            }
        }
        builder.append("[File diff truncated after ").append(maxLines).append(" allocated lines]\n");
        return builder.toString();
    }

    private List<String> splitRawFileDiffs(String diff) {
        List<String> chunks = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        for (String line : diff.split("\\R")) {
            if (line.startsWith("diff --git ") && !current.isEmpty()) {
                chunks.add(current.toString());
                current = new StringBuilder();
            }
            current.append(line).append(System.lineSeparator());
        }
        if (!current.isEmpty()) {
            chunks.add(current.toString());
        }
        return chunks;
    }

    private List<FileDiff> splitFileDiffs(String diff, boolean truncated) {
        if (diff == null || diff.isBlank()) {
            return List.of();
        }
        List<FileDiff> diffs = new ArrayList<>();
        String currentPath = "diff";
        StringBuilder current = new StringBuilder();
        for (String line : diff.split("\\R")) {
            if (line.startsWith("diff --git ") && !current.isEmpty()) {
                diffs.add(new FileDiff(currentPath, current.toString(), truncated));
                current = new StringBuilder();
                currentPath = extractPath(line);
            } else if (line.startsWith("diff --git ")) {
                currentPath = extractPath(line);
            }
            current.append(line).append(System.lineSeparator());
        }
        if (!current.isEmpty()) {
            diffs.add(new FileDiff(currentPath, current.toString(), truncated));
        }
        return diffs;
    }

    private String extractPath(String diffHeader) {
        String[] parts = diffHeader.split(" ");
        if (parts.length >= 4) {
            return parts[3].replaceFirst("^b/", "");
        }
        return "diff";
    }

    private List<CodeContextFile> readFullFiles(GenerateRfcRequest request, String sourceRef, List<ChangedFile> files) {
        List<CodeContextFile> contextFiles = new ArrayList<>();
        for (ChangedFile file : files) {
            if (file.type() == ChangeType.DELETED || !isTextCodePath(file.path())) {
                continue;
            }
            readFileAtRef(request, sourceRef, file.path())
                    .ifPresent(content -> contextFiles.add(new CodeContextFile(file.path(), "full changed file", content)));
        }
        return contextFiles;
    }

    private List<CodeContextFile> readRelatedContextFiles(GenerateRfcRequest request, String sourceRef, List<ChangedFile> files) {
        Set<String> changedPaths = new LinkedHashSet<>();
        Set<String> relatedPaths = new LinkedHashSet<>();
        for (ChangedFile file : files) {
            changedPaths.add(file.path());
            classNameFromPath(file.path()).ifPresent(className -> relatedPaths.addAll(grepReferences(request, sourceRef, className)));
        }

        List<CodeContextFile> contextFiles = new ArrayList<>();
        for (String path : relatedPaths) {
            if (contextFiles.size() >= 20) {
                break;
            }
            if (changedPaths.contains(path) || !isTextCodePath(path)) {
                continue;
            }
            readFileAtRef(request, sourceRef, path)
                    .ifPresent(content -> contextFiles.add(new CodeContextFile(path, "related reference from changed symbols", content)));
        }
        return contextFiles;
    }

    private List<String> grepReferences(GenerateRfcRequest request, String sourceRef, String symbol) {
        return runGitOptional(request, "grep", "-l", symbol, sourceRef, "--", "*.java", "*.kt", "*.ts", "*.tsx", "*.js", "*.jsx")
                .map(output -> output.lines()
                        .map(line -> line.replaceFirst("^" + java.util.regex.Pattern.quote(sourceRef) + ":", ""))
                        .filter(line -> !line.isBlank())
                        .toList())
                .orElse(List.of());
    }

    private Optional<String> readFileAtRef(GenerateRfcRequest request, String sourceRef, String path) {
        return runGitOptional(request, "show", sourceRef + ":" + path)
                .filter(content -> !content.contains("\u0000"))
                .map(content -> content.length() > 80_000 ? content.substring(0, 80_000) + "\n[File content truncated at 80000 characters]\n" : content);
    }

    private Optional<String> classNameFromPath(String path) {
        int slash = path.lastIndexOf('/');
        String filename = slash >= 0 ? path.substring(slash + 1) : path;
        int dot = filename.indexOf('.');
        if (dot <= 0) {
            return Optional.empty();
        }
        return Optional.of(filename.substring(0, dot));
    }

    private boolean isTextCodePath(String path) {
        return path.endsWith(".java")
                || path.endsWith(".kt")
                || path.endsWith(".js")
                || path.endsWith(".jsx")
                || path.endsWith(".ts")
                || path.endsWith(".tsx")
                || path.endsWith(".json")
                || path.endsWith(".yaml")
                || path.endsWith(".yml")
                || path.endsWith(".xml")
                || path.endsWith(".properties")
                || path.endsWith(".md")
                || path.endsWith("pom.xml")
                || path.endsWith(".gradle");
    }

    private record TruncatedDiff(String content, boolean truncated) {
    }

    private record GitCommandResult(int exitCode, String stdout, String stderr) {
    }
}
