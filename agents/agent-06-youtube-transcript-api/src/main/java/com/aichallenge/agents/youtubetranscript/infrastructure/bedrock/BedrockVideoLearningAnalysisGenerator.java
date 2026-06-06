package com.aichallenge.agents.youtubetranscript.infrastructure.bedrock;

import com.aichallenge.agents.youtubetranscript.application.LearningAnalysisProperties;
import com.aichallenge.agents.youtubetranscript.domain.ImportantSegment;
import com.aichallenge.agents.youtubetranscript.domain.ProjectApplication;
import com.aichallenge.agents.youtubetranscript.domain.Transcript;
import com.aichallenge.agents.youtubetranscript.domain.TranscriptSegment;
import com.aichallenge.agents.youtubetranscript.domain.VideoLearningAnalysis;
import com.aichallenge.agents.youtubetranscript.domain.port.VideoLearningAnalysisGenerator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.core.SdkBytes;
import software.amazon.awssdk.services.bedrockruntime.BedrockRuntimeClient;
import software.amazon.awssdk.services.bedrockruntime.model.InvokeModelRequest;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Component
@Qualifier("bedrockLearningAnalysisGenerator")
@ConditionalOnProperty(prefix = "app.learning-analysis.bedrock", name = "enabled", havingValue = "true")
public class BedrockVideoLearningAnalysisGenerator implements VideoLearningAnalysisGenerator {

    private static final String PROVIDER = "BEDROCK";
    private static final String PROMPT_VERSION = "bedrock-openai-learning-v1";

    private final BedrockRuntimeClient bedrockRuntimeClient;
    private final ObjectMapper objectMapper;
    private final LearningAnalysisProperties properties;

    public BedrockVideoLearningAnalysisGenerator(
            BedrockRuntimeClient bedrockRuntimeClient,
            ObjectMapper objectMapper,
            LearningAnalysisProperties properties
    ) {
        this.bedrockRuntimeClient = bedrockRuntimeClient;
        this.objectMapper = objectMapper;
        this.properties = properties;
    }

    @Override
    public VideoLearningAnalysis analyze(Transcript transcript) {
        BedrockAnalysisPayload payload = invokeModel(transcript);

        return new VideoLearningAnalysis(
                null,
                transcript.videoId(),
                transcript.id(),
                transcript.language(),
                "es",
                PROVIDER,
                properties.getBedrock().getModelId(),
                payload.summary(),
                limit(payload.keyIdeas(), 10),
                limit(payload.projectApplications(), 8),
                limit(payload.toImportantSegments(), 10),
                limit(payload.personalLearningNotes(), 10),
                limit(payload.suggestedActions(), 10),
                PROMPT_VERSION,
                LocalDateTime.now()
        );
    }

    private BedrockAnalysisPayload invokeModel(Transcript transcript) {
        String requestJson = toJson(chatCompletionsRequest(transcript));
        var request = InvokeModelRequest.builder()
                .modelId(properties.getBedrock().getModelId())
                .contentType("application/json")
                .accept("application/json")
                .body(SdkBytes.fromUtf8String(requestJson))
                .build();

        String responseJson = bedrockRuntimeClient.invokeModel(request).body().asUtf8String();
        String assistantText = extractAssistantText(responseJson);
        return parseAnalysisPayload(assistantText);
    }

    private Map<String, Object> chatCompletionsRequest(Transcript transcript) {
        String prompt = """
                Analiza este transcript de YouTube para aprendizaje personal.

                Reglas:
                - Usa el transcript original como fuente de verdad.
                - La salida debe estar en español rioplatense/neutro claro.
                - No inventes datos no presentes en el transcript.
                - Extrae conclusiones reales: historia personal, decisiones de negocio, startup, IA/agentes, clientes, producto, fundraising, liderazgo, aprendizajes y aplicaciones a proyectos.
                - Usa timestamps cuando encuentres momentos importantes.
                - Responde SOLO JSON valido, sin markdown.

                JSON esperado:
                {
                  "summary": "resumen profundo en 1 o 2 parrafos",
                  "keyIdeas": ["idea concreta y accionable"],
                  "projectApplications": [{"idea":"aplicacion para mis proyectos","whyItMatters":"por que importa"}],
                  "importantSegments": [{"start":123.0,"duration":90.0,"reason":"por que vale revisar este momento"}],
                  "personalLearningNotes": ["nota personal de aprendizaje"],
                  "suggestedActions": ["accion concreta siguiente"]
                }

                Metadata:
                videoId: %s
                idioma original: %s
                segmentos: %d

                Transcript con timestamps:
                %s
                """.formatted(
                transcript.videoId(),
                transcript.language(),
                transcript.segments().size(),
                transcriptForPrompt(transcript)
        );

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("model", properties.getBedrock().getModelId());
        body.put("messages", List.of(
                Map.of(
                        "role", "system",
                        "content", "Sos un analista senior de aprendizaje, startups e IA. Convertis videos largos en conocimiento aplicable a proyectos personales. Responde solo JSON valido y nunca expongas razonamiento."
                ),
                Map.of("role", "user", "content", prompt)
        ));
        body.put("temperature", properties.getBedrock().getTemperature());
        body.put("top_p", 0.9);
        body.put("max_completion_tokens", properties.getBedrock().getMaxTokens());
        body.put("stream", false);
        return body;
    }

