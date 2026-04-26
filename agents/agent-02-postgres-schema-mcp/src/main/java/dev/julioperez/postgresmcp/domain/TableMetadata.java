package dev.julioperez.postgresmcp.domain;

import java.util.List;
import java.util.Map;

public record TableMetadata(
    String name,
    long approximateRowCount,
    List<ColumnMetadata> columns,
    List<String> primaryKeys,
    List<ForeignKeyMetadata> foreignKeys,
    List<String> indexes,
    List<Map<String, Object>> sampleRows
) {
}
