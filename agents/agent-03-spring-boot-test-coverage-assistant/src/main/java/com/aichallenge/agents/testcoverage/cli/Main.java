package com.aichallenge.agents.testcoverage.cli;

import com.aichallenge.agents.testcoverage.application.CoverageAnalysisRequest;
import com.aichallenge.agents.testcoverage.application.CoverageStrategyPlanner;
import com.aichallenge.agents.testcoverage.application.SpringBootTestCoverageAnalyzer;
import com.aichallenge.agents.testcoverage.domain.CoverageAnalysis;
import com.aichallenge.agents.testcoverage.domain.CoverageStrategy;
import com.aichallenge.agents.testcoverage.infrastructure.JavaSourceProjectScanner;
import com.aichallenge.agents.testcoverage.infrastructure.ai.BedrockConfig;
import com.aichallenge.agents.testcoverage.infrastructure.ai.BedrockCoverageStrategyAdvisor;
import com.aichallenge.agents.testcoverage.presentation.CoveragePlanRenderer;
import com.aichallenge.agents.testcoverage.presentation.LocalCoverageStrategyAdvisor;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

public class Main {
    public static void main(String[] args) {
        try {
            if (contains(args, "--help")) {
                printHelp();
                return;
            }

            CliArguments cliArguments = parseArguments(args);
            SpringBootTestCoverageAnalyzer analyzer = new SpringBootTestCoverageAnalyzer(new JavaSourceProjectScanner());
            CoverageAnalysisRequest request = new CoverageAnalysisRequest(
                cliArguments.projectPath(),
                cliArguments.target(),
                cliArguments.maxRecommendationsPerSection()
            );

            if (cliArguments.aiEnabled()) {
                try {
                    try (BedrockCoverageStrategyAdvisor advisor = new BedrockCoverageStrategyAdvisor(BedrockConfig.fromEnvironment())) {
                        CoverageStrategy strategy = new CoverageStrategyPlanner(analyzer, advisor).plan(request);
                        System.out.println(strategy.content());
                    }
                } catch (IllegalStateException exception) {
                    System.err.println("Aviso: Bedrock no devolvio una respuesta final visible. Se imprime el plan local como fallback.");
                    CoverageAnalysis analysis = analyzer.analyze(request);
                    CoverageStrategy strategy = new LocalCoverageStrategyAdvisor(new CoveragePlanRenderer()).advise(analysis);
                    System.out.println(strategy.content());
                }
                return;
            }

            CoverageAnalysis analysis = analyzer.analyze(request);
            CoverageStrategy strategy = new LocalCoverageStrategyAdvisor(new CoveragePlanRenderer()).advise(analysis);
            System.out.println(strategy.content());
        } catch (IllegalArgumentException exception) {
            System.err.println("Error: " + exception.getMessage());
            System.exit(1);
        } catch (IOException exception) {
            System.err.println("Error leyendo el proyecto: " + exception.getMessage());
            System.exit(1);
        }
    }

    private static CliArguments parseArguments(String[] args) {
        Map<String, String> options = new HashMap<>();

        for (int index = 0; index < args.length; index++) {
            String current = args[index];
            if (!current.startsWith("--")) {
                throw new IllegalArgumentException("Argumento no reconocido: " + current);
            }
            if ("--ai".equals(current)) {
                options.put(current, "true");
                continue;
            }
            if (index + 1 >= args.length || args[index + 1].startsWith("--")) {
                throw new IllegalArgumentException("Falta valor para " + current);
            }
            options.put(current, args[++index]);
        }

        Path projectPath = Path.of(options.getOrDefault("--project-path", ".")).toAbsolutePath().normalize();
        if (!Files.isDirectory(projectPath)) {
            throw new IllegalArgumentException("La ruta no existe o no es un directorio: " + projectPath);
        }

        int maxRecommendationsPerSection = parsePositiveInt(options.getOrDefault("--max-items", "8"), "--max-items");

        return new CliArguments(
            projectPath,
            options.get("--target"),
            maxRecommendationsPerSection,
            Boolean.parseBoolean(options.getOrDefault("--ai", "false"))
        );
    }

    private static int parsePositiveInt(String rawValue, String optionName) {
        try {
            int value = Integer.parseInt(rawValue);
            if (value < 1) {
                throw new IllegalArgumentException(optionName + " debe ser mayor a cero.");
            }
            return value;
        } catch (NumberFormatException exception) {
            throw new IllegalArgumentException(optionName + " debe ser un numero entero.");
        }
    }

    private static boolean contains(String[] args, String expected) {
        for (String arg : args) {
            if (expected.equals(arg)) {
                return true;
            }
        }
        return false;
    }

    private static void printHelp() {
        System.out.println("""
            Uso:
              java -jar target/agent-03-spring-boot-test-coverage-assistant-0.1.0.jar [opciones]

            Opciones:
              --project-path /ruta/api   Ruta del proyecto Spring Boot local. Por defecto: directorio actual.
              --target users             Clase, paquete, feature o modulo a analizar. Opcional.
              --max-items 8              Maximo de recomendaciones visibles por seccion. Por defecto: 8.
              --ai                       Usa Bedrock para generar una estrategia menos repetitiva.
              --help                     Muestra esta ayuda.

            Ejemplos:
              ./run.sh --project-path ../mi-api
              ./run.sh --project-path ../mi-api --target users
              ./run.sh --project-path ../mi-api --target users --ai
              ./run.sh --project-path ../mi-api --max-items 12
              ./run.sh --project-path ../mi-api --target UserController
            """);
    }

    private record CliArguments(Path projectPath, String target, int maxRecommendationsPerSection, boolean aiEnabled) {
    }
}
