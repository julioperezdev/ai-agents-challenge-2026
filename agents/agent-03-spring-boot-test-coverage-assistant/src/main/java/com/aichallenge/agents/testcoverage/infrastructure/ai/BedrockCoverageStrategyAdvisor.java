package com.aichallenge.agents.testcoverage.infrastructure.ai;

import com.aichallenge.agents.testcoverage.domain.ComponentCategory;
import com.aichallenge.agents.testcoverage.domain.CoverageAnalysis;
import com.aichallenge.agents.testcoverage.domain.CoverageStrategy;
import com.aichallenge.agents.testcoverage.domain.SpringComponent;
import com.aichallenge.agents.testcoverage.domain.TestRecommendation;
import com.aichallenge.agents.testcoverage.domain.port.CoverageStrategyAdvisor;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import software.amazon.awssdk.core.SdkBytes;
import software.amazon.awssdk.services.bedrockruntime.BedrockRuntimeClient;
import software.amazon.awssdk.services.bedrockruntime.model.InvokeModelRequest;
import software.amazon.awssdk.services.bedrockruntime.model.InvokeModelResponse;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashSet;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Pattern;

public class BedrockCoverageStrategyAdvisor implements CoverageStrategyAdvisor, AutoCloseable {
    private static final int MAX_SNIPPET_CLASSES = 8;
    private static final int MAX_SNIPPET_CHARS_PER_CLASS = 2_600;
    private static final int MAX_SNIPPET_CHARS_TOTAL = 12_000;
    private static final Pattern REASONING_BLOCK_PATTERN = Pattern.compile("(?s)<reasoning>.*?</reasoning>");
    private static final Pattern JAVA_LINE_COMMENT_PATTERN = Pattern.compile("(?m)^\\s*//.*$");
    private static final Pattern JAVA_BLOCK_COMMENT_PATTERN = Pattern.compile("(?s)/\\*.*?\\*/");
    private static final Pattern IMPLEMENTATION_TEST_CASE_PATTERN = Pattern.compile(
        "(?m)^(\\s*-\\s+`)([A-Z][A-Za-z0-9_]*)(?<!Test)\\.([a-z][A-Za-z0-9_]*)"
    );

    private final BedrockConfig config;
    private final BedrockRuntimeClient client;
    private final ObjectMapper objectMapper;

    public BedrockCoverageStrategyAdvisor(BedrockConfig config) {
        this(
            config,
            BedrockRuntimeClient.builder()
                .region(config.region())
                .build(),
            new ObjectMapper()
        );
    }

    BedrockCoverageStrategyAdvisor(BedrockConfig config, BedrockRuntimeClient client, ObjectMapper objectMapper) {
        this.config = config;
        this.client = client;
        this.objectMapper = objectMapper;
    }

    @Override
    public CoverageStrategy advise(CoverageAnalysis analysis) throws IOException {
        String requestBody = buildRequestBody(analysis);
        InvokeModelResponse response = client.invokeModel(
            InvokeModelRequest.builder()
                .modelId(config.modelId())
                .contentType("application/json")
                .accept("application/json")
                .body(SdkBytes.fromUtf8String(requestBody))
                .build()
        );

        String content = sanitizeModelOutput(extractContent(response.body().asUtf8String()));
        if (content.isBlank()) {
            throw new IllegalStateException("Bedrock no devolvio una estrategia de testing valida.");
        }
        return new CoverageStrategy(content, true);
    }

    @Override
    public void close() {
        client.close();
    }

