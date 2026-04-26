package dev.julioperez.postgresmcp.infrastructure.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.sql")
public record SqlProperties(
    int maxQueryLimit
) {
    public int normalizedMaxQueryLimit() {
        return maxQueryLimit <= 0 ? 50 : maxQueryLimit;
    }
}
