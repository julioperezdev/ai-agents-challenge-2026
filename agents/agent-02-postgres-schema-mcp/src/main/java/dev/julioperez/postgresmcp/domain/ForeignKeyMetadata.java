package dev.julioperez.postgresmcp.domain;

public record ForeignKeyMetadata(
    String name,
    String sourceTable,
    String sourceColumn,
    String targetTable,
    String targetColumn
) {
}
