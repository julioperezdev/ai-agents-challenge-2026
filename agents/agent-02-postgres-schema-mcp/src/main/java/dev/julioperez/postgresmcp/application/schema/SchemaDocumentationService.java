package dev.julioperez.postgresmcp.application.schema;

import dev.julioperez.postgresmcp.application.SchemaInspectionService;
import dev.julioperez.postgresmcp.domain.DemoDatasetSummary;
import dev.julioperez.postgresmcp.domain.ForeignKeyMetadata;
import dev.julioperez.postgresmcp.domain.SchemaMetadata;
import dev.julioperez.postgresmcp.domain.TableMetadata;
import dev.julioperez.postgresmcp.domain.port.DemoDatasetSummaryRepository;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Service
public class SchemaDocumentationService {

    private static final DateTimeFormatter DOCUMENTATION_TIMESTAMP_FORMATTER = DateTimeFormatter.ofPattern("ddMMyy_HHmm");

    private final SchemaInspectionService schemaInspectionService;
    private final DemoDatasetSummaryRepository demoDatasetSummaryRepository;

    public SchemaDocumentationService(
        SchemaInspectionService schemaInspectionService,
        DemoDatasetSummaryRepository demoDatasetSummaryRepository
    ) {
        this.schemaInspectionService = schemaInspectionService;
        this.demoDatasetSummaryRepository = demoDatasetSummaryRepository;
    }

    public String buildDocumentation(boolean includeQueryExamples) {
        return buildDocumentation(schemaInspectionService.inspect(true, true, false), includeQueryExamples);
    }

    public String buildDocumentation(SchemaMetadata schemaMetadata, boolean includeQueryExamples) {
        DemoDatasetSummary snapshot = demoDatasetSummaryRepository.getSummary();
        StringBuilder builder = new StringBuilder();
        builder.append("# Database Schema Documentation\n\n");
        builder.append("## Overview\n\n");
        builder.append("Schema inspeccionado: `").append(schemaMetadata.schemaName()).append("`.\n\n");
        builder.append("Este schema representa un sistema simple de ventas. ");
        builder.append("Los clientes crean órdenes, las órdenes contienen productos mediante `order_items` ");
        builder.append("y cada orden puede tener pagos asociados.\n\n");
        builder.append("Dataset demo cargado: ").append(snapshot.summaryLine()).append("\n\n");
        if (!snapshot.recentCustomers().isEmpty()) {
            builder.append("Clientes más recientes: ").append(String.join(", ", snapshot.recentCustomers())).append(".\n\n");
        }

        builder.append("## Tables\n\n");
        for (TableMetadata table : schemaMetadata.tables()) {
            builder.append("### ").append(table.name()).append("\n\n");
            builder.append("Purpose: ").append(describeTable(table.name())).append("\n\n");
            builder.append("Columns:\n");
            for (var column : table.columns()) {
                builder.append("- `").append(column.name()).append("` — ").append(column.dataType());
                builder.append(column.nullable() ? ", nullable" : ", not null");
                if (column.defaultValue() != null) {
                    builder.append(", default `").append(column.defaultValue()).append("`");
                }
                builder.append("\n");
            }
            builder.append("\n");
            builder.append("Primary key: ");
            builder.append(table.primaryKeys().isEmpty() ? "N/A" : String.join(", ", table.primaryKeys()));
            builder.append("\n\n");

            builder.append("Foreign keys:\n");
            if (table.foreignKeys().isEmpty()) {
                builder.append("- N/A\n");
            } else {
                for (ForeignKeyMetadata foreignKey : table.foreignKeys()) {
                    builder.append("- `")
                        .append(foreignKey.sourceColumn())
                        .append("` -> `")
                        .append(foreignKey.targetTable())
                        .append(".")
                        .append(foreignKey.targetColumn())
                        .append("`\n");
                }
            }
            builder.append("\n");

            if (includeQueryExamples) {
                builder.append("Common queries:\n");
                for (String example : suggestQueries(table.name())) {
                    builder.append("- ").append(example).append("\n");
                }
                builder.append("\n");
            }
        }

        builder.append("## Relationships\n\n");
        for (ForeignKeyMetadata relationship : schemaMetadata.relationships()) {
            builder.append("- `")
                .append(relationship.sourceTable())
                .append(".")
                .append(relationship.sourceColumn())
                .append("` -> `")
                .append(relationship.targetTable())
                .append(".")
                .append(relationship.targetColumn())
                .append("`\n");
        }
        builder.append("\n");

        builder.append("## Suggested Natural Language Questions\n\n");
        builder.append("- ¿Qué tablas existen en la base de datos?\n");
        builder.append("- Muéstrame los 10 clientes más recientes.\n");
        builder.append("- Muéstrame las órdenes pagadas con el nombre del cliente.\n");
        builder.append("- ¿Cuáles son los productos más vendidos?\n");
        builder.append("- ¿Cuánto dinero se ha aprobado en pagos por cliente?\n\n");

        builder.append("## SQL Safety Rules\n\n");
        builder.append("- Solo se permiten consultas `SELECT` o `WITH`.\n");
        builder.append("- No se permiten `INSERT`, `UPDATE`, `DELETE`, `DROP`, `ALTER`, `TRUNCATE`, `CREATE`, `GRANT`, `REVOKE`, `COPY`, `CALL`, `DO`, `EXECUTE` ni `MERGE`.\n");
        builder.append("- Siempre se debe devolver una sola consulta.\n");
        builder.append("- Si no existe `LIMIT`, el sistema lo agrega automáticamente.\n");
        return builder.toString();
    }

