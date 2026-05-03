package com.aichallenge.agents.testcoverage.infrastructure.ai;

import com.aichallenge.agents.testcoverage.application.CoverageAnalysisRequest;
import com.aichallenge.agents.testcoverage.application.SpringBootTestCoverageAnalyzer;
import com.aichallenge.agents.testcoverage.infrastructure.JavaSourceProjectScanner;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BedrockCoverageStrategyAdvisorTest {
    @TempDir
    Path projectPath;

    @Test
    void removesReasoningBlocksFromModelOutput() {
        String sanitized = BedrockCoverageStrategyAdvisor.sanitizeModelOutput("""
            <reasoning>internal chain of thought</reasoning>
            ## Diagnostico
            - Plan visible.
            """);

        assertFalse(sanitized.contains("<reasoning>"));
        assertFalse(sanitized.contains("internal chain of thought"));
        assertTrue(sanitized.contains("## Diagnostico"));
    }

    @Test
    void normalizesImplementationTestCaseNames() {
        String sanitized = BedrockCoverageStrategyAdvisor.sanitizeModelOutput("""
            ## Tests a implementar
            - `AgentStrategyService.activateStrategy_success` - objetivo - asserts principales.
            - `BackofficeAgentStrategyControllerTest.createStrategy_validation` - objetivo - asserts principales.
            """);

        assertTrue(sanitized.contains("`AgentStrategyServiceTest.activateStrategy_success`"));
        assertTrue(sanitized.contains("`BackofficeAgentStrategyControllerTest.createStrategy_validation`"));
        assertFalse(sanitized.contains("`AgentStrategyService.activateStrategy_success`"));
    }

    @Test
    void includesSourceSnippetsInUserPrompt() throws IOException {
        write("src/main/java/com/example/agent/AgentStrategyService.java", """
            package com.example.agent;

            import org.springframework.stereotype.Service;

            @Service
            class AgentStrategyService {
                String resolve(String agentType) {
                    if (agentType == null || agentType.isBlank()) {
                        throw new IllegalArgumentException("agentType is required");
                    }
                    return agentType.trim().toLowerCase();
                }
            }
            """);

        SpringBootTestCoverageAnalyzer analyzer = new SpringBootTestCoverageAnalyzer(new JavaSourceProjectScanner());
        var analysis = analyzer.analyze(new CoverageAnalysisRequest(projectPath, "agent"));
        BedrockCoverageStrategyAdvisor advisor = new BedrockCoverageStrategyAdvisor(
            BedrockConfig.fromEnvironment(),
            null,
            new ObjectMapper()
        );

        String prompt = advisor.buildUserPrompt(analysis);

        assertTrue(prompt.contains("Snippets de codigo fuente"));
        assertTrue(prompt.contains("class AgentStrategyService"));
        assertTrue(prompt.contains("throw new IllegalArgumentException"));
    }

    private void write(String relativePath, String content) throws IOException {
        Path file = projectPath.resolve(relativePath);
        Files.createDirectories(file.getParent());
        Files.writeString(file, content);
    }
}
