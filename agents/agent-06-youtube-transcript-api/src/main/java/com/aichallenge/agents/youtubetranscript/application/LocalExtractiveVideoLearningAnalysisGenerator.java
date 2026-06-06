package com.aichallenge.agents.youtubetranscript.application;

import com.aichallenge.agents.youtubetranscript.domain.ImportantSegment;
import com.aichallenge.agents.youtubetranscript.domain.ProjectApplication;
import com.aichallenge.agents.youtubetranscript.domain.Transcript;
import com.aichallenge.agents.youtubetranscript.domain.TranscriptSegment;
import com.aichallenge.agents.youtubetranscript.domain.VideoLearningAnalysis;
import com.aichallenge.agents.youtubetranscript.domain.port.VideoLearningAnalysisGenerator;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.text.BreakIterator;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

@Component
@Qualifier("localLearningAnalysisGenerator")
public class LocalExtractiveVideoLearningAnalysisGenerator implements VideoLearningAnalysisGenerator {

    private static final String PROVIDER = "LOCAL";
    private static final String MODEL = "extractive-v3";
    private static final String PROMPT_VERSION = "local-extractive-v3";
    private static final int MAX_KEY_IDEAS = 7;
    private static final int MAX_IMPORTANT_SEGMENTS = 7;
    private static final double WINDOW_SECONDS = 90.0;

    @Override
    public VideoLearningAnalysis analyze(Transcript transcript) {
        List<TranscriptWindow> representativeWindows = selectRepresentativeWindows(transcript.segments(), MAX_KEY_IDEAS);
        List<String> keyIdeas = representativeWindows.stream()
                .map(window -> "Idea detectada cerca de " + formatTime(window.start()) + ": " + cleanText(window.text()))
                .toList();

        List<ImportantSegment> importantSegments = selectImportantSegments(transcript.segments());

        return new VideoLearningAnalysis(
                null,
                transcript.videoId(),
                transcript.id(),
                transcript.language(),
                "es",
                PROVIDER,
                MODEL,
                buildSummary(transcript, representativeWindows),
                keyIdeas,
                projectApplications(transcript),
                importantSegments,
                personalLearningNotes(transcript, keyIdeas),
                suggestedActions(transcript),
                PROMPT_VERSION,
                LocalDateTime.now()
        );
    }

    private String buildSummary(Transcript transcript, List<TranscriptWindow> representativeWindows) {
        List<String> topics = detectTopics(transcript.fullText());
        String topicText = topics.isEmpty()
                ? "aprendizajes generales extraidos del transcript"
                : String.join(", ", topics);
        String evidence = representativeWindows.isEmpty()
                ? "No se detectaron segmentos suficientes para una muestra extractiva."
                : representativeWindows.stream()
                .limit(3)
                .map(window -> formatTime(window.start()) + " " + cleanText(window.text()))
                .toList()
                .toString();

        return "Análisis local extractivo en castellano. Se revisaron %d segmentos del transcript original en idioma '%s' y se seleccionaron ventanas de contexto distribuidas a lo largo del video. Temas detectados: %s. Evidencia inicial: %s"
                .formatted(transcript.segments().size(), transcript.language(), topicText, evidence);
    }

    private List<ProjectApplication> projectApplications(Transcript transcript) {
        String text = transcript.fullText().toLowerCase(Locale.ROOT);
        List<ProjectApplication> applications = new ArrayList<>();

        if (text.contains("agent") || text.contains("agents")) {
            applications.add(new ProjectApplication(
                    "Revisar si las ideas del video pueden convertirse en una mejora para tus agentes del challenge.",
                    "El transcript menciona agentes, por lo que puede inspirar patrones de coordinación, handoff o automatización para tus proyectos."
            ));
        }

        if (text.contains("skill") || text.contains("skills")) {
            applications.add(new ProjectApplication(
                    "Evaluar si el contenido puede transformarse en una skill reutilizable.",
                    "Las skills permiten convertir prácticas repetidas en capacidades versionadas y fáciles de invocar."
            ));
        }

        if (applications.isEmpty()) {
            applications.add(new ProjectApplication(
                    "Crear una nota de aprendizaje vinculada a un proyecto actual.",
                    "El objetivo del sistema es convertir videos interesantes en acciones pequeñas y aprovechables."
            ));
        }

        return applications;
    }