    String buildUserPrompt(CoverageAnalysis analysis) {
        StringBuilder builder = new StringBuilder();
        builder.append("Proyecto: ").append(analysis.projectPath().toAbsolutePath()).append("\n");
        builder.append("Target: ").append(analysis.target() == null || analysis.target().isBlank() ? "proyecto completo" : analysis.target()).append("\n");
        builder.append("Componentes detectados: ").append(analysis.components().size()).append("\n");
        builder.append("Tests detectados: ").append(analysis.testInventory().tests().size()).append("\n");
        builder.append("Gaps candidatos: ").append(analysis.recommendations().size()).append("\n\n");

        appendComponents(builder, "Controllers", analysis, ComponentCategory.CONTROLLER, 18);
        appendComponents(builder, "Services con logica", analysis, ComponentCategory.SERVICE, 24);
        appendComponents(builder, "Repositories con persistencia custom", analysis, ComponentCategory.REPOSITORY, 16);
        appendRecommendations(builder, analysis);
        appendSourceSnippets(builder, analysis);

        builder.append("""

            Genera un prompt final listo para pegarle a un agente coding que va a implementar los tests.
            El texto debe hablarle directamente al agente implementador, en imperativo y con alcance claro.
            Al inicio de "Prompt para agente implementador", incluye obligatoriamente:
            - Proyecto: <ruta absoluta del proyecto>
            - Target: <target analizado>
            No repitas una linea por cada clase si pertenecen al mismo flujo.
            Priorizacion esperada: riesgo de negocio, errores esperados, contratos HTTP, persistencia custom y costo/beneficio.
            Usa los snippets para nombrar metodos, branches, exceptions, queries y dependencias concretas.
            Si un caso de test sale de una inferencia por nombre y no del snippet, aclara que es una hipotesis.
            Trata "Tests detectados" como una senal general del target/proyecto, no como prueba de que un controller especifico ya tiene cobertura.
            Para endpoints, usa solamente rutas presentes en la lista de endpoints; si necesitas mencionar una ruta no detectada, escribe "confirmar path".
            Si hay repositories con persistencia custom, incluye al menos un test de integracion @DataJpaTest en "Integracion recomendada".
            No pongas repositories con persistencia custom en "No haria todavia" salvo que expliques que ya estan cubiertos.
            No recomiendes @SpringBootTest para combinar service + repository si un unit test o @DataJpaTest cubre el riesgo.
            Para persistencia custom o native queries, no sugieras H2 como default; usa la misma base que produccion o Testcontainers si el SQL depende del motor.
            En "Carga minima", prioriza endpoints HTTP. No propongas carga sobre services internos salvo evidencia de endpoint critico o requisito explicito.
            El alcance inicial debe tener como maximo 3 bloques de trabajo: unit, integracion HTTP y persistencia custom.
            No mezcles casos de una clase bajo el encabezado de otra clase.
            Responde en formato compacto, sin tablas, y termina todas las secciones.
            """);
        return builder.toString();
    }

    private String buildRequestBody(CoverageAnalysis analysis) throws IOException {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("model", config.modelId());
        body.put(
            "messages",
            List.of(
                Map.of("role", "system", "content", buildSystemPrompt()),
                Map.of("role", "user", "content", buildUserPrompt(analysis))
            )
        );
        body.put("temperature", 0.2);
        body.put("top_p", 0.9);
        body.put("max_completion_tokens", 2_200);
        body.put("stream", false);
        return objectMapper.writeValueAsString(body);
    }

