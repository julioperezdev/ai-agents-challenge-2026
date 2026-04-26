package dev.julioperez.postgresmcp.infrastructure.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.ai")
public record AiProperties(
    String provider
) {
    public boolean isMock() {
        return provider == null || provider.isBlank() || "mock".equalsIgnoreCase(provider);
    }

    public boolean isBedrock() {
        return "bedrock".equalsIgnoreCase(provider);
    }
}