    public Path writeDocumentation(boolean includeQueryExamples) throws IOException {
        Path outputPath = buildDocumentationPath();
        String content = buildDocumentation(includeQueryExamples);
        Files.createDirectories(outputPath.toAbsolutePath().normalize().getParent());
        Files.writeString(outputPath, content);
        return outputPath.toAbsolutePath().normalize();
    }

    public String explainSchema(String detailLevel) {
        SchemaMetadata metadata = schemaInspectionService.inspect(false, true, false);
        int tableCount = metadata.tables().size();
        DemoDatasetSummary snapshot = demoDatasetSummaryRepository.getSummary();
        return switch (detailLevel == null ? "medium" : detailLevel.toLowerCase()) {
            case "short" -> "Esta base representa un sistema simple de ventas con clientes, productos, órdenes, ítems y pagos.";
            case "detailed" -> "Esta base representa un sistema de ventas/e-commerce. Tiene %d tablas en el schema public. "
                .formatted(tableCount)
                + "Los clientes (`customers`) generan órdenes (`orders`), cada orden se descompone en líneas (`order_items`) "
                + "que referencian productos (`products`) y además puede tener pagos (`payments`) con distintos estados. "
                + "El dataset demo contiene " + snapshot.summaryLine() + " "
                + "Sirve para responder preguntas sobre ventas, productos más vendidos, pagos aprobados, órdenes canceladas y actividad por cliente.";
            default -> "Esta base representa un sistema simple de ventas. Los clientes crean órdenes, las órdenes contienen productos a través de "
                + "`order_items` y cada orden puede tener pagos asociados. Sirve para responder preguntas sobre ventas, clientes, "
                + "productos más vendidos y pagos aprobados o rechazados. "
                + "Actualmente el dataset demo incluye " + snapshot.summaryLine();
        };
    }

    private String describeTable(String tableName) {
        return switch (tableName) {
            case "customers" -> "Clientes registrados en el sistema.";
            case "products" -> "Catálogo de productos disponibles para vender.";
            case "orders" -> "Órdenes creadas por los clientes.";
            case "order_items" -> "Detalle de productos y cantidades por orden.";
            case "payments" -> "Pagos realizados o intentados sobre órdenes.";
            default -> "Tabla de negocio del dominio demo.";
        };
    }

    private List<String> suggestQueries(String tableName) {
        List<String> queries = new ArrayList<>();
        switch (tableName) {
            case "customers" -> {
                queries.add("Listar clientes más recientes.");
                queries.add("Buscar clientes con más órdenes pagadas.");
            }
            case "products" -> {
                queries.add("Mostrar productos más vendidos.");
                queries.add("Listar productos activos por categoría.");
            }
            case "orders" -> {
                queries.add("Mostrar órdenes pagadas con nombre del cliente.");
                queries.add("Listar órdenes canceladas.");
            }
            case "order_items" -> {
                queries.add("Calcular cantidad total vendida por producto.");
            }
            case "payments" -> {
                queries.add("Sumar pagos aprobados por cliente.");
                queries.add("Listar pagos rechazados.");
            }
            default -> queries.add("Explorar contenido de la tabla.");
        }
        return queries;
    }

    private Path buildDocumentationPath() {
        String timestamp = LocalDateTime.now().format(DOCUMENTATION_TIMESTAMP_FORMATTER);
        return Path.of("./docs/docs/DATABASE_SCHEMA_%s.md".formatted(timestamp));
    }
}
