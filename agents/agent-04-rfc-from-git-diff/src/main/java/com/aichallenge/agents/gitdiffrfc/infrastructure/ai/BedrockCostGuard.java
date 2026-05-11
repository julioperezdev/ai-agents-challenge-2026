package com.aichallenge.agents.gitdiffrfc.infrastructure.ai;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import org.springframework.stereotype.Component;

@Component
public class BedrockCostGuard {

    private static final double CONFIRMATION_THRESHOLD_USD = 1.00;

    public void confirmIfNeeded(BedrockCostEstimate estimate) {
        if (estimate.maxTotalCostUsd() <= CONFIRMATION_THRESHOLD_USD) {
            return;
        }

        String message = """
                Estimated Bedrock cost is above USD %.2f.
                Input: ~%,d tokens (USD %.4f)
                Output budget: up to %,d tokens (USD %.4f)
                Maximum estimated total: USD %.4f
                Continue with Bedrock? [y/N]:\s""".formatted(
                CONFIRMATION_THRESHOLD_USD,
                estimate.inputTokens(),
                estimate.inputCostUsd(),
                estimate.maxOutputTokens(),
                estimate.maxOutputCostUsd(),
                estimate.maxTotalCostUsd()
        );

        String answer = readAnswer(message);
        if (!"y".equalsIgnoreCase(answer) && !"yes".equalsIgnoreCase(answer)) {
            throw new IllegalStateException("Bedrock call cancelled because estimated cost exceeds USD %.2f.".formatted(CONFIRMATION_THRESHOLD_USD));
        }
    }

    private String readAnswer(String message) {
        System.err.print(message);
        try {
            if (System.console() != null) {
                String answer = System.console().readLine();
                return answer == null ? "" : answer.trim();
            }
            BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
            String answer = reader.readLine();
            return answer == null ? "" : answer.trim();
        } catch (IOException ex) {
            throw new IllegalStateException("Unable to read cost confirmation.", ex);
        }
    }
}
