package com.aichallenge.agents.testcoverage.presentation;

import com.aichallenge.agents.testcoverage.application.CoverageAnalysisRequest;
import com.aichallenge.agents.testcoverage.application.SpringBootTestCoverageAnalyzer;
import com.aichallenge.agents.testcoverage.domain.CoverageAnalysis;
import com.aichallenge.agents.testcoverage.infrastructure.JavaSourceProjectScanner;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertTrue;

class CoveragePlanRendererTest {
    @TempDir
    Path projectPath;

    @Test
    void rendersPrioritizedBackendFriendlyPlan() throws IOException {
        write("src/main/java/com/example/users/UserController.java", """
            package com.example.users;

            import org.springframework.web.bind.annotation.GetMapping;
            import org.springframework.web.bind.annotation.RestController;

            @RestController
            class UserController {
                @GetMapping("/users")
                void list() {
                }
            }
            """);

        SpringBootTestCoverageAnalyzer analyzer = new SpringBootTestCoverageAnalyzer(new JavaSourceProjectScanner());
        CoverageAnalysis analysis = analyzer.analyze(new CoverageAnalysisRequest(projectPath, null));

        String output = new CoveragePlanRenderer().render(analysis);

        assertTrue(output.contains("Spring Boot Test Coverage Assistant"));
        assertTrue(output.contains("Integracion prioritaria"));
        assertTrue(output.contains("@WebMvcTest + MockMvc"));
        assertTrue(output.contains("Carga minima sugerida"));
    }

    private void write(String relativePath, String content) throws IOException {
        Path file = projectPath.resolve(relativePath);
        Files.createDirectories(file.getParent());
        Files.writeString(file, content);
    }
}
