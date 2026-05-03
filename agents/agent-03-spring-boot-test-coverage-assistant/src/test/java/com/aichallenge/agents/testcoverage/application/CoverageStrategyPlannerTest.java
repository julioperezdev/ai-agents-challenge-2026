package com.aichallenge.agents.testcoverage.application;

import com.aichallenge.agents.testcoverage.domain.CoverageAnalysis;
import com.aichallenge.agents.testcoverage.domain.CoverageStrategy;
import com.aichallenge.agents.testcoverage.domain.port.CoverageStrategyAdvisor;
import com.aichallenge.agents.testcoverage.infrastructure.JavaSourceProjectScanner;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CoverageStrategyPlannerTest {
    @TempDir
    Path projectPath;

    @Test
    void delegatesStaticAnalysisToStrategyAdvisor() throws IOException {
        write("src/main/java/com/example/auth/AuthController.java", """
            package com.example.auth;

            import org.springframework.web.bind.annotation.PostMapping;
            import org.springframework.web.bind.annotation.RestController;

            @RestController
            class AuthController {
                @PostMapping("/auth/login")
                void login() {
                }
            }
            """);

        SpringBootTestCoverageAnalyzer analyzer = new SpringBootTestCoverageAnalyzer(new JavaSourceProjectScanner());
        CoverageStrategyPlanner planner = new CoverageStrategyPlanner(analyzer, new FakeAiAdvisor());

        CoverageStrategy strategy = planner.plan(new CoverageAnalysisRequest(projectPath, "auth"));

        assertTrue(strategy.generatedByAi());
        assertEquals("AI plan for 1 components", strategy.content());
    }

    private void write(String relativePath, String content) throws IOException {
        Path file = projectPath.resolve(relativePath);
        Files.createDirectories(file.getParent());
        Files.writeString(file, content);
    }

    private static class FakeAiAdvisor implements CoverageStrategyAdvisor {
        @Override
        public CoverageStrategy advise(CoverageAnalysis analysis) {
            return new CoverageStrategy(
                "AI plan for " + analysis.components().size() + " components",
                true
            );
        }
    }
}
