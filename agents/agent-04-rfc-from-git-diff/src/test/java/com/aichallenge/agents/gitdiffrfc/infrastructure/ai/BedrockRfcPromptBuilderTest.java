package com.aichallenge.agents.gitdiffrfc.infrastructure.ai;

import static org.assertj.core.api.Assertions.assertThat;

import com.aichallenge.agents.gitdiffrfc.domain.ChangeSet;
import com.aichallenge.agents.gitdiffrfc.domain.ChangeType;
import com.aichallenge.agents.gitdiffrfc.domain.ChangedFile;
import com.aichallenge.agents.gitdiffrfc.domain.CodeContextFile;
import com.aichallenge.agents.gitdiffrfc.domain.FileDiff;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;

class BedrockRfcPromptBuilderTest {

    private final BedrockRfcPromptBuilder builder = new BedrockRfcPromptBuilder();

    @Test
    void buildsPromptWithRulesAndRequiredSections() {
        String prompt = builder.build(sampleChangeSet());

        assertThat(prompt)
                .contains("Return only the final Markdown document")
                .contains("Do not invent exact request fields, endpoint paths, table names, method names, config keys, or behavior")
                .contains("Use plain ASCII spaces only")
                .contains("# RFC: <generated title>")
                .contains("## Summary")
                .contains("## Review Checklist");
    }

    @Test
    void rendersStructuredEvidenceAndDiffExcerpts() {
        String prompt = builder.build(sampleChangeSet());

        assertThat(prompt)
                .contains("<change_scope>")
                .contains("range: main..feature/webcheck")
                .contains("source: feature/webcheck")
                .contains("target: main")
                .contains("files_changed: 2")
                .contains("<files>")
                .contains("path: src/main/java/ChatsController.java")
                .contains("previous_path: src/main/java/OldRequest.java")
                .contains("<diff_stat>")
                .contains("2 files changed, 10 insertions(+), 2 deletions(-)")
                .contains("<diff_excerpts>")
                .contains("<file_diff path=\"src/main/java/ChatsController.java\" truncated=\"false\">")
                .contains("+@PostMapping(\"/{chatId}/webcheck\")")
                .contains("<full_changed_files>")
                .contains("<context_file path=\"src/main/java/ChatsController.java\" reason=\"full changed file\">")
                .contains("<related_context_files>")
                .contains("<context_file path=\"src/main/java/ChatService.java\" reason=\"related reference from changed symbols\">");
    }

    @Test
    void escapesDiffPathAttribute() {
        ChangeSet changeSet = new ChangeSet(
                Optional.empty(),
                Optional.empty(),
                "main..feature",
                List.of(),
                "",
                List.of(new FileDiff("src/A&B\"<Demo>.java", "+demo", false)),
                List.of(),
                List.of(),
                100,
                false
        );

        String prompt = builder.build(changeSet);

        assertThat(prompt).contains("<file_diff path=\"src/A&amp;B&quot;&lt;Demo&gt;.java\" truncated=\"false\">");
    }

    private ChangeSet sampleChangeSet() {
        return new ChangeSet(
                Optional.of("feature/webcheck"),
                Optional.of("main"),
                "main..feature/webcheck",
                List.of(
                        new ChangedFile(ChangeType.MODIFIED, "src/main/java/ChatsController.java", Optional.empty()),
                        new ChangedFile(ChangeType.RENAMED, "src/main/java/WebCheckRequest.java", Optional.of("src/main/java/OldRequest.java"))
                ),
                "2 files changed, 10 insertions(+), 2 deletions(-)",
                List.of(new FileDiff(
                        "src/main/java/ChatsController.java",
                        """
                                diff --git a/src/main/java/ChatsController.java b/src/main/java/ChatsController.java
                                @@ -1,2 +1,3 @@
                                +@PostMapping(\"/{chatId}/webcheck\")
                                """,
                        false
                )),
                List.of(new CodeContextFile("src/main/java/ChatsController.java", "full changed file", "class ChatsController {}")),
                List.of(new CodeContextFile("src/main/java/ChatService.java", "related reference from changed symbols", "class ChatService {}")),
                1200,
                true
        );
    }
}
