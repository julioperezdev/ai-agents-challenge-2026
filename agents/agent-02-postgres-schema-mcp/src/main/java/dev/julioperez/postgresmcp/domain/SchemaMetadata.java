package dev.julioperez.postgresmcp.domain;

import java.time.Instant;
import java.util.List;

public record SchemaMetadata(
    String schemaName,
    Instant generatedAt,
    List<TableMetadata> tables,
    List<ForeignKeyMetadata> relationships
) {
}
