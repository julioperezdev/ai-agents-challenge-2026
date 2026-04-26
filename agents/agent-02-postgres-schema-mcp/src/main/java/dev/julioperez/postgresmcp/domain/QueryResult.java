package dev.julioperez.postgresmcp.domain;

import java.util.List;

public record QueryResult(
    String sql,
    List<String> columns,
    List<List<String>> rows
) {
    public int rowCount() {
        return rows.size();
    }
}
