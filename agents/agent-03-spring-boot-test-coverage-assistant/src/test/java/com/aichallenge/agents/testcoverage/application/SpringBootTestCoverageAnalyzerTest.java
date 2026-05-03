package com.aichallenge.agents.testcoverage.application;

import com.aichallenge.agents.testcoverage.domain.CoverageAnalysis;
import com.aichallenge.agents.testcoverage.domain.TestType;
import com.aichallenge.agents.testcoverage.infrastructure.JavaSourceProjectScanner;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertTrue;

class SpringBootTestCoverageAnalyzerTest {
    @TempDir
    Path projectPath;

    @Test
    void prioritizesUsefulGapsWithoutRecommendingTrivialServiceTests() throws IOException {
        write("src/main/java/com/example/orders/OrderController.java", """
            package com.example.orders;

            import jakarta.validation.Valid;
            import org.springframework.web.bind.annotation.PostMapping;
            import org.springframework.web.bind.annotation.RequestBody;
            import org.springframework.web.bind.annotation.RestController;

            @RestController
            class OrderController {
                @PostMapping("/orders")
                void create(@Valid @RequestBody CreateOrderRequest request) {
                }
            }
            """);
        write("src/main/java/com/example/orders/CreateOrderRequest.java", """
            package com.example.orders;

            import jakarta.validation.constraints.NotBlank;

            record CreateOrderRequest(@NotBlank String customerId) {
            }
            """);
        write("src/main/java/com/example/orders/OrderService.java", """
            package com.example.orders;

            import org.springframework.stereotype.Service;

            @Service
            class OrderService {
                void create(CreateOrderRequest request) {
                    if (request.customerId().isBlank()) {
                        throw new IllegalArgumentException("customerId is required");
                    }
                }
            }
            """);
        write("src/main/java/com/example/orders/OrderRepository.java", """
            package com.example.orders;

            import org.springframework.data.jpa.repository.Query;
            import org.springframework.stereotype.Repository;

            @Repository
            interface OrderRepository {
                @Query(value = "select * from orders where status = ?1", nativeQuery = true)
                void findByStatus(String status);
            }
            """);

        SpringBootTestCoverageAnalyzer analyzer = new SpringBootTestCoverageAnalyzer(new JavaSourceProjectScanner());
        CoverageAnalysis analysis = analyzer.analyze(new CoverageAnalysisRequest(projectPath, "orders"));

        assertTrue(analysis.recommendations().stream().anyMatch(recommendation -> recommendation.type() == TestType.UNIT));
        assertTrue(analysis.recommendations().stream().anyMatch(recommendation -> recommendation.title().contains("contrato HTTP")));
        assertTrue(analysis.recommendations().stream().anyMatch(recommendation -> recommendation.title().contains("persistencia")));
        assertTrue(analysis.recommendations().stream().anyMatch(recommendation -> recommendation.type() == TestType.LOAD));
    }

    private void write(String relativePath, String content) throws IOException {
        Path file = projectPath.resolve(relativePath);
        Files.createDirectories(file.getParent());
        Files.writeString(file, content);
    }
}
