package dev.julioperez.postgresmcp.infrastructure.postgres;

import dev.julioperez.postgresmcp.domain.ColumnMetadata;
import dev.julioperez.postgresmcp.domain.ForeignKeyMetadata;
import dev.julioperez.postgresmcp.domain.port.SchemaRepository;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

@Repository
public class PostgresSchemaRepository implements SchemaRepository {

    private final JdbcTemplate jdbcTemplate;
    private final JdbcQueryExecutor queryExecutor;

    public PostgresSchemaRepository(JdbcTemplate jdbcTemplate, JdbcQueryExecutor queryExecutor) {
        this.jdbcTemplate = jdbcTemplate;
        this.queryExecutor = queryExecutor;
    }

    @Override
    public List<String> findPublicTables() {
        return jdbcTemplate.queryForList("""
            SELECT table_name
            FROM information_schema.tables
            WHERE table_schema = 'public'
              AND table_type = 'BASE TABLE'
            ORDER BY table_name
            """, String.class);
    }

    @Override
    public List<ColumnMetadata> findColumns(String tableName) {
        return jdbcTemplate.query("""
            SELECT column_name, data_type, is_nullable, column_default
            FROM information_schema.columns
            WHERE table_schema = 'public'
              AND table_name = ?
            ORDER BY ordinal_position
            """,
            (resultSet, rowNum) -> new ColumnMetadata(
                resultSet.getString("column_name"),
                resultSet.getString("data_type"),
                "YES".equalsIgnoreCase(resultSet.getString("is_nullable")),
                resultSet.getString("column_default")
            ),
            tableName
        );
    }

    @Override
    public List<String> findPrimaryKeys(String tableName) {
        return jdbcTemplate.queryForList("""
            SELECT kcu.column_name
            FROM information_schema.table_constraints tc
            JOIN information_schema.key_column_usage kcu
              ON tc.constraint_name = kcu.constraint_name
             AND tc.table_schema = kcu.table_schema
            WHERE tc.table_schema = 'public'
              AND tc.table_name = ?
              AND tc.constraint_type = 'PRIMARY KEY'
            ORDER BY kcu.ordinal_position
            """, String.class, tableName);
    }

    @Override
    public List<ForeignKeyMetadata> findForeignKeys(String tableName) {
        return jdbcTemplate.query("""
            SELECT
              tc.constraint_name,
              tc.table_name AS source_table,
              kcu.column_name AS source_column,
              ccu.table_name AS target_table,
              ccu.column_name AS target_column
            FROM information_schema.table_constraints tc
            JOIN information_schema.key_column_usage kcu
              ON tc.constraint_name = kcu.constraint_name
             AND tc.table_schema = kcu.table_schema
            JOIN information_schema.constraint_column_usage ccu
              ON ccu.constraint_name = tc.constraint_name
             AND ccu.table_schema = tc.table_schema
            WHERE tc.constraint_type = 'FOREIGN KEY'
              AND tc.table_schema = 'public'
              AND tc.table_name = ?
            ORDER BY tc.constraint_name, kcu.ordinal_position
            """,
            (resultSet, rowNum) -> new ForeignKeyMetadata(
                resultSet.getString("constraint_name"),
                resultSet.getString("source_table"),
                resultSet.getString("source_column"),
                resultSet.getString("target_table"),
                resultSet.getString("target_column")
            ),
            tableName
        );
    }

    @Override
    public List<ForeignKeyMetadata> findAllForeignKeys() {
        return jdbcTemplate.query("""
            SELECT
              tc.constraint_name,
              tc.table_name AS source_table,
              kcu.column_name AS source_column,
              ccu.table_name AS target_table,
              ccu.column_name AS target_column
            FROM information_schema.table_constraints tc
            JOIN information_schema.key_column_usage kcu
              ON tc.constraint_name = kcu.constraint_name
             AND tc.table_schema = kcu.table_schema
            JOIN information_schema.constraint_column_usage ccu
              ON ccu.constraint_name = tc.constraint_name
             AND ccu.table_schema = tc.table_schema
            WHERE tc.constraint_type = 'FOREIGN KEY'
              AND tc.table_schema = 'public'
            ORDER BY tc.table_name, tc.constraint_name, kcu.ordinal_position
            """,
            (resultSet, rowNum) -> new ForeignKeyMetadata(
                resultSet.getString("constraint_name"),
                resultSet.getString("source_table"),
                resultSet.getString("source_column"),
                resultSet.getString("target_table"),
                resultSet.getString("target_column")
            )
        );
    }

    @Override
    public List<String> findIndexes(String tableName) {
        return jdbcTemplate.queryForList("""
            SELECT indexname
            FROM pg_indexes
            WHERE schemaname = 'public'
              AND tablename = ?
            ORDER BY indexname
            """, String.class, tableName);
    }

    @Override
    public long countRows(String tableName) {
        String sql = "SELECT COUNT(*) FROM public.%s".formatted(queryExecutor.quoteIdentifier(tableName));
        Long count = jdbcTemplate.queryForObject(sql, Long.class);
        return count == null ? 0L : count;
    }

    @Override
    public List<Map<String, Object>> sampleRows(String tableName, int limit) {
        String sql = "SELECT * FROM public.%s LIMIT %d"
            .formatted(queryExecutor.quoteIdentifier(tableName), Math.max(1, limit));
        return jdbcTemplate.queryForList(sql);
    }
}
