package com.aichallenge.agents.gitdiffrfc.infrastructure.ai;

import com.aichallenge.agents.gitdiffrfc.domain.ChangeSet;
import com.aichallenge.agents.gitdiffrfc.domain.RfcDocument;
import com.aichallenge.agents.gitdiffrfc.domain.RfcWriter;
import com.aichallenge.agents.gitdiffrfc.infrastructure.output.LocalRfcWriter;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.core.SdkBytes;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.bedrockruntime.BedrockRuntimeClient;
import software.amazon.awssdk.services.bedrockruntime.model.InvokeModelRequest;
import software.amazon.awssdk.services.bedrockruntime.model.InvokeModelResponse;

@Component
public class BedrockRfcWriter implements RfcWriter {

    private static final String DEFAULT_MODEL_ID = "openai.gpt-oss-20b-1:0";
    private static final int MAX_COMPLETION_TOKENS = 3000;

    private final ObjectMapper objectMapper;
    private final LocalRfcWriter fallbackWriter;
    private final BedrockRfcPromptBuilder promptBuilder;
    private final BedrockCostEstimator costEstimator;
    private final BedrockCostGuard costGuard;

    public BedrockRfcWriter(
            ObjectMapper objectMapper,
            LocalRfcWriter fallbackWriter,
            BedrockRfcPromptBuilder promptBuilder,
            BedrockCostEstimator costEstimator,
            BedrockCostGuard costGuard
    ) {
        this.objectMapper = objectMapper;
        this.fallbackWriter = fallbackWriter;
        this.promptBuilder = promptBuilder;
        this.costEstimator = costEstimator;
        this.costGuard = costGuard;
    }

    @Override
    public RfcDocument write(ChangeSet changeSet) {
        String region = env("AWS_REGION", "us-east-1");
        String modelId = env("BEDROCK_MODEL_ID", DEFAULT_MODEL_ID);
        String prompt = promptBuilder.build(changeSet);

        try {
            costGuard.confirmIfNeeded(costEstimator.estimate(prompt, MAX_COMPLETION_TOKENS));
        } catch (RuntimeException ex) {
            return fallbackWithNote(changeSet, ex.getMessage());
        }

        try (BedrockRuntimeClient client = BedrockRuntimeClient.builder()
                .region(Region.of(region))
                .credentialsProvider(DefaultCredentialsProvider.create())
                .overrideConfiguration(config -> config.apiCallTimeout(Duration.ofMinutes(2)))
                .build()) {
            InvokeModelResponse response = client.invokeModel(InvokeModelRequest.builder()
                    .modelId(modelId)
                    .contentType("application/json")
                    .accept("application/json")
                    .body(SdkBytes.fromString(requestBody(prompt), StandardCharsets.UTF_8))
                    .build());
            String markdown = extractMarkdown(response.body().asUtf8String());
            if (markdown.isBlank()) {
                return fallbackWithNote(changeSet, "Bedrock returned an empty response.");
            }
            return new RfcDocument(markdown);
        } catch (RuntimeException ex) {
            return fallbackWithNote(changeSet, "Bedrock generation failed: " + ex.getMessage());
        }
    }

    private String requestBody(String prompt) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("messages", List.of(
                Map.of("role", "system", "content", "You write practical RFCs for software engineering teams. Return only final Markdown and never expose reasoning."),
                Map.of("role", "user", "content", prompt)
        ));
        body.put("temperature", 0.2);
        body.put("max_completion_tokens", MAX_COMPLETION_TOKENS);
        try {
            return objectMapper.writeValueAsString(body);
        } catch (JsonProcessingException ex) {
            throw new IllegalStateException("Unable to serialize Bedrock request.", ex);
        }
    }

    private String extractMarkdown(String json) {
        try {
            JsonNode root = objectMapper.readTree(json);
            JsonNode outputText = root.at("/choices/0/message/content");
            if (!outputText.isMissingNode()) {
                return outputText.asText();
            }
            JsonNode contentText = root.at("/output/message/content/0/text");
            if (!contentText.isMissingNode()) {
                return contentText.asText();
            }
            JsonNode text = root.at("/content/0/text");
            if (!text.isMissingNode()) {
                return text.asText();
            }
            return json;
        } catch (JsonProcessingException ex) {
            return json;
        }
    }

    private RfcDocument fallbackWithNote(ChangeSet changeSet, String reason) {
        RfcDocument fallback = fallbackWriter.write(changeSet);
        return new RfcDocument(fallback.markdown() + "\n\n<!-- AI fallback: " + reason + " -->\n");
    }

    private String env(String name, String fallback) {
        String value = System.getenv(name);
        return value == null || value.isBlank() ? fallback : value;
    }
}
