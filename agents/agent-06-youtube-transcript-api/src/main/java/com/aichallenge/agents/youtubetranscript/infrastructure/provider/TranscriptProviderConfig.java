package com.aichallenge.agents.youtubetranscript.infrastructure.provider;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
@EnableConfigurationProperties(TranscriptProviderProperties.class)
public class TranscriptProviderConfig {

    @Bean
    RestTemplate transcriptProviderRestTemplate(RestTemplateBuilder builder, TranscriptProviderProperties properties) {
        return builder
                .connectTimeout(properties.connectTimeout())
                .readTimeout(properties.readTimeout())
                .build();
    }
}
