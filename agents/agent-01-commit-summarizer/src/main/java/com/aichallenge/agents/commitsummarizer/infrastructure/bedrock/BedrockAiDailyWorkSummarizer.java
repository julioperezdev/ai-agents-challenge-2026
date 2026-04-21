package com.aichallenge.agents.commitsummarizer.infrastructure.bedrock;

import com.aichallenge.agents.commitsummarizer.application.DailyWorkSummaryRequest;
import com.aichallenge.agents.commitsummarizer.application.DailyWorkSummarizer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import software.amazon.awssdk.core.SdkBytes;
import software.amazon.awssdk.services.bedrockruntime.BedrockRuntimeClient;
import software.amazon.awssdk.services.bedrockruntime.model.InvokeModelRequest;
import software.amazon.awssdk.services.bedrockruntime.model.InvokeModelResponse;

import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class BedrockAiDailyWorkSummarizer implements DailyWorkSummarizer, AutoCloseable {
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE;

    private final BedrockConfig config;
    private final BedrockRuntimeClient client;
    private final ObjectMapper objectMapper;

    public BedrockAiDailyWorkSummarizer(BedrockConfig config) {
        this(
            config,
            BedrockRuntimeClient.builder()
                .region(config.region())
                .build(),
            new ObjectMapper()
        );
    }

    BedrockAiDailyWorkSummarizer(BedrockConfig config, BedrockRuntimeClient client, ObjectMapper objectMapper) {
        this.config = config;
        this.client = client;
        this.objectMapper = objectMapper;
    }

    @Override
    public List<String> summarize(DailyWorkSummaryRequest request) throws IOException {
        if (request.meetings().isEmpty() && request.activityLines().isEmpty()) {
            return List.of();
        }

        String requestBody = buildRequestBody(request);
        InvokeModelResponse response = client.invokeModel(
            InvokeModelRequest.builder()
                .modelId(config.modelId())
                .contentType("application/json")
                .accept("application/json")
                .body(SdkBytes.fromUtf8String(requestBody))
                .build()
        );

        String rawContent = extractContent(response.body().asUtf8String());
        List<String> summaryLines = normalizeSummaryLines(rawContent);

        if (summaryLines.isEmpty()) {
            throw new IllegalStateException("Bedrock no devolvió bullets válidos para el resumen diario.");
        }

        return summaryLines;
    }

    @Override
    public void close() {
        client.close();
    }

    static List<String> normalizeSummaryLines(String rawContent) {
        String withoutReasoning = rawContent
            .replaceAll("(?s)<reasoning>.*?</reasoning>", "")
            .replace("```text", "")
            .replace("```markdown", "")
            .replace("```", "")
            .trim();

        if (withoutReasoning.isBlank()) {
            return List.of();
        }

        List<String> lines = new ArrayList<>();
        for (String rawLine : withoutReasoning.split("\\R")) {
            String line = rawLine.trim();
            if (line.isBlank()) {
                continue;
            }

            line = line.replaceFirst("^[\\-*•–—]\\s+", "");
            line = line.replaceFirst("^\\d+[.)]\\s+", "");
            line = line.trim();

            if (!line.isBlank()) {
                lines.add(line);
            }
        }

        if (lines.isEmpty() && !withoutReasoning.isBlank()) {
            return List.of(withoutReasoning);
        }

        return lines;
    }

    private String buildRequestBody(DailyWorkSummaryRequest request) throws IOException {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("model", config.modelId());
        body.put(
            "messages",
            List.of(
                Map.of("role", "system", "content", buildSystemPrompt()),
                Map.of("role", "user", "content", buildUserPrompt(request))
            )
        );
        body.put("temperature", 0.2);
        body.put("top_p", 0.9);
        body.put("max_completion_tokens", 350);
        body.put("stream", false);
        return objectMapper.writeValueAsString(body);
    }

    private String buildSystemPrompt() {
        return """
            Sos un asistente que transforma commits y actividades técnicas en avances laborales breves para un time tracker.
            Reglas obligatorias:
            - Respondé siempre en español.
            - Devolvé únicamente una lista de bullets.
            - Cada línea debe empezar exactamente con "- ".
            - No incluyas títulos, fechas, explicaciones, introducciones, markdown extra, bloques de código ni texto fuera de la lista.
            - No inventes trabajo que no esté presente en la entrada.
            - Si hay commits repetidos o muy parecidos, consolidalos en menos bullets.
            - Traducí mensajes en inglés al español profesional.
            - No menciones hashes, tickets ni ruido técnico innecesario.
            - El tono debe ser breve, profesional y listo para copiar y pegar.
            """;
    }

    private String buildUserPrompt(DailyWorkSummaryRequest request) {
        StringBuilder builder = new StringBuilder();
        builder.append("Fecha: ").append(request.date().format(DATE_FORMATTER)).append("\n");
        builder.append("Contexto: ");
        builder.append(request.multiRepo() ? "actividades de múltiples repositorios" : "actividades de un repositorio");
        builder.append(".\n\n");

        if (!request.meetings().isEmpty()) {
            builder.append("Reuniones del día:\n");
            for (String meeting : request.meetings()) {
                builder.append("- ").append(meeting).append("\n");
            }
            builder.append("\n");
        }

        builder.append("Actividades técnicas del día:\n");
        for (String line : request.activityLines()) {
            builder.append("- ").append(line).append("\n");
        }
        builder.append("\n");
        builder.append("Devolvé solo la lista final de bullets en español.");
        return builder.toString();
    }

    private String extractContent(String responseBody) throws IOException {
        JsonNode root = objectMapper.readTree(responseBody);
        JsonNode choices = root.path("choices");
        if (!choices.isArray() || choices.isEmpty()) {
            throw new IllegalStateException("Bedrock devolvió una respuesta sin choices.");
        }

        JsonNode contentNode = choices.get(0).path("message").path("content");
        if (contentNode.isTextual()) {
            return contentNode.asText();
        }

        if (contentNode.isArray()) {
            StringBuilder builder = new StringBuilder();
            for (JsonNode item : contentNode) {
                if (item.isTextual()) {
                    builder.append(item.asText()).append("\n");
                } else if (item.has("text")) {
                    builder.append(item.path("text").asText()).append("\n");
                }
            }
            return builder.toString().trim();
        }

        if (contentNode.has("text")) {
            return contentNode.path("text").asText();
        }

        throw new IllegalStateException("No se pudo extraer el contenido textual de la respuesta de Bedrock.");
    }
}
