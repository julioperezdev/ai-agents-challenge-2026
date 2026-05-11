package com.aichallenge.agents.gitdiffrfc.infrastructure.output;

import static org.assertj.core.api.Assertions.assertThat;

import com.aichallenge.agents.gitdiffrfc.domain.RfcDocument;
import org.junit.jupiter.api.Test;

class MarkdownRfcRendererTest {

    private final MarkdownRfcRenderer renderer = new MarkdownRfcRenderer();

    @Test
    void removesOuterMarkdownFence() {
        String result = renderer.render(new RfcDocument("```markdown\n# RFC: Demo\n```"));

        assertThat(result).isEqualTo("# RFC: Demo\n");
    }

    @Test
    void removesReasoningBlocks() {
        String result = renderer.render(new RfcDocument("<reasoning>private notes</reasoning>\n# RFC: Demo"));

        assertThat(result).isEqualTo("# RFC: Demo\n");
    }

    @Test
    void normalizesNonBreakingSpaces() {
        String result = renderer.render(new RfcDocument("# RFC: Demo\n1\u202F000\u00A0000"));

        assertThat(result).isEqualTo("# RFC: Demo\n1 000 000\n");
    }
}
