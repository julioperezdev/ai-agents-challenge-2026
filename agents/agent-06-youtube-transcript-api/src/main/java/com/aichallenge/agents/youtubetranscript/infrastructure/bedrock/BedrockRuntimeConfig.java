package com.aichallenge.agents.youtubetranscript.infrastructure.bedrock;

import com.aichallenge.agents.youtubetranscript.application.LearningAnalysisProperties;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.auth.credentials.ProfileCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.bedrockruntime.BedrockRuntimeClient;

import java.time.Duration;

@Configuration
@ConditionalOnProperty(prefix = "app.learning-analysis.bedrock", name = "enabled", havingValue = "true")
public class BedrockRuntimeConfig {

    @Bean
    public BedrockRuntimeClient bedrockRuntimeClient(LearningAnalysisProperties properties) {
        return BedrockRuntimeClient.builder()
                .region(Region.of(properties.getBedrock().getRegion()))
                .credentialsProvider(credentialsProvider(properties))
                .overrideConfiguration(config -> config.apiCallTimeout(Duration.ofMinutes(3)))
                .build();
    }

    private AwsCredentialsProvider credentialsProvider(LearningAnalysisProperties properties) {
        String profile = properties.getBedrock().getProfile();
        if (profile != null && !profile.isBlank()) {
            return ProfileCredentialsProvider.builder()
                    .profileName(profile)
                    .build();
        }
        return DefaultCredentialsProvider.create();
    }
}
