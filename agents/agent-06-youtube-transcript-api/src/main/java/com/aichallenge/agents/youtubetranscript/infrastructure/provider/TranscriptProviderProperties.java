package com.aichallenge.agents.youtubetranscript.infrastructure.provider;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

@ConfigurationProperties(prefix = "app.transcript-provider")
public record TranscriptProviderProperties(
        int port,
        String baseUrl,
        Duration connectTimeout,
        Duration readTimeout
) {
}
