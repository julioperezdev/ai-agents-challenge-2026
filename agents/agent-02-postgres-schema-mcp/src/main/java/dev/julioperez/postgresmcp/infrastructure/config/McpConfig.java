package dev.julioperez.postgresmcp.infrastructure.config;

import dev.julioperez.postgresmcp.infrastructure.mcp.PostgresMcpTools;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.ai.tool.method.MethodToolCallbackProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class McpConfig {

    @Bean
    public ToolCallbackProvider postgresToolCallbacks(PostgresMcpTools postgresMcpTools) {
        return MethodToolCallbackProvider.builder()
            .toolObjects(postgresMcpTools)
            .build();
    }
}
