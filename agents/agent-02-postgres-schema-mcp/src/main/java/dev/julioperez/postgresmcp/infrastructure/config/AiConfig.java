package dev.julioperez.postgresmcp.infrastructure.config;

import dev.julioperez.postgresmcp.domain.port.NaturalLanguageSqlGenerator;
import dev.julioperez.postgresmcp.infrastructure.ai.BedrockSqlGenerator;
import dev.julioperez.postgresmcp.infrastructure.ai.MockSqlGenerator;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AiConfig {

    @Bean
    public NaturalLanguageSqlGenerator naturalLanguageSqlGenerator(
        AiProperties aiProperties,
        ObjectProvider<ChatModel> chatModelProvider
    ) {
        if (aiProperties.isMock()) {
            return new MockSqlGenerator();
        }
        if (!aiProperties.isBedrock()) {
            throw new IllegalArgumentException("Proveedor AI no soportado: " + aiProperties.provider());
        }

        ChatModel chatModel = chatModelProvider.getIfAvailable();
        if (chatModel == null) {
            throw new IllegalStateException("No se pudo inicializar el ChatModel de Bedrock.");
        }
        return new BedrockSqlGenerator(chatModel);
    }
}
