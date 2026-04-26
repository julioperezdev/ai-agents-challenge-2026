package dev.julioperez.postgresmcp.application;

import dev.julioperez.postgresmcp.domain.ForeignKeyMetadata;
import dev.julioperez.postgresmcp.domain.SchemaMetadata;
import dev.julioperez.postgresmcp.domain.TableMetadata;
import dev.julioperez.postgresmcp.domain.port.SchemaRepository;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class SchemaInspectionService {

    private final SchemaRepository schemaRepository;

    public SchemaInspectionService(SchemaRepository schemaRepository) {
        this.schemaRepository = schemaRepository;
    }

    public SchemaMetadata inspect(boolean includeIndexes, boolean includeConstraints, boolean includeSampleRows) {
        List<TableMetadata> tables = new ArrayList<>();
        for (String tableName : schemaRepository.findPublicTables()) {
            List<ForeignKeyMetadata> foreignKeys = includeConstraints
                ? schemaRepository.findForeignKeys(tableName)
                : List.of();
            List<String> primaryKeys = includeConstraints
                ? schemaRepository.findPrimaryKeys(tableName)
                : List.of();
            List<String> indexes = includeIndexes
                ? schemaRepository.findIndexes(tableName)
                : List.of();
            List<Map<String, Object>> sampleRows = includeSampleRows
                ? schemaRepository.sampleRows(tableName, 2)
                : List.of();

            tables.add(new TableMetadata(
                tableName,
                schemaRepository.countRows(tableName),
                schemaRepository.findColumns(tableName),
                primaryKeys,
                foreignKeys,
                indexes,
                sampleRows
            ));
        }

        return new SchemaMetadata(
            "public",
            Instant.now(),
            tables,
            includeConstraints ? schemaRepository.findAllForeignKeys() : List.of()
        );
    }
}
