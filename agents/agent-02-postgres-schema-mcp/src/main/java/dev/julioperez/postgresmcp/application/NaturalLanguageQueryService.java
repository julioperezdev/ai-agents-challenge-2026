package dev.julioperez.postgresmcp.application;

import dev.julioperez.postgresmcp.application.schema.SchemaDocumentationService;
import dev.julioperez.postgresmcp.domain.QueryResult;
import dev.julioperez.postgresmcp.domain.port.NaturalLanguageSqlGenerator;
import dev.julioperez.postgresmcp.shared.ConsoleTableFormatter;
import org.springframework.stereotype.Service;

@Service
public class NaturalLanguageQueryService {

    private final SchemaDocumentationService schemaDocumentationService;
    private final NaturalLanguageSqlGenerator naturalLanguageSqlGenerator;
    private final SqlSafetyValidator sqlSafetyValidator;
    private final QueryExecutionService queryExecutionService;
    private final ConsoleTableFormatter consoleTableFormatter;

    public NaturalLanguageQueryService(
        SchemaDocumentationService schemaDocumentationService,
        NaturalLanguageSqlGenerator naturalLanguageSqlGenerator,
        SqlSafetyValidator sqlSafetyValidator,
        QueryExecutionService queryExecutionService,
        ConsoleTableFormatter consoleTableFormatter
    ) {
        this.schemaDocumentationService = schemaDocumentationService;
        this.naturalLanguageSqlGenerator = naturalLanguageSqlGenerator;
        this.sqlSafetyValidator = sqlSafetyValidator;
        this.queryExecutionService = queryExecutionService;
        this.consoleTableFormatter = consoleTableFormatter;
    }

    public String askDatabase(String question, Integer limit, boolean showSql) {
        String schemaDocumentation = schemaDocumentationService.buildDocumentation(true);
        String generatedSql = naturalLanguageSqlGenerator.generateSql(question, schemaDocumentation, limit == null ? 50 : limit);
        String safeSql = sqlSafetyValidator.validateAndNormalize(generatedSql, limit);
        QueryResult result = queryExecutionService.executeReadOnlyQuery(safeSql);

        StringBuilder builder = new StringBuilder();
        builder.append("Question:\n").append(question).append("\n\n");
        if (showSql) {
            builder.append("Generated SQL:\n").append(safeSql).append("\n\n");
        }
        builder.append("Results:\n").append(consoleTableFormatter.format(result));
        return builder.toString();
    }
}
