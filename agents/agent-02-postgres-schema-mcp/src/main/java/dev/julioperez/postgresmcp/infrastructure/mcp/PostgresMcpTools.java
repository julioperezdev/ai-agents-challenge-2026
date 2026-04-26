package dev.julioperez.postgresmcp.infrastructure.mcp;

import dev.julioperez.postgresmcp.application.NaturalLanguageQueryService;
import dev.julioperez.postgresmcp.application.QueryExecutionService;
import dev.julioperez.postgresmcp.application.SchemaInspectionService;
import dev.julioperez.postgresmcp.application.schema.SchemaDocumentationService;
import dev.julioperez.postgresmcp.domain.SchemaMetadata;
import dev.julioperez.postgresmcp.infrastructure.config.AiProperties;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Path;

@Component
public class PostgresMcpTools {

    private final SchemaInspectionService schemaInspectionService;
    private final SchemaDocumentationService schemaDocumentationService;
    private final NaturalLanguageQueryService naturalLanguageQueryService;
    private final QueryExecutionService queryExecutionService;
    private final AiProperties aiProperties;

    public PostgresMcpTools(
        SchemaInspectionService schemaInspectionService,
        SchemaDocumentationService schemaDocumentationService,
        NaturalLanguageQueryService naturalLanguageQueryService,
        QueryExecutionService queryExecutionService,
        AiProperties aiProperties
    ) {
        this.schemaInspectionService = schemaInspectionService;
        this.schemaDocumentationService = schemaDocumentationService;
        this.naturalLanguageQueryService = naturalLanguageQueryService;
        this.queryExecutionService = queryExecutionService;
        this.aiProperties = aiProperties;
    }

    @Tool(name = "get_public_schema", description = "Obtiene metadata estructurada del schema public")
    public SchemaMetadata getPublicSchema(
        @ToolParam(description = "Incluir índices") Boolean includeIndexes,
        @ToolParam(description = "Incluir constraints") Boolean includeConstraints,
        @ToolParam(description = "Incluir filas de ejemplo") Boolean includeSampleRows
    ) {
        return schemaInspectionService.inspect(
            Boolean.TRUE.equals(includeIndexes),
            includeConstraints == null || includeConstraints,
            Boolean.TRUE.equals(includeSampleRows)
        );
    }

    @Tool(name = "generate_schema_documentation", description = "Genera un archivo timestampado en docs/docs/DATABASE_SCHEMA_ddMMyy_hhmm.md desde PostgreSQL")
    public DocumentationResult generateSchemaDocumentation(
        @ToolParam(description = "Parámetro reservado; la salida siempre se genera en docs/docs con timestamp") String outputPath,
        @ToolParam(description = "Incluir preguntas y ejemplos de consulta") Boolean includeQueryExamples
    ) throws IOException {
        Path generatedPath = schemaDocumentationService.writeDocumentation(includeQueryExamples == null || includeQueryExamples);
        return new DocumentationResult(generatedPath.toString(), schemaDocumentationService.buildDocumentation(includeQueryExamples == null || includeQueryExamples));
    }

    @Tool(name = "ask_database", description = "Convierte una pregunta natural a SQL seguro, ejecuta la consulta y devuelve resultados")
    public String askDatabase(
        @ToolParam(description = "Pregunta en lenguaje natural") String question,
        @ToolParam(description = "Límite máximo de filas") Integer limit,
        @ToolParam(description = "Mostrar SQL generado") Boolean showSql
    ) {
        return naturalLanguageQueryService.askDatabase(
            question,
            limit,
            showSql == null || showSql
        );
    }

    @Tool(name = "explain_schema", description = "Explica el schema en lenguaje humano")
    public String explainSchema(
        @ToolParam(description = "Nivel de detalle: short, medium o detailed") String detailLevel
    ) {
        return schemaDocumentationService.explainSchema(detailLevel);
    }

    @Tool(name = "health_check", description = "Verifica conexión a PostgreSQL y estado básico del MCP")
    public HealthCheckResult healthCheck() {
        return new HealthCheckResult(
            "ok",
            queryExecutionService.isHealthy(),
            aiProperties.provider(),
            "PostgreSQL reachable and MCP tools loaded"
        );
    }

    public record DocumentationResult(
        String outputPath,
        String content
    ) {
    }

    public record HealthCheckResult(
        String status,
        boolean databaseReachable,
        String aiProvider,
        String message
    ) {
    }
}