    private List<ImportantSegment> selectImportantSegments(List<TranscriptSegment> segments) {
        return selectRepresentativeWindows(segments, MAX_IMPORTANT_SEGMENTS).stream()
                .map(window -> new ImportantSegment(
                        window.start(),
                        window.duration(),
                        "Ventana relevante detectada por densidad, palabras clave y separacion temporal."
                ))
                .toList();
    }

    private List<String> personalLearningNotes(Transcript transcript, List<String> keyIdeas) {
        List<String> notes = new ArrayList<>();
        notes.add("Revisar las ideas clave y decidir si el video merece verse completo.");
        notes.add("Usar el transcript original como fuente para un análisis LLM posterior en castellano.");
        if (!keyIdeas.isEmpty()) {
            notes.add("Convertir la primera idea clave en una nota o tarea concreta para un proyecto personal.");
        }
        if (!"es".equalsIgnoreCase(transcript.language())) {
            notes.add("El contexto original está en '%s'; mantener ese idioma como fuente y pedir salida final en castellano.".formatted(transcript.language()));
        }
        return notes;
    }

    private List<String> suggestedActions(Transcript transcript) {
        return List.of(
                "Generar un análisis LLM real usando este transcript persistido.",
                "Revisar las ventanas de timestamps distribuidas para decidir qué partes del video ver.",
                "Relacionar las ideas útiles con issues, specs o playbooks de tus proyectos."
        );
    }

    private List<TranscriptWindow> selectRepresentativeWindows(List<TranscriptSegment> segments, int maxWindows) {
        List<TranscriptWindow> windows = buildWindows(segments);
        List<ScoredWindow> scoredWindows = windows.stream()
                .filter(window -> window.text().length() >= 80)
                .map(window -> new ScoredWindow(window, score(window.text())))
                .sorted(Comparator.comparingDouble(ScoredWindow::score).reversed())
                .toList();

        if (scoredWindows.isEmpty()) {
            return List.of();
        }

        double lastStart = segments.stream()
                .mapToDouble(TranscriptSegment::start)
                .max()
                .orElse(0.0);
        double minimumGap = Math.max(WINDOW_SECONDS, lastStart / Math.max(8.0, maxWindows * 1.7));
        List<TranscriptWindow> selected = new ArrayList<>();

        for (ScoredWindow scoredWindow : scoredWindows) {
            TranscriptWindow candidate = scoredWindow.window();
            boolean tooClose = selected.stream()
                    .anyMatch(existing -> Math.abs(existing.start() - candidate.start()) < minimumGap);
            if (!tooClose) {
                selected.add(candidate);
            }
            if (selected.size() == maxWindows) {
                break;
            }
        }

        if (selected.size() < maxWindows) {
            for (ScoredWindow scoredWindow : scoredWindows) {
                if (!selected.contains(scoredWindow.window())) {
                    selected.add(scoredWindow.window());
                }
                if (selected.size() == maxWindows) {
                    break;
                }
            }
        }

        return selected.stream()
                .sorted(Comparator.comparingDouble(TranscriptWindow::start))
                .toList();
    }

    private List<TranscriptWindow> buildWindows(List<TranscriptSegment> segments) {
        List<TranscriptSegment> sortedSegments = segments.stream()
                .filter(segment -> segment.text() != null && !segment.text().isBlank())
                .sorted(Comparator.comparingDouble(TranscriptSegment::start))
                .toList();
        if (sortedSegments.isEmpty()) {
            return List.of();
        }

        List<TranscriptWindow> windows = new ArrayList<>();
        double currentStart = sortedSegments.get(0).start();
        double currentEnd = currentStart + WINDOW_SECONDS;
        List<String> currentTexts = new ArrayList<>();

        for (TranscriptSegment segment : sortedSegments) {
            if (segment.start() >= currentEnd && !currentTexts.isEmpty()) {
                windows.add(new TranscriptWindow(currentStart, currentEnd - currentStart, joinTexts(currentTexts)));
                currentTexts.clear();
                currentStart = segment.start();
                currentEnd = currentStart + WINDOW_SECONDS;
            }
            currentTexts.add(segment.text());
        }

        if (!currentTexts.isEmpty()) {
            double duration = Math.max(WINDOW_SECONDS, sortedSegments.get(sortedSegments.size() - 1).start() - currentStart);
            windows.add(new TranscriptWindow(currentStart, duration, joinTexts(currentTexts)));
        }

        return windows;
    }

