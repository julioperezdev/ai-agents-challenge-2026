package com.aichallenge.agents.youtubetranscript.application;

import com.aichallenge.agents.youtubetranscript.domain.Transcript;
import org.springframework.stereotype.Component;

import java.text.BreakIterator;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

@Component
public class SpanishTranscriptInsightGenerator {

    private static final int MAX_CONTEXT_PREVIEW_WORDS = 20;

    public TranscriptInsightResponse generate(Transcript transcript) {
        String preview = firstWords(transcript.fullText(), MAX_CONTEXT_PREVIEW_WORDS);
        List<String> keyPoints = firstSentences(transcript.fullText(), 3).stream()
                .map(sentence -> "Punto extraido del transcript original: " + sentence)
                .toList();

        String explanation = """
                El contenido principal del video se preparo usando la transcripcion original como contexto. Cuando conectes un adapter LLM, debe interpretar ese contexto en su idioma original y redactar la explicacion final en espanol.
                """.trim();

        return new TranscriptInsightResponse(
                transcript.language(),
                "es",
                transcript.languageDetectionMethod(),
                transcript.languageFallbackUsed(),
                preview,
                buildLlmInstructions(transcript),
                explanation,
                keyPoints
        );
    }

    private String buildLlmInstructions(Transcript transcript) {
        return """
                Usa la transcripcion completa del campo fullText como contexto principal. El idioma original detectado es %s. No traduzcas el contexto antes de razonarlo: interpretalo en su idioma original. Devuelve solamente una explicacion en espanol con lo mas importante del contenido del video y una lista breve de puntos clave.
                """.formatted(transcript.language()).trim();
    }

    private String firstWords(String text, int maxWords) {
        if (text == null || text.isBlank()) {
            return "";
        }

        String[] words = text.trim().split("\\s+");
        return String.join(" ", Arrays.stream(words).limit(maxWords).toList());
    }

    private List<String> firstSentences(String text, int maxSentences) {
        if (text == null || text.isBlank()) {
            return List.of();
        }

        BreakIterator iterator = BreakIterator.getSentenceInstance(Locale.ROOT);
        iterator.setText(text);

        java.util.ArrayList<String> sentences = new java.util.ArrayList<>();
        int start = iterator.first();
        for (int end = iterator.next(); end != BreakIterator.DONE && sentences.size() < maxSentences; start = end, end = iterator.next()) {
            String sentence = text.substring(start, end).trim();
            if (!sentence.isBlank()) {
                sentences.add(sentence);
            }
        }

        if (sentences.isEmpty()) {
            sentences.add(firstWords(text, 28));
        }

        return sentences;
    }
}
