package dev.julioperez.postgresmcp.domain;

public record ColumnMetadata(
    String name,
    String dataType,
    boolean nullable,
    String defaultValue
) {
}
