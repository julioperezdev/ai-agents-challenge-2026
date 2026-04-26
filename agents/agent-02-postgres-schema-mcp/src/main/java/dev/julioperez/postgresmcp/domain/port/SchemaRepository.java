package dev.julioperez.postgresmcp.domain.port;

import dev.julioperez.postgresmcp.domain.ColumnMetadata;
import dev.julioperez.postgresmcp.domain.ForeignKeyMetadata;

import java.util.List;
import java.util.Map;

public interface SchemaRepository {

    List<String> findPublicTables();

    List<ColumnMetadata> findColumns(String tableName);

    List<String> findPrimaryKeys(String tableName);

    List<ForeignKeyMetadata> findForeignKeys(String tableName);

    List<ForeignKeyMetadata> findAllForeignKeys();

    List<String> findIndexes(String tableName);

    long countRows(String tableName);

    List<Map<String, Object>> sampleRows(String tableName, int limit);
}
