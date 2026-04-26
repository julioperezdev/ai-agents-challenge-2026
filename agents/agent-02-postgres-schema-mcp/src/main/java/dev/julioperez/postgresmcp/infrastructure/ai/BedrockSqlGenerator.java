package dev.julioperez.postgresmcp.infrastructure.ai;

import dev.julioperez.postgresmcp.domain.port.NaturalLanguageSqlGenerator;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;

public class BedrockSqlGenerator implements NaturalLanguageSqlGenerator {

    private final ChatClient chatClient;

    public BedrockSqlGenerator(ChatModel chatModel) {
        this.chatClient = ChatClient.create(chatModel);
    }

    @Override
    public String generateSql(String question, String schemaDocumentation, int limit) {
        String response = chatClient.prompt()
            .system("""
                You are a PostgreSQL SQL assistant.

                Your task is to convert the user's natural language question into a safe PostgreSQL SELECT query.

                Rules:
                - Use only the provided database schema.
                - Generate only one SQL statement.
                - Only SELECT or WITH queries are allowed.
                - Never generate INSERT, UPDATE, DELETE, DROP, ALTER, TRUNCATE, CREATE, GRANT, REVOKE, CALL, DO, EXECUTE, MERGE or COPY.
                - Do not invent tables or columns.
                - Always include a LIMIT if the query returns rows.
                - Prefer explicit JOINs.
                - Return only SQL. No markdown. No explanation.
                """)
            .user("""
                Database schema:
                %s

                User question:
                %s

                Limit to enforce:
                %d
                """.formatted(schemaDocumentation, question, limit))
            .call()
            .content();

        if (response == null || response.isBlank()) {
            throw new IllegalStateException("El modelo Bedrock no devolvió SQL.");
        }

        return response.trim();
    }
}