    private String buildSystemPrompt() {
        return """
            Sos un agente experto en testing de APIs Spring Boot.
            Tu trabajo es convertir un analisis estatico en un prompt de implementacion para otro agente coding.
            Reglas obligatorias:
            - Responde siempre en espanol.
            - Se especifico y accionable para un developer backend.
            - No recomiendes testear todo.
            - No uses cobertura porcentual como objetivo.
            - No recomiendes @SpringBootTest por defecto si un slice test alcanza.
            - No recomiendes @SpringBootTest para flujos service + repository en el MVP; prefiere unit tests con mocks o @DataJpaTest segun el riesgo.
            - Agrupa clases por feature o flujo cuando sea mejor que repetir clase por clase.
            - Distingue unit tests, integracion HTTP, persistencia y smoke/load.
            - Si no hay evidencia suficiente para una recomendacion, dilo con cautela.
            - No afirmes que un controller o clase tiene test existente salvo que la entrada lo demuestre explicitamente.
            - No inventes paths HTTP: usa solo endpoints detectados o indica "confirmar path".
            - No declares cantidades exactas de endpoints salvo que aparezcan explicitamente como lista contada en la entrada; prefiere "varios endpoints" o lista rutas concretas detectadas.
            - Si detectas persistencia custom, incluye al menos un @DataJpaTest en Integracion recomendada y no lo mandes a No haria todavia.
            - Para persistencia custom, no propongas H2 como default. Recomienda Testcontainers o la misma base que produccion cuando haya SQL custom, native queries o comportamiento especifico del motor.
            - No inventes thresholds de performance como p95 < 200 ms ni volumenes grandes como 500/1000 requests.
            - Para carga minima, recomienda smoke inicial chico, por ejemplo 20-50 requests o "volumen bajo inicial", midiendo p95, tasa de error y baseline.
            - En carga minima, enfocate en endpoints HTTP. No propongas carga directa sobre services internos salvo que la entrada muestre un endpoint critico asociado.
            - No incluyas razonamiento interno, tags <reasoning>, ni texto fuera de las secciones.
            - Escribe directamente la respuesta final; no expliques como pensaste.
            - Cuando haya snippets, propone nombres de tests concretos y asserts esperados.
            - El output debe ser texto accionable para que otro agente implemente tests sin pedir mas contexto.
            - Incluye la ruta del proyecto y el target como las primeras dos lineas de la seccion "Prompt para agente implementador".
            - Indica que el agente implementador debe revisar el codigo real antes de escribir cada test y ajustar nombres, paths, mensajes y fixtures a lo que encuentre.
            - Define un alcance inicial chico: no implementar todo, empezar por los tests de mayor valor.
            - Incluye comandos de verificacion sugeridos al final.
            - No uses bloques de codigo fenced. Escribe comandos como bullets con backticks inline.
            - En Tests a implementar, lista como maximo 6 casos concretos en total, contando unit, integracion y persistencia.
            - Cada caso debe tener formato: `ClaseTest.nombreDelTest` - objetivo - asserts principales.
            - El prefijo antes del punto debe ser siempre el nombre de la clase de test, por ejemplo `AgentStrategyServiceTest.activateStrategy_success`, no `AgentStrategyService.activateStrategy_success`.
            - No uses subtareas anidadas que expandan un caso en muchos tests.
            - No uses tablas Markdown; consumen demasiados tokens y suelen cortarse.
            - Usa bullets cortos, con maximo 2 lineas por caso de test.
            - Prioriza como maximo 4 cosas que no harias todavia.
            - Si hay mas candidatos, agrupalos en una frase final en vez de listarlos todos.
            - Devuelve Markdown breve con estas secciones exactas:
              ## Prompt para agente implementador
              ## Alcance inicial
              ## Tests a implementar
              ## Reglas de implementacion
              ## Verificacion
              ## Fuera de alcance
            """;
    }

    private void appendComponents(
        StringBuilder builder,
        String title,
        CoverageAnalysis analysis,
        ComponentCategory category,
        int limit
    ) {
        List<SpringComponent> components = analysis.components().stream()
            .filter(component -> component.category() == category)
            .filter(component -> category != ComponentCategory.REPOSITORY || component.hasPersistenceSpecificLogic())
            .filter(component -> category != ComponentCategory.SERVICE || component.hasRealLogic())
            .sorted(Comparator.comparing(SpringComponent::className))
            .limit(limit)
            .toList();

        if (components.isEmpty()) {
            return;
        }

        builder.append(title).append(":\n");
        for (SpringComponent component : components) {
            builder.append("- ").append(component.className())
                .append(" | path=").append(component.sourcePath())
                .append(" | endpoints=").append(component.endpoints().stream().map(Object::toString).toList())
                .append(" | branches=").append(component.hasBranchingLogic())
                .append(" | exceptions=").append(component.hasExceptionHandling())
                .append(" | validations=").append(component.hasValidationAnnotations())
                .append(" | persistenceCustom=").append(component.hasPersistenceSpecificLogic())
                .append("\n");
        }
        builder.append("\n");
    }

    private void appendRecommendations(StringBuilder builder, CoverageAnalysis analysis) {
        builder.append("Recomendaciones candidatas del analizador local:\n");
        for (TestRecommendation recommendation : analysis.recommendations().stream().limit(40).toList()) {
            builder.append("- ")
                .append(recommendation.priority()).append(" | ")
                .append(recommendation.type()).append(" | ")
                .append(recommendation.title()).append(" | ")
                .append(recommendation.suggestedTool())
                .append("\n");
        }
    }

    private void appendSourceSnippets(StringBuilder builder, CoverageAnalysis analysis) {
        List<SpringComponent> components = selectSnippetComponents(analysis);
        if (components.isEmpty()) {
            return;
        }

        builder.append("\nSnippets de codigo fuente para recomendaciones accionables:\n");
        int remainingChars = MAX_SNIPPET_CHARS_TOTAL;
        for (SpringComponent component : components) {
            if (remainingChars <= 0) {
                break;
            }

            Optional<String> snippet = readSnippet(analysis, component, Math.min(MAX_SNIPPET_CHARS_PER_CLASS, remainingChars));
            if (snippet.isEmpty()) {
                continue;
            }

            builder.append("\n### ").append(component.className())
                .append(" (").append(component.category()).append(")\n")
                .append("Path: ").append(component.sourcePath()).append("\n")
                .append("```java\n")
                .append(snippet.get())
                .append("\n```\n");
            remainingChars -= snippet.get().length();
        }
    }

