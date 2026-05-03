package com.aichallenge.agents.testcoverage.application;

import com.aichallenge.agents.testcoverage.domain.ComponentCategory;
import com.aichallenge.agents.testcoverage.domain.CoverageAnalysis;
import com.aichallenge.agents.testcoverage.domain.EndpointMapping;
import com.aichallenge.agents.testcoverage.domain.Priority;
import com.aichallenge.agents.testcoverage.domain.SpringComponent;
import com.aichallenge.agents.testcoverage.domain.TestInventory;
import com.aichallenge.agents.testcoverage.domain.TestRecommendation;
import com.aichallenge.agents.testcoverage.domain.TestType;
import com.aichallenge.agents.testcoverage.infrastructure.JavaSourceProjectScanner;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class SpringBootTestCoverageAnalyzer {
    private final JavaSourceProjectScanner scanner;

    public SpringBootTestCoverageAnalyzer(JavaSourceProjectScanner scanner) {
        this.scanner = scanner;
    }

    public CoverageAnalysis analyze(CoverageAnalysisRequest request) throws IOException {
        List<SpringComponent> components = scanner.scanProductionComponents(request.projectPath(), request.target());
        TestInventory testInventory = scanner.scanTests(request.projectPath(), components);
        List<TestRecommendation> recommendations = buildRecommendations(components, testInventory);

        return new CoverageAnalysis(
            request.projectPath(),
            request.target(),
            request.maxRecommendationsPerSection(),
            components,
            testInventory,
            recommendations
        );
    }

    private List<TestRecommendation> buildRecommendations(List<SpringComponent> components, TestInventory testInventory) {
        List<TestRecommendation> recommendations = new ArrayList<>();
        boolean hasControllerInTarget = components.stream()
            .anyMatch(component -> component.category() == ComponentCategory.CONTROLLER);

        for (SpringComponent component : components) {
            switch (component.category()) {
                case CONTROLLER -> addControllerRecommendations(component, testInventory, recommendations);
                case SERVICE -> addServiceRecommendations(component, testInventory, recommendations);
                case REPOSITORY -> addRepositoryRecommendations(component, testInventory, recommendations);
                case DTO -> addDtoRecommendations(component, testInventory, recommendations, hasControllerInTarget);
                default -> {
                }
            }
        }

        return recommendations.stream()
            .sorted(Comparator
                .comparing(TestRecommendation::priority)
                .thenComparing(TestRecommendation::type)
                .thenComparing(TestRecommendation::title))
            .toList();
    }

    private void addControllerRecommendations(
        SpringComponent component,
        TestInventory testInventory,
        List<TestRecommendation> recommendations
    ) {
        if (!testInventory.hasWebMvcCoverage(component.className())) {
            String endpointSummary = component.endpoints().isEmpty()
                ? "los contratos HTTP principales"
                : representativeEndpoint(component).displayName();

            recommendations.add(new TestRecommendation(
                controllerPriority(component),
                TestType.INTEGRATION,
                component.className() + ": cubrir contrato HTTP",
                "Validar " + endpointSummary + " con happy path y errores esperados. Usar el slice mas chico antes de cargar todo el contexto.",
                "@WebMvcTest + MockMvc"
            ));
        }

        if (component.hasValidationAnnotations() && !testInventory.hasWebMvcCoverage(component.className())) {
            recommendations.add(new TestRecommendation(
                Priority.MEDIUM,
                TestType.INTEGRATION,
                component.className() + ": payload invalido responde 400",
                "Cubrir validaciones de request desde HTTP; si viven en anotaciones, el valor esta en verificar el contrato y no en un unit test artificial.",
                "MockMvc"
            ));
        }

        if (shouldSuggestLoadSmoke(component)) {
            recommendations.add(new TestRecommendation(
                Priority.LOW,
                TestType.LOAD,
                component.className() + ": smoke load minimo",
                "Ejecutar un escenario chico sobre el endpoint mas critico del controller, empezando con 20 requests concurrentes y controlando p95 y tasa de error.",
                "k6 o equivalente"
            ));
        }
    }

    private Priority controllerPriority(SpringComponent component) {
        if (component.className().equals("HealthController")) {
            return Priority.LOW;
        }
        return Priority.HIGH;
    }

    private EndpointMapping representativeEndpoint(SpringComponent component) {
        return component.endpoints().stream()
            .filter(endpoint -> !"REQUEST".equals(endpoint.httpMethod()))
            .findFirst()
            .orElse(component.endpoints().get(0));
    }

    private boolean shouldSuggestLoadSmoke(SpringComponent component) {
        if (component.endpoints().isEmpty() || component.className().equals("HealthController")) {
            return false;
        }
        boolean hasWriteEndpoint = component.endpoints().stream()
            .anyMatch(endpoint -> endpoint.httpMethod().equals("POST")
                || endpoint.httpMethod().equals("PUT")
                || endpoint.httpMethod().equals("PATCH")
                || endpoint.httpMethod().equals("DELETE"));
        if (hasWriteEndpoint) {
            return true;
        }
        String className = component.className().toLowerCase();
        return className.contains("auth")
            || className.contains("analytics")
            || className.contains("chat")
            || className.contains("document")
            || className.contains("subscription")
            || className.contains("user")
            || className.contains("vault");
    }

    private void addServiceRecommendations(
        SpringComponent component,
        TestInventory testInventory,
        List<TestRecommendation> recommendations
    ) {
        if (!component.hasRealLogic()) {
            return;
        }

        if (!testInventory.covers(component.className())) {
            recommendations.add(new TestRecommendation(
                Priority.HIGH,
                TestType.UNIT,
                component.className() + ": cubrir reglas y errores",
                "Tiene logica condicional o manejo de excepciones. Priorizar branches de negocio, casos negativos y colaboracion con repositorios mediante mocks.",
                "JUnit 5 + Mockito"
            ));
        }
    }

    private void addRepositoryRecommendations(
        SpringComponent component,
        TestInventory testInventory,
        List<TestRecommendation> recommendations
    ) {
        if (!component.hasPersistenceSpecificLogic()) {
            return;
        }

        if (!testInventory.hasDataJpaCoverage(component.className())) {
            recommendations.add(new TestRecommendation(
                Priority.MEDIUM,
                TestType.INTEGRATION,
                component.className() + ": validar consulta de persistencia",
                "Hay SQL, query custom o acceso JDBC. Cubrir el comportamiento con DB realista; si depende de PostgreSQL, preferir Testcontainers antes que H2.",
                "@DataJpaTest + Testcontainers si aplica"
            ));
        }
    }

    private void addDtoRecommendations(
        SpringComponent component,
        TestInventory testInventory,
        List<TestRecommendation> recommendations,
        boolean hasControllerInTarget
    ) {
        if (!component.hasValidationAnnotations() || testInventory.covers(component.className()) || hasControllerInTarget) {
            return;
        }

        recommendations.add(new TestRecommendation(
            Priority.LOW,
            TestType.INTEGRATION,
            component.className() + ": validar restricciones desde HTTP",
            "No crear unit tests para DTOs triviales. Conviene cubrir estas restricciones a traves del endpoint que consume el payload.",
            "MockMvc"
        ));
    }
}
