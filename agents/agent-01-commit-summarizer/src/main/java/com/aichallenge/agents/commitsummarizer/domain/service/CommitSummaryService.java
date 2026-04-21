package com.aichallenge.agents.commitsummarizer.domain.service;

import com.aichallenge.agents.commitsummarizer.domain.model.Commit;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.AbstractMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.regex.Pattern;

public class CommitSummaryService {
    private static final Pattern PREFIX_PATTERN = Pattern.compile(
        "^\\s*(?:\\[[^\\]]+\\]\\s*)?(feat|fix|refactor|docs|test|perf|chore|style|ci|build|revert)(?:\\([^)]+\\))?:\\s*",
        Pattern.CASE_INSENSITIVE
    );
    private static final Pattern TICKET_PATTERN = Pattern.compile("\\b[A-Z]{2,}-\\d+\\b|\\B#\\d+\\b");
    private static final Pattern WIP_PATTERN = Pattern.compile("^\\s*(wip|tmp|misc|changes?)\\s*$", Pattern.CASE_INSENSITIVE);

    private static final List<Map.Entry<String, String>> PHRASE_REPLACEMENTS = List.of(
        new AbstractMap.SimpleEntry<>("cesco payload validation", "la validación del payload de Cesco"),
        new AbstractMap.SimpleEntry<>("office sync logic", "la lógica de sincronización de oficinas"),
        new AbstractMap.SimpleEntry<>("office sync", "la sincronización de oficinas"),
        new AbstractMap.SimpleEntry<>("payload validation", "la validación del payload"),
        new AbstractMap.SimpleEntry<>("save flow checks", "el flujo de guardado y sus validaciones"),
        new AbstractMap.SimpleEntry<>("save flow", "el flujo de guardado"),
        new AbstractMap.SimpleEntry<>("related data", "los datos relacionados"),
        new AbstractMap.SimpleEntry<>("main flow", "el flujo principal"),
        new AbstractMap.SimpleEntry<>("user flow", "el flujo de usuario")
    );

    private static final Map<String, String> WORD_REPLACEMENTS = Map.ofEntries(
        Map.entry("api", "API"),
        Map.entry("auth", "autenticación"),
        Map.entry("bug", "errores"),
        Map.entry("bugs", "errores"),
        Map.entry("cache", "cache"),
        Map.entry("cesco", "Cesco"),
        Map.entry("check", "validación"),
        Map.entry("checks", "validaciones"),
        Map.entry("config", "configuración"),
        Map.entry("data", "datos"),
        Map.entry("docs", "documentación"),
        Map.entry("document", "documentación"),
        Map.entry("flow", "flujo"),
        Map.entry("handling", "manejo"),
        Map.entry("logic", "lógica"),
        Map.entry("module", "módulo"),
        Map.entry("modules", "módulos"),
        Map.entry("office", "oficinas"),
        Map.entry("offices", "oficinas"),
        Map.entry("payload", "payload"),
        Map.entry("persistence", "persistencia"),
        Map.entry("save", "guardado"),
        Map.entry("saving", "guardado"),
        Map.entry("stability", "estabilidad"),
        Map.entry("structure", "estructura"),
        Map.entry("sync", "sincronización"),
        Map.entry("test", "pruebas"),
        Map.entry("tests", "pruebas"),
        Map.entry("ui", "interfaz"),
        Map.entry("update", "actualización"),
        Map.entry("updates", "actualizaciones"),
        Map.entry("validation", "validación"),
        Map.entry("validations", "validaciones")
    );

    private static final List<String> LEADING_ACTIONS = List.of(
        "add", "adjust", "build", "cleanup", "correct", "create", "fix", "implement",
        "improve", "optimize", "refactor", "remove", "resolve", "restructure", "update"
    );

    private static final Map<String, List<String>> TYPE_PATTERNS = Map.of(
        "fix", List.of("fix", "bugfix", "hotfix", "bug", "resolve", "correct"),
        "feat", List.of("feat", "feature", "add", "implement", "create", "build"),
        "refactor", List.of("refactor", "cleanup", "clean", "rework", "restructure"),
        "docs", List.of("docs", "doc", "document"),
        "test", List.of("test", "tests", "qa", "spec"),
        "perf", List.of("perf", "optimize", "optimization", "speed"),
        "chore", List.of("chore", "bump", "deps", "dependency", "maintenance")
    );

