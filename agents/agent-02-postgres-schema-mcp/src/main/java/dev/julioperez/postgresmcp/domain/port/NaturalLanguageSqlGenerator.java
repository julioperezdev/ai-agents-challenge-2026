package dev.julioperez.postgresmcp.domain.port;

public interface NaturalLanguageSqlGenerator {

    String generateSql(String question, String schemaDocumentation, int limit);
}
