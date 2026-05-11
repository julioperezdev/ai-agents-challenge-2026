package com.aichallenge.agents.gitdiffrfc.infrastructure.input.cli;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.aichallenge.agents.gitdiffrfc.application.GenerateRfcRequest;
import org.junit.jupiter.api.Test;

class CliArgumentsParserTest {

    private final CliArgumentsParser parser = new CliArgumentsParser();

    @Test
    void parsesExplicitRange() {
        GenerateRfcRequest request = parser.parse(new String[] {"--range", "main..feature/demo", "--ai", "--max-diff-lines", "25"});

        assertThat(request.range()).contains("main..feature/demo");
        assertThat(request.ai()).isTrue();
        assertThat(request.maxDiffLines()).isEqualTo(25);
    }

    @Test
    void defaultsToUnlimitedDiffLines() {
        GenerateRfcRequest request = parser.parse(new String[] {"--range", "main..feature/demo"});

        assertThat(request.maxDiffLines()).isZero();
    }

    @Test
    void allowsZeroAsUnlimitedDiffLines() {
        GenerateRfcRequest request = parser.parse(new String[] {"--range", "main..feature/demo", "--max-diff-lines", "0"});

        assertThat(request.maxDiffLines()).isZero();
    }

    @Test
    void parsesExpandedContextFlags() {
        GenerateRfcRequest request = parser.parse(new String[] {
                "--range", "main..feature/demo",
                "--include-related-context"
        });

        assertThat(request.includeFullFiles()).isTrue();
        assertThat(request.includeRelatedContext()).isTrue();
    }

    @Test
    void rejectsNegativeDiffLines() {
        assertThatThrownBy(() -> parser.parse(new String[] {"--range", "main..feature/demo", "--max-diff-lines", "-1"}))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("--max-diff-lines must be zero or greater");
    }

    @Test
    void requiresRangeOrSourceAndTarget() {
        assertThatThrownBy(() -> parser.parse(new String[] {"--source", "feature/demo"}))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Provide --range or both --source and --target");
    }
}