    private String transcriptForPrompt(Transcript transcript) {
        StringBuilder builder = new StringBuilder();
        for (TranscriptSegment segment : transcript.segments()) {
            if (segment.text() == null || segment.text().isBlank()) {
                continue;
            }
            String line = "[%s] %s%n".formatted(formatTime(segment.start()), segment.text().replaceAll("\\s+", " ").trim());
            if (builder.length() + line.length() > properties.getBedrock().getMaxTranscriptChars()) {
                builder.append("%n[TRANSCRIPT_TRUNCATED] Se omitio el resto por limite configurado.".formatted());
                break;
            }
            builder.append(line);
        }
        return builder.toString();
    }

    private String extractAssistantText(String responseJson) {
        try {
            JsonNode root = objectMapper.readTree(responseJson);
            JsonNode choicesContent = root.at("/choices/0/message/content");
            if (choicesContent.isTextual()) {
                return choicesContent.asText();
            }
            if (choicesContent.isArray()) {
                String text = extractTextFromContentArray(choicesContent);
                if (!text.isBlank()) {
                    return text;
                }
            }
            JsonNode outputText = root.at("/output/message/content/0/text");
            if (outputText.isTextual()) {
                return outputText.asText();
            }
            JsonNode contentText = root.at("/content/0/text");
            if (contentText.isTextual()) {
                return contentText.asText();
            }
            throw new IllegalStateException("Bedrock response did not contain assistant text.");
        } catch (JsonProcessingException ex) {
            throw new IllegalStateException("Could not parse Bedrock OpenAI-compatible response.", ex);
        }
    }

    private String extractTextFromContentArray(JsonNode content) {
        StringBuilder builder = new StringBuilder();
        for (JsonNode item : content) {
            if (item.isTextual()) {
                builder.append(item.asText()).append("\n");
            } else if (item.has("text")) {
                builder.append(item.path("text").asText()).append("\n");
            }
        }
        return builder.toString().trim();
    }

    private BedrockAnalysisPayload parseAnalysisPayload(String assistantText) {
        String json = extractJsonObject(assistantText);
        try {
            BedrockAnalysisPayload payload = objectMapper.readValue(json, BedrockAnalysisPayload.class);
            payload.validate();
            return payload;
        } catch (JsonProcessingException ex) {
            throw new IllegalStateException("Bedrock analysis response was not valid JSON.", ex);
        }
    }

    private String extractJsonObject(String text) {
        int start = text.indexOf('{');
        int end = text.lastIndexOf('}');
        if (start < 0 || end <= start) {
            throw new IllegalStateException("Bedrock analysis response did not include a JSON object.");
        }
        return text.substring(start, end + 1);
    }

    private String toJson(Object value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (JsonProcessingException ex) {
            throw new IllegalStateException("Could not serialize Bedrock request.", ex);
        }
    }

    private String formatTime(double seconds) {
        int totalSeconds = (int) Math.floor(seconds);
        int hours = totalSeconds / 3600;
        int minutes = (totalSeconds % 3600) / 60;
        int rest = totalSeconds % 60;
        if (hours > 0) {
            return "%d:%02d:%02d".formatted(hours, minutes, rest);
        }
        return "%d:%02d".formatted(minutes, rest);
    }

    private <T> List<T> limit(List<T> values, int limit) {
        if (values == null) {
            return List.of();
        }
        return values.stream().limit(limit).toList();
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private record BedrockAnalysisPayload(
            String summary,
            List<String> keyIdeas,
            List<ProjectApplication> projectApplications,
            List<BedrockImportantSegment> importantSegments,
            List<String> personalLearningNotes,
            List<String> suggestedActions
    ) {
        void validate() {
            if (summary == null || summary.isBlank()) {
                throw new IllegalStateException("Bedrock analysis response is missing summary.");
            }
            if (keyIdeas == null || keyIdeas.isEmpty()) {
                throw new IllegalStateException("Bedrock analysis response is missing keyIdeas.");
            }
        }

        List<ImportantSegment> toImportantSegments() {
            if (importantSegments == null) {
                return List.of();
            }
            List<ImportantSegment> segments = new ArrayList<>();
            for (BedrockImportantSegment segment : importantSegments) {
                segments.add(new ImportantSegment(segment.start(), segment.duration(), segment.reason()));
            }
            return segments;
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private record BedrockImportantSegment(double start, double duration, String reason) {
    }
}
