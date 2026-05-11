package com.aichallenge.agents.gitdiffrfc.infrastructure.ai;

import com.aichallenge.agents.gitdiffrfc.domain.ChangeSet;
import com.aichallenge.agents.gitdiffrfc.domain.ChangedFile;
import com.aichallenge.agents.gitdiffrfc.domain.CodeContextFile;
import com.aichallenge.agents.gitdiffrfc.domain.FileDiff;
import org.springframework.stereotype.Component;

@Component
public class BedrockRfcPromptBuilder {

    public String build(ChangeSet changeSet) {
        return """
                You are generating an RFC from Git diff evidence.

                Rules:
                - Return only the final Markdown document.
                - Do not include reasoning, analysis notes, XML tags, code fences, or placeholders such as author/date unless present in the diff.
                - Use only visible evidence from the provided change set and diff excerpts.
                - Mark uncertainty explicitly when evidence is incomplete.
                - Do not invent exact request fields, endpoint paths, table names, method names, config keys, or behavior.
                - If exact values are visible in evidence, copy them exactly.
                - If a value is not visible, say "not visible in diff excerpts".
                - Use plain ASCII spaces only. Do not use non-breaking spaces, narrow no-break spaces, or other Unicode spacing characters.
                - Keep the RFC concise, practical, and reviewable.

                Required output structure:
                # RFC: <generated title>

                ## Summary
                ## Change Scope
                ## Technical Changes
                ## Functional Impact
                ## Risks & Considerations
                ## Open Questions
                ## Review Checklist

                Evidence:
                <change_scope>
                %s
                </change_scope>

                <files>
                %s
                </files>

                <diff_stat>
                %s
                </diff_stat>

                <diff_excerpts>
                %s
                </diff_excerpts>

                <full_changed_files>
                %s
                </full_changed_files>

                <related_context_files>
                %s
                </related_context_files>
                """.formatted(
                renderChangeScope(changeSet),
                renderFiles(changeSet),
                nullToEmpty(changeSet.diffStat()).trim(),
                renderDiffs(changeSet),
                renderContextFiles(changeSet.fullFiles()),
                renderContextFiles(changeSet.relatedContextFiles())
        );
    }

    private String renderChangeScope(ChangeSet changeSet) {
        return """
                range: %s
                source: %s
                target: %s
                files_changed: %d
                additions: %d
                deletions: %d
                max_diff_lines: %s
                diff_truncated: %s
                """.formatted(
                changeSet.range(),
                changeSet.source().orElse("N/A"),
                changeSet.target().orElse("N/A"),
                changeSet.filesChanged(),
                changeSet.additions(),
                changeSet.deletions(),
                maxDiffLinesLabel(changeSet.maxDiffLines()),
                changeSet.diffTruncated()
        ).trim();
    }

    private String maxDiffLinesLabel(int maxDiffLines) {
        return maxDiffLines == 0 ? "unlimited" : String.valueOf(maxDiffLines);
    }

    private String renderFiles(ChangeSet changeSet) {
        if (changeSet.files().isEmpty()) {
            return "No files changed.";
        }
        StringBuilder builder = new StringBuilder();
        for (ChangedFile file : changeSet.files()) {
            builder.append("- type: ").append(file.type()).append("\n");
            builder.append("  path: ").append(file.path()).append("\n");
            file.previousPath().ifPresent(previous -> builder.append("  previous_path: ").append(previous).append("\n"));
        }
        return builder.toString().trim();
    }

    private String renderDiffs(ChangeSet changeSet) {
        if (changeSet.diffs().isEmpty()) {
            return "No diff excerpts available.";
        }
        StringBuilder builder = new StringBuilder();
        for (FileDiff diff : changeSet.diffs()) {
            builder.append("<file_diff path=\"").append(escapeAttribute(diff.path())).append("\" truncated=\"")
                    .append(diff.truncated()).append("\">\n");
            builder.append(nullToEmpty(diff.content()).trim()).append("\n");
            builder.append("</file_diff>\n\n");
        }
        return builder.toString().trim();
    }

    private String renderContextFiles(java.util.List<CodeContextFile> files) {
        if (files.isEmpty()) {
            return "No additional context files included.";
        }
        StringBuilder builder = new StringBuilder();
        for (CodeContextFile file : files) {
            builder.append("<context_file path=\"").append(escapeAttribute(file.path())).append("\" reason=\"")
                    .append(escapeAttribute(file.reason())).append("\">\n");
            builder.append(nullToEmpty(file.content()).trim()).append("\n");
            builder.append("</context_file>\n\n");
        }
        return builder.toString().trim();
    }

    private String escapeAttribute(String value) {
        return nullToEmpty(value)
                .replace("&", "&amp;")
                .replace("\"", "&quot;")
                .replace("<", "&lt;")
                .replace(">", "&gt;");
    }

    private String nullToEmpty(String value) {
        return value == null ? "" : value;
    }
}
