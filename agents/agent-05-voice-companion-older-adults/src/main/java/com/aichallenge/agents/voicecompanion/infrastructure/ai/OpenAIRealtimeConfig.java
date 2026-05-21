package com.aichallenge.agents.voicecompanion.infrastructure.ai;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(OpenAIRealtimeProperties.class)
public class OpenAIRealtimeConfig {}
