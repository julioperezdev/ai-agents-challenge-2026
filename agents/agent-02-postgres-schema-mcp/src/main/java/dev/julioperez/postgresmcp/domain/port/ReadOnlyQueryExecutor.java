package dev.julioperez.postgresmcp.domain.port;

import dev.julioperez.postgresmcp.domain.QueryResult;

public interface ReadOnlyQueryExecutor {

    QueryResult execute(String sql);

    boolean isHealthy();
}