    public Map<LocalDate, List<Commit>> groupByDay(List<Commit> commits) {
        Map<LocalDate, List<Commit>> grouped = new TreeMap<>();
        for (Commit commit : commits) {
            grouped.computeIfAbsent(commit.localDay(), ignored -> new ArrayList<>()).add(commit);
        }
        return new LinkedHashMap<>(grouped);
    }

    public List<String> summarizeDay(List<Commit> commits) {
        Set<String> uniqueLines = new LinkedHashSet<>();
        for (Commit commit : commits) {
            uniqueLines.add(summarizeCommitMessage(commit.message()));
        }
        return new ArrayList<>(uniqueLines);
    }

    public String cleanCommitMessage(String message) {
        String cleaned = PREFIX_PATTERN.matcher(message).replaceFirst("");
        cleaned = TICKET_PATTERN.matcher(cleaned).replaceAll("");
        cleaned = cleaned.replaceAll("\\s+", " ").trim();
        cleaned = cleaned.replaceAll("^[\\-_:;,.\\s]+|[\\-_:;,.\\s]+$", "");
        return cleaned.isBlank() ? "actualizaciones internas" : cleaned;
    }

    public String summarizeCommitMessage(String message) {
        String cleaned = cleanCommitMessage(message);
        if (WIP_PATTERN.matcher(cleaned).matches()) {
            return "Avances internos y ajustes de soporte del flujo de trabajo.";
        }

        String commitType = detectCommitType(message, cleaned);
        String topic = humanizeTopic(cleaned);

        return switch (commitType) {
            case "fix" -> "Correcciones en " + topic + ".";
            case "feat" -> "Implementación de mejoras en " + topic + ".";
            case "refactor" -> "Refactor y mejoras en " + topic + ".";
            case "docs" -> "Actualización de documentación sobre " + topic + ".";
            case "test" -> "Cobertura de pruebas para " + topic + ".";
            case "perf" -> "Optimización de " + topic + ".";
            case "chore" -> "Mantenimiento y ajustes en " + topic + ".";
            default -> "Avances en " + topic + ".";
        };
    }

    private String detectCommitType(String originalMessage, String cleanedMessage) {
        var prefixMatcher = PREFIX_PATTERN.matcher(originalMessage);
        if (prefixMatcher.find()) {
            return prefixMatcher.group(1).toLowerCase(Locale.ROOT);
        }

        String lowered = cleanedMessage.toLowerCase(Locale.ROOT);
        String firstWord = lowered.contains(" ") ? lowered.substring(0, lowered.indexOf(' ')) : lowered;

        for (Map.Entry<String, List<String>> entry : TYPE_PATTERNS.entrySet()) {
            if (entry.getValue().contains(firstWord)) {
                return entry.getKey();
            }
            for (String pattern : entry.getValue()) {
                if (lowered.startsWith(pattern + " ")) {
                    return entry.getKey();
                }
            }
        }

        return "default";
    }

    private String humanizeTopic(String message) {
        String lowered = message.toLowerCase(Locale.ROOT);
        for (String action : LEADING_ACTIONS) {
            if (lowered.startsWith(action + " ")) {
                message = message.substring(action.length()).trim();
                lowered = message.toLowerCase(Locale.ROOT);
                break;
            }
        }

        String normalized = lowered
            .replace("_", " ")
            .replace("/", " ")
            .replace("-", " ")
            .replaceAll("[(){}\\[\\]`'\"]", " ")
            .replaceAll("\\s+", " ")
            .trim();

        if (normalized.isBlank()) {
            return "tareas internas del proyecto";
        }

        for (Map.Entry<String, String> replacement : PHRASE_REPLACEMENTS) {
            normalized = normalized.replace(replacement.getKey(), replacement.getValue());
        }

        String[] tokens = normalized.split(" ");
        List<String> translatedTokens = new ArrayList<>();
        for (String token : tokens) {
            translatedTokens.add(WORD_REPLACEMENTS.getOrDefault(token, token));
        }

        String topic = String.join(" ", translatedTokens)
            .replaceAll("\\s+", " ")
            .trim()
            .replace("de el", "del")
            .replace("a el", "al")
            .replace("la validación del payload cesco", "la validación del payload de Cesco");

        if (topic.startsWith("la ") || topic.startsWith("el ") || topic.startsWith("los ") || topic.startsWith("las ")) {
            return topic;
        }

        return topic;
    }
}
