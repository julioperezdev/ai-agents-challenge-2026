package com.aichallenge.agents.testcoverage.infrastructure.ai;

import software.amazon.awssdk.regions.Region;

public record BedrockConfig(
    Region region,
    String modelId
) {
    public static final String DEFAULT_REGION = "us-east-1";
    public static final String DEFAULT_MODEL_ID = "openai.gpt-oss-20b-1:0";

    public static BedrockConfig fromEnvironment() {
        String regionValue = System.getenv("AWS_REGION");
        if (regionValue == null || regionValue.isBlank()) {
            regionValue = DEFAULT_REGION;
        }
        String modelId = System.getenv("BEDROCK_MODEL_ID");
        if (modelId == null || modelId.isBlank()) {
            modelId = DEFAULT_MODEL_ID;
        }

        try {
            return new BedrockConfig(Region.of(regionValue), modelId);
        } catch (RuntimeException exception) {
            throw new IllegalArgumentException(
                "AWS_REGION invalida: '" + regionValue + "'. Usa un valor como 'us-east-1'.",
                exception
            );
        }
    }
}
