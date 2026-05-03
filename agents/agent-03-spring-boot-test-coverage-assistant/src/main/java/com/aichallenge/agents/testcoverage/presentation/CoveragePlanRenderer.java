package com.aichallenge.agents.testcoverage.presentation;

import com.aichallenge.agents.testcoverage.domain.ComponentCategory;
import com.aichallenge.agents.testcoverage.domain.CoverageAnalysis;
import com.aichallenge.agents.testcoverage.domain.Priority;
import com.aichallenge.agents.testcoverage.domain.SpringComponent;
import com.aichallenge.agents.testcoverage.domain.TestRecommendation;
import com.aichallenge.agents.testcoverage.domain.TestType;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class CoveragePlanRenderer {
    public String render(CoverageAnalysis analysis) {
        StringBuilder output = new StringBuilder();

        output.append("Spring Boot Test Coverage Assistant\n");
        output.append("Proyecto: ").append(analysis.projectPath().toAbsolutePath()).append("\n");
        output.append("Target: ").append(formatTarget(analysis.target())).append("\n\n");

        output.append("Resumen\n");
        output.append("- Componentes detectados: ").append(analysis.components().size()).append("\n");
        output.append("- Tests detectados: ").append(analysis.testInventory().tests().size()).append("\n");
        output.append("- Gaps priorizados: ").append(analysis.recommendations().size()).append("\n");
        appendComponentSummary(output, analysis.components());

        output.append("\nRiesgos principales\n");
        appendRiskSummary(output, analysis);

        appendRecommendations(output, "Unit tests prioritarios", analysis.recommendations(), TestType.UNIT, analysis.maxRecommendationsPerSection());
        appendRecommendations(output, "Integracion prioritaria", analysis.recommendations(), TestType.INTEGRATION, analysis.maxRecommendationsPerSection());
        appendRecommendations(output, "Carga minima sugerida", analysis.recommendations(), TestType.LOAD, analysis.maxRecommendationsPerSection());

        output.append("\nNotas\n");
        output.append("- No se recomienda @SpringBootTest por defecto si @WebMvcTest o @DataJpaTest cubren el riesgo.\n");
        output.append("- La cobertura util se prioriza por riesgo, no por porcentaje bruto.\n");

        return output.toString();
    }

    private String formatTarget(String target) {
        if (target == null || target.isBlank()) {
            return "proyecto completo";
        }
        return target;
    }

    private void appendComponentSummary(StringBuilder output, List<SpringComponent> components) {
        Map<ComponentCategory, Long> counts = components.stream()
            .collect(Collectors.groupingBy(SpringComponent::category, Collectors.counting()));

        for (ComponentCategory category : Arrays.asList(
            ComponentCategory.CONTROLLER,
            ComponentCategory.SERVICE,
            ComponentCategory.REPOSITORY,
            ComponentCategory.DTO,
            ComponentCategory.EXCEPTION
        )) {
            long count = counts.getOrDefault(category, 0L);
            if (count > 0) {
                output.append("- ").append(labelFor(category)).append(": ").append(count).append("\n");
            }
        }
    }

    private void appendRiskSummary(StringBuilder output, CoverageAnalysis analysis) {
        boolean hasAnyRisk = false;
        if (!analysis.testInventory().hasAnyTests()) {
            output.append("- No se detectaron tests existentes en src/test/java.\n");
            hasAnyRisk = true;
        }
        if (analysis.components().stream().anyMatch(component -> component.category() == ComponentCategory.CONTROLLER)) {
            output.append("- Contratos HTTP: validar status codes, serializacion y errores esperados.\n");
            hasAnyRisk = true;
        }
        if (analysis.components().stream().anyMatch(component -> component.category() == ComponentCategory.SERVICE && component.hasRealLogic())) {
            output.append("- Logica de negocio: cubrir branches y excepciones sin levantar contexto Spring.\n");
            hasAnyRisk = true;
        }
        if (analysis.components().stream().anyMatch(SpringComponent::hasPersistenceSpecificLogic)) {
            output.append("- Persistencia: hay queries o acceso SQL que conviene validar con integracion realista.\n");
            hasAnyRisk = true;
        }

        if (!hasAnyRisk) {
            output.append("- No aparecen gaps obvios de alto riesgo para el target analizado.\n");
        }
    }

    private void appendRecommendations(
        StringBuilder output,
        String sectionTitle,
        List<TestRecommendation> recommendations,
        TestType type,
        int maxRecommendations
    ) {
        List<TestRecommendation> filtered = recommendations.stream()
            .filter(recommendation -> recommendation.type() == type)
            .toList();
        List<TestRecommendation> visible = filtered.stream()
            .limit(maxRecommendations)
            .toList();

        output.append("\n").append(sectionTitle).append("\n");
        if (filtered.isEmpty()) {
            output.append("- Sin recomendaciones prioritarias para este nivel.\n");
            return;
        }

        for (TestRecommendation recommendation : visible) {
            output.append("- [").append(labelFor(recommendation.priority())).append("] ")
                .append(recommendation.title())
                .append(" - ")
                .append(recommendation.detail())
                .append(" Herramienta sugerida: ")
                .append(recommendation.suggestedTool())
                .append(".\n");
        }
        int hidden = filtered.size() - visible.size();
        if (hidden > 0) {
            output.append("- ... ").append(hidden)
                .append(" recomendaciones mas ocultas. Usa --target para acotar por feature o --max-items para ampliar.\n");
        }
    }

    private String labelFor(ComponentCategory category) {
        return switch (category) {
            case CONTROLLER -> "Controllers";
            case SERVICE -> "Services";
            case REPOSITORY -> "Repositories";
            case DTO -> "DTOs";
            case EXCEPTION -> "Exceptions";
            case CONFIGURATION -> "Configuration";
            case OTHER -> "Otros";
        };
    }

    private String labelFor(Priority priority) {
        return switch (priority) {
            case HIGH -> "alta";
            case MEDIUM -> "media";
            case LOW -> "baja";
        };
    }
}
