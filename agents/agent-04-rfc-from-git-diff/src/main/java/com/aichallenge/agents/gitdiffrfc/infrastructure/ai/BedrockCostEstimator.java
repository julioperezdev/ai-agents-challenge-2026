package com.aichallenge.agents.gitdiffrfc.infrastructure.ai;

import org.springframework.stereotype.Component;

@Component
public class BedrockCostEstimator {

    private static final double INPUT_USD_PER_MILLION_TOKENS = 0.09;
    private static final double OUTPUT_USD_PER_MILLION_TOKENS = 0.39;
    private static final int APPROX_CHARS_PER_TOKEN = 4;

    public BedrockCostEstimate estimate(String prompt, int maxOutputTokens) {
        int inputTokens = estimateTokens(prompt);
        double inputCost = cost(inputTokens, INPUT_USD_PER_MILLION_TOKENS);
        double outputCost = cost(maxOutputTokens, OUTPUT_USD_PER_MILLION_TOKENS);
        return new BedrockCostEstimate(inputTokens, maxOutputTokens, inputCost, outputCost, inputCost + outputCost);
    }

    private int estimateTokens(String text) {
        if (text == null || text.isBlank()) {
            return 0;
        }
        return (int) Math.ceil((double) text.length() / APPROX_CHARS_PER_TOKEN);
    }

    private double cost(int tokens, double usdPerMillionTokens) {
        return tokens / 1_000_000.0 * usdPerMillionTokens;
    }
}