    private List<SpringComponent> selectSnippetComponents(CoverageAnalysis analysis) {
        Set<String> selectedClassNames = new LinkedHashSet<>();
        for (TestRecommendation recommendation : analysis.recommendations()) {
            String className = recommendation.title().split(":", 2)[0].trim();
            if (!className.isBlank()) {
                selectedClassNames.add(className);
            }
            if (selectedClassNames.size() >= MAX_SNIPPET_CLASSES) {
                break;
            }
        }

        if (selectedClassNames.size() < MAX_SNIPPET_CLASSES) {
            analysis.components().stream()
                .filter(component -> component.category() == ComponentCategory.CONTROLLER
                    || component.category() == ComponentCategory.SERVICE
                    || component.hasPersistenceSpecificLogic())
                .sorted(Comparator.comparing(SpringComponent::className))
                .map(SpringComponent::className)
                .forEach(className -> {
                    if (selectedClassNames.size() < MAX_SNIPPET_CLASSES) {
                        selectedClassNames.add(className);
                    }
                });
        }

        return selectedClassNames.stream()
            .flatMap(className -> analysis.components().stream()
                .filter(component -> component.className().equals(className))
                .findFirst()
                .stream())
            .toList();
    }

    private Optional<String> readSnippet(CoverageAnalysis analysis, SpringComponent component, int maxChars) {
        Path sourcePath = analysis.projectPath()
            .resolve(Path.of("src", "main", "java"))
            .resolve(component.sourcePath());
        if (!Files.isRegularFile(sourcePath)) {
            return Optional.empty();
        }

        try {
            String compacted = compactJavaSource(Files.readString(sourcePath));
            if (compacted.length() <= maxChars) {
                return Optional.of(compacted);
            }
            return Optional.of(compacted.substring(0, Math.max(0, maxChars - 80)).stripTrailing()
                + "\n// ... snippet truncated for prompt size");
        } catch (IOException exception) {
            return Optional.empty();
        }
    }

    private String compactJavaSource(String source) {
        String withoutBlockComments = JAVA_BLOCK_COMMENT_PATTERN.matcher(source).replaceAll("");
        String withoutLineComments = JAVA_LINE_COMMENT_PATTERN.matcher(withoutBlockComments).replaceAll("");
        StringBuilder compacted = new StringBuilder();
        boolean previousBlank = false;
        for (String rawLine : withoutLineComments.split("\\R")) {
            String line = rawLine.stripTrailing();
            boolean blank = line.isBlank();
            if (blank && previousBlank) {
                continue;
            }
            compacted.append(line).append("\n");
            previousBlank = blank;
        }
        return compacted.toString().strip();
    }

    private String extractContent(String responseBody) throws IOException {
        JsonNode root = objectMapper.readTree(responseBody);
        JsonNode choices = root.path("choices");
        if (!choices.isArray() || choices.isEmpty()) {
            throw new IllegalStateException("Bedrock devolvio una respuesta sin choices.");
        }

        JsonNode contentNode = choices.get(0).path("message").path("content");
        if (contentNode.isTextual()) {
            return contentNode.asText();
        }

        if (contentNode.isArray()) {
            StringBuilder builder = new StringBuilder();
            for (JsonNode item : contentNode) {
                if (item.isTextual()) {
                    builder.append(item.asText()).append("\n");
                } else if (item.has("text")) {
                    builder.append(item.path("text").asText()).append("\n");
                }
            }
            return builder.toString().trim();
        }

        if (contentNode.has("text")) {
            return contentNode.path("text").asText();
        }

        throw new IllegalStateException("No se pudo extraer el contenido textual de la respuesta de Bedrock.");
    }

    static String sanitizeModelOutput(String rawContent) {
        String sanitized = REASONING_BLOCK_PATTERN.matcher(rawContent)
            .replaceAll("")
            .replace("```markdown", "")
            .replace("```text", "")
            .replace("```", "")
            .trim();
        return normalizeImplementationTestCaseNames(sanitized);
    }

    private static String normalizeImplementationTestCaseNames(String content) {
        return IMPLEMENTATION_TEST_CASE_PATTERN.matcher(content)
            .replaceAll("$1$2Test.$3");
    }
}
