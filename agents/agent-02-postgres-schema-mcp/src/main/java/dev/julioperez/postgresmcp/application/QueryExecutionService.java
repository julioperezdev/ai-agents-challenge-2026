package dev.julioperez.postgresmcp.application;

import dev.julioperez.postgresmcp.domain.QueryResult;
import dev.julioperez.postgresmcp.domain.port.ReadOnlyQueryExecutor;
import org.springframework.stereotype.Service;

@Service
public class QueryExecutionService {

    private final ReadOnlyQueryExecutor readOnlyQueryExecutor;

    public QueryExecutionService(ReadOnlyQueryExecutor readOnlyQueryExecutor) {
        this.readOnlyQueryExecutor = readOnlyQueryExecutor;
    }

    public QueryResult executeReadOnlyQuery(String sql) {
        return readOnlyQueryExecutor.execute(sql);
    }

    public boolean isHealthy() {
        return readOnlyQueryExecutor.isHealthy();
    }
}
