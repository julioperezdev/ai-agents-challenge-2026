package com.aichallenge.agents.gitdiffrfc.infrastructure.ai;

public record BedrockCostEstimate(
        int inputTokens,
        int maxOutputTokens,
        double inputCostUsd,
        double maxOutputCostUsd,
        double maxTotalCostUsd
) {
}