    private String joinTexts(List<String> texts) {
        return texts.stream()
                .map(this::cleanCaptionFragment)
                .filter(fragment -> !fragment.isBlank())
                .reduce("", (left, right) -> {
                    if (left.isBlank()) {
                        return right;
                    }
                    return left + " " + right;
                })
                .replaceAll("\\s+", " ")
                .trim();
    }

    private double score(String sourceText) {
        String text = sourceText.toLowerCase(Locale.ROOT);
        double score = Math.min(text.length(), 180) / 18.0;
        score += keywordScore(text);
        if (text.contains("?")) {
            score += 1.0;
        }
        if (text.contains("aprend") || text.contains("learn")) {
            score += 2.0;
        }
        return score;
    }

    private double keywordScore(String text) {
        double score = 0.0;
        String[] keywords = {
                "agent", "agents", "agente", "agentes", "ai", "ia", "inteligencia artificial",
                "startup", "empresa", "negocio", "business", "founder", "fundador",
                "cliente", "clientes", "customer", "product", "producto", "mercado", "market",
                "inversion", "inversionista", "fundraising", "millones", "revenue", "ingresos",
                "equipo", "team", "liderazgo", "decision", "decidir", "problema", "solucion",
                "fracaso", "error", "riesgo", "crecer", "escala", "scale", "leccion"
        };
        for (String keyword : keywords) {
            if (text.contains(keyword)) {
                score += 1.25;
            }
        }
        return score;
    }

    private List<String> detectTopics(String fullText) {
        String text = fullText == null ? "" : fullText.toLowerCase(Locale.ROOT);
        List<String> topics = new ArrayList<>();
        if (containsAny(text, "agent", "agents", "agente", "agentes", "ai", "ia", "inteligencia artificial")) {
            topics.add("IA y agentes");
        }
        if (containsAny(text, "startup", "empresa", "negocio", "founder", "fundador")) {
            topics.add("startup y negocio");
        }
        if (containsAny(text, "inversion", "inversionista", "fundraising", "millones", "acciones")) {
            topics.add("inversion y financiamiento");
        }
        if (containsAny(text, "cliente", "clientes", "producto", "product", "mercado", "market")) {
            topics.add("producto, clientes y mercado");
        }
        if (containsAny(text, "familia", "papá", "mama", "mamá", "hermana", "personal")) {
            topics.add("historia personal");
        }
        if (containsAny(text, "equipo", "team", "liderazgo", "contratar", "hire")) {
            topics.add("equipo y liderazgo");
        }
        return topics;
    }

    private boolean containsAny(String text, String... needles) {
        for (String needle : needles) {
            if (text.contains(needle)) {
                return true;
            }
        }
        return false;
    }

    private String cleanText(String text) {
        if (text == null) {
            return "";
        }
        String normalized = text.replaceAll("\\s+", " ").trim();
        if (normalized.length() <= 360) {
            return normalized;
        }
        BreakIterator iterator = BreakIterator.getSentenceInstance(Locale.ROOT);
        iterator.setText(normalized);
        int firstSentenceEnd = iterator.next();
        if (firstSentenceEnd != BreakIterator.DONE && firstSentenceEnd >= 120 && firstSentenceEnd <= 360) {
            return normalized.substring(0, firstSentenceEnd).trim();
        }
        return normalized.substring(0, 357).trim() + "...";
    }

    private String cleanCaptionFragment(String text) {
        if (text == null) {
            return "";
        }
        return text.replaceAll("\\s+", " ").trim();
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

    private record TranscriptWindow(double start, double duration, String text) {
    }

    private record ScoredWindow(TranscriptWindow window, double score) {
    }
}
