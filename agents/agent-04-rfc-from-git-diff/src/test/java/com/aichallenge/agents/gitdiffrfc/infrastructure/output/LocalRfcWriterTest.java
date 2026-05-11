package com.aichallenge.agents.gitdiffrfc.infrastructure.output;

import static org.assertj.core.api.Assertions.assertThat;

import com.aichallenge.agents.gitdiffrfc.domain.ChangeSet;
import com.aichallenge.agents.gitdiffrfc.domain.ChangeType;
import com.aichallenge.agents.gitdiffrfc.domain.ChangedFile;
import com.aichallenge.agents.gitdiffrfc.domain.RfcDocument;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;

class LocalRfcWriterTest {

    private final LocalRfcWriter writer = new LocalRfcWriter();

    @Test
    void writesRequiredSections() {
        ChangeSet changeSet = new ChangeSet(
                Optional.of("feature/demo"),
                Optional.of("main"),
                "main..feature/demo",
                List.of(new ChangedFile(ChangeType.MODIFIED, "src/main/java/Demo.java", Optional.empty())),
                "1 file changed, 4 insertions(+), 2 deletions(-)",
                List.of(),
                List.of(),
                List.of(),
                800,
                false
        );

        RfcDocument document = writer.write(changeSet);

        assertThat(document.markdown()).contains("# RFC:", "## Summary", "## Change Scope", "## Review Checklist");
        assertThat(document.markdown()).contains("Files changed: 1", "Additions: 4", "Deletions: 2");
    }
}
