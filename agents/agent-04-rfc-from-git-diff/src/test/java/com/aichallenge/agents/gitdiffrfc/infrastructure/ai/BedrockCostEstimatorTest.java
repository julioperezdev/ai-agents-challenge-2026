package com.aichallenge.agents.gitdiffrfc.infrastructure.ai;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class BedrockCostEstimatorTest {

    private final BedrockCostEstimator estimator = new BedrockCostEstimator();

    @Test
    void estimatesInputAndOutputCost() {
        BedrockCostEstimate estimate = estimator.estimate("a".repeat(40_000), 3_000);

        assertThat(estimate.inputTokens()).isEqualTo(10_000);
        assertThat(estimate.maxOutputTokens()).isEqualTo(3_000);
        assertThat(estimate.inputCostUsd()).isCloseTo(0.0009, within(0.000001));
        assertThat(estimate.maxOutputCostUsd()).isCloseTo(0.00117, within(0.000001));
        assertThat(estimate.maxTotalCostUsd()).isCloseTo(0.00207, within(0.000001));
    }

    private org.assertj.core.data.Offset<Double> within(double offset) {
        return org.assertj.core.data.Offset.offset(offset);
    }
}
