package dev.julioperez.postgresmcp.infrastructure.postgres;

import dev.julioperez.postgresmcp.domain.QueryResult;
import dev.julioperez.postgresmcp.domain.port.ReadOnlyQueryExecutor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Component
public class JdbcQueryExecutor implements ReadOnlyQueryExecutor {

    private final JdbcTemplate jdbcTemplate;

    public JdbcQueryExecutor(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public QueryResult execute(String sql) {
        return jdbcTemplate.query(sql, resultSet -> {
            ResultSetMetaData metadata = resultSet.getMetaData();
            int columnCount = metadata.getColumnCount();

            List<String> columns = new ArrayList<>();
            for (int index = 1; index <= columnCount; index++) {
                columns.add(metadata.getColumnLabel(index));
            }

            List<List<String>> rows = new ArrayList<>();
            while (resultSet.next()) {
                List<String> row = new ArrayList<>();
                for (int index = 1; index <= columnCount; index++) {
                    Object value = resultSet.getObject(index);
                    row.add(value == null ? "NULL" : String.valueOf(value));
                }
                rows.add(row);
            }

            return new QueryResult(sql, columns, rows);
        });
    }

    @Override
    public boolean isHealthy() {
        Integer value = jdbcTemplate.queryForObject("SELECT 1", Integer.class);
        return value != null && value == 1;
    }

    public List<String> sampleColumnNames(String tableName) {
        String sql = "SELECT * FROM public.%s LIMIT 0".formatted(quoteIdentifier(tableName));
        return jdbcTemplate.query(sql, resultSet -> {
            ResultSetMetaData metadata = resultSet.getMetaData();
            List<String> columns = new ArrayList<>();
            for (int index = 1; index <= metadata.getColumnCount(); index++) {
                columns.add(metadata.getColumnLabel(index).toLowerCase(Locale.ROOT));
            }
            return columns;
        });
    }

    public String quoteIdentifier(String identifier) {
        return "\"" + identifier.replace("\"", "\"\"") + "\"";
    }
}
