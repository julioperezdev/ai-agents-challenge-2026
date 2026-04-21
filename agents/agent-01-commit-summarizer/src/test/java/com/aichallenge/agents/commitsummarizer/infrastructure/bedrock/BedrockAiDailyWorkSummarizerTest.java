package com.aichallenge.agents.commitsummarizer.infrastructure.bedrock;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class BedrockAiDailyWorkSummarizerTest {
    @Test
    void normalizesBulletsAndRemovesReasoning() {
        String raw = """
            <reasoning>internal</reasoning>
            - Ajustes en el flujo principal.
            • Correcciones en validaciones.
            1. Traducción de mensajes técnicos.
            """;

        List<String> lines = BedrockAiDailyWorkSummarizer.normalizeSummaryLines(raw);

        assertEquals(
            List.of(
                "Ajustes en el flujo principal.",
                "Correcciones en validaciones.",
                "Traducción de mensajes técnicos."
            ),
            lines
        );
    }
}
