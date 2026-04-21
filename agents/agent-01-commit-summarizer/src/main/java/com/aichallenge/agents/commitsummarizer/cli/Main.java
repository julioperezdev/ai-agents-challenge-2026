package com.aichallenge.agents.commitsummarizer.cli;

import com.aichallenge.agents.commitsummarizer.application.GenerateSummaryRequest;
import com.aichallenge.agents.commitsummarizer.application.GenerateSummaryUseCase;
import com.aichallenge.agents.commitsummarizer.application.MultiRepoSummaryRequest;
import com.aichallenge.agents.commitsummarizer.application.MultiRepoSummaryUseCase;
import com.aichallenge.agents.commitsummarizer.domain.model.DistributionMode;
import com.aichallenge.agents.commitsummarizer.domain.model.MultiRepoSummaryResult;
import com.aichallenge.agents.commitsummarizer.domain.model.ProjectWhitelistEntry;
import com.aichallenge.agents.commitsummarizer.domain.model.RepositoryEntry;
import com.aichallenge.agents.commitsummarizer.domain.model.SummaryResult;
import com.aichallenge.agents.commitsummarizer.domain.service.CommitSummaryService;
import com.aichallenge.agents.commitsummarizer.infrastructure.GitCommitReader;
import com.aichallenge.agents.commitsummarizer.infrastructure.MeetingCsvRepository;
import com.aichallenge.agents.commitsummarizer.infrastructure.ProjectWhitelistRepository;
import com.aichallenge.agents.commitsummarizer.infrastructure.RepositoryCsvRepository;
import com.aichallenge.agents.commitsummarizer.infrastructure.bedrock.BedrockAiDailyWorkSummarizer;
import com.aichallenge.agents.commitsummarizer.infrastructure.bedrock.BedrockConfig;
import com.aichallenge.agents.commitsummarizer.presentation.MultiRepoSummaryRenderer;
import com.aichallenge.agents.commitsummarizer.presentation.SummaryRenderer;

import java.io.IOException;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Main {
    private static final Path DEFAULT_WHITELIST_FILE = Path.of("config", "project-whitelist.csv");

    public static void main(String[] args) {
        try {
            // If no args → interactive mode
            if (args.length == 0) {
                new InteractiveMode().run();
                return;
            }

            if (contains(args, "--help")) {
                printHelp();
                return;
            }

            CliArguments cliArguments = parseArguments(args);
            ProjectWhitelistRepository whitelistRepository = new ProjectWhitelistRepository();

            if (cliArguments.listProjects()) {
                printWhitelist(whitelistRepository.load(cliArguments.whitelistFile()));
                return;
            }

            // New multi-repo path: if --project is set, use legacy single-repo path
            // If --repo-path is set, use legacy single-repo path
            // Otherwise, if only --from/--to/--mode are given, use multi-repo with repositories.csv
            if (cliArguments.projectAlias() == null && cliArguments.repoPath() == null) {
                runMultiRepo(cliArguments);
            } else {
                runSingleRepo(cliArguments, whitelistRepository);
            }

        } catch (IllegalArgumentException exception) {
            System.err.println("Error: " + exception.getMessage());
            System.exit(1);
        } catch (IOException | InterruptedException exception) {
            System.err.println("Error: " + exception.getMessage());
            System.exit(1);
        }
    }

    // -----------------------------------------------------------------------
    // Multi-repo execution using repositories.csv
    // -----------------------------------------------------------------------
    private static void runMultiRepo(CliArguments cliArguments) throws IOException, InterruptedException {
        RepositoryCsvRepository repoCsvRepository = new RepositoryCsvRepository();
        List<RepositoryEntry> repos = repoCsvRepository.load();

        // Filter to only valid git repos (skip ai-team if it's not a git repo)
        GitCommitReader gitCommitReader = new GitCommitReader();
        List<RepositoryEntry> validRepos = new java.util.ArrayList<>();
        for (RepositoryEntry repo : repos) {
            try {
                gitCommitReader.ensureGitRepository(repo.path());
                validRepos.add(repo);
            } catch (IllegalArgumentException ignored) {
                // Not a valid git repo, skip
            }
        }

        try (BedrockAiDailyWorkSummarizer aiSummarizer = new BedrockAiDailyWorkSummarizer(BedrockConfig.fromEnvironment())) {
            MultiRepoSummaryUseCase useCase = new MultiRepoSummaryUseCase(
                gitCommitReader,
                new CommitSummaryService(),
                new MeetingCsvRepository(),
                aiSummarizer
            );

            MultiRepoSummaryResult result = useCase.execute(new MultiRepoSummaryRequest(
                cliArguments.dateFrom(),
                cliArguments.dateTo(),
                validRepos,
                false,  // CLI mode: no meetings by default
                cliArguments.distributionMode()
            ));

            MultiRepoSummaryRenderer renderer = new MultiRepoSummaryRenderer();
            System.out.println(renderer.render(result));
        }
    }

    // -----------------------------------------------------------------------
    // Single-repo execution (legacy: --project or --repo-path)
    // -----------------------------------------------------------------------
    private static void runSingleRepo(CliArguments cliArguments, ProjectWhitelistRepository whitelistRepository)
        throws IOException, InterruptedException {
        CommitSummaryService commitSummaryService = new CommitSummaryService();
        SummaryRenderer renderer = new SummaryRenderer(commitSummaryService);

        Path repoPath = resolveRepoPath(cliArguments, whitelistRepository);

        try (BedrockAiDailyWorkSummarizer aiSummarizer = new BedrockAiDailyWorkSummarizer(BedrockConfig.fromEnvironment())) {
            GenerateSummaryUseCase useCase = new GenerateSummaryUseCase(
                new GitCommitReader(),
                commitSummaryService,
                aiSummarizer
            );

            SummaryResult result = useCase.execute(new GenerateSummaryRequest(
                cliArguments.author(),
                cliArguments.dateFrom(),
                cliArguments.dateTo(),
                repoPath,
                cliArguments.outputFormat()
            ));
            System.out.println(renderer.render(result, cliArguments.outputFormat()));
        }
    }

    // -----------------------------------------------------------------------
    // Argument parsing
    // -----------------------------------------------------------------------
    private static CliArguments parseArguments(String[] args) {
        Map<String, String> options = new HashMap<>();
        boolean listProjects = false;

        for (int index = 0; index < args.length; index++) {
            String current = args[index];
            if (!current.startsWith("--")) {
                throw new IllegalArgumentException("Argumento no reconocido: " + current);
            }
            if ("--list-projects".equals(current)) {
                listProjects = true;
                continue;
            }
            if (index + 1 >= args.length || args[index + 1].startsWith("--")) {
                throw new IllegalArgumentException("Falta valor para " + current);
            }
            options.put(current, args[++index]);
        }

        LocalDate dateFrom = listProjects ? null : parseDate(requiredOption(options, "--from"));
        // --to is optional, defaults to today
        LocalDate dateTo;
        if (listProjects) {
            dateTo = null;
        } else if (options.containsKey("--to")) {
            dateTo = parseDate(options.get("--to"));
        } else {
            dateTo = LocalDate.now();
        }

        String outputFormat = options.getOrDefault("--output-format", "time-tracker");
        if (!outputFormat.equals("time-tracker") && !outputFormat.equals("markdown") && !outputFormat.equals("raw")) {
            throw new IllegalArgumentException("El formato debe ser time-tracker, markdown o raw.");
        }

        DistributionMode distributionMode = DistributionMode.from(options.getOrDefault("--mode", "strict"));

        return new CliArguments(
            options.get("--author"),
            dateFrom,
            dateTo,
            options.containsKey("--repo-path") ? Path.of(options.get("--repo-path")) : null,
            options.get("--project"),
            outputFormat,
            listProjects,
            Path.of(options.getOrDefault("--whitelist-file", DEFAULT_WHITELIST_FILE.toString())),
            distributionMode
        );
    }

    private static Path resolveRepoPath(CliArguments cliArguments, ProjectWhitelistRepository whitelistRepository) throws IOException {
        if (cliArguments.projectAlias() != null && !cliArguments.projectAlias().isBlank()) {
            ProjectWhitelistEntry entry = whitelistRepository.findEnabledByAlias(
                cliArguments.whitelistFile(),
                cliArguments.projectAlias()
            );
            return entry.repoPath();
        }

        if (cliArguments.repoPath() != null) {
            return cliArguments.repoPath();
        }

        return Path.of(".");
    }

    private static String requiredOption(Map<String, String> options, String key) {
        String value = options.get(key);
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("El parametro " + key + " es obligatorio.");
        }
        return value;
    }

    private static LocalDate parseDate(String raw) {
        try {
            return LocalDate.parse(raw);
        } catch (DateTimeParseException exception) {
            throw new IllegalArgumentException("Fecha inválida '" + raw + "'. Usa el formato YYYY-MM-DD.");
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
              java -jar target/agent-01-commit-summarizer-0.1.0.jar --from 2026-04-13 [opciones]

            Opciones:
              --author "Julio Perez"      Nombre o email del autor. Si se omite, intenta autodetectarlo desde Git.
              --from 2026-04-13           Fecha inicial en formato YYYY-MM-DD.
              --to 2026-04-18             Fecha final en formato YYYY-MM-DD. Por defecto: hoy.
              --mode strict|distributed   Modo de distribución de commits. Por defecto: strict.
              --repo-path /ruta/al/repo   Ruta al repositorio Git local. Por defecto usa el directorio actual.
              --project alias             Alias de un proyecto guardado en la whitelist.
              --output-format formato     time-tracker, markdown o raw. Por defecto time-tracker.
              --whitelist-file ruta       Ruta al archivo de whitelist. Por defecto config/project-whitelist.csv.
              --list-projects             Lista los proyectos disponibles en la whitelist.
              --help                      Muestra esta ayuda.

            Modo interactivo (sin argumentos):
              Ejecutar sin argumentos lanza el modo interactivo con selección de repos y reuniones.
            """);
    }

    private static void printWhitelist(Iterable<ProjectWhitelistEntry> entries) {
        System.out.println("Proyectos en whitelist:");
        for (ProjectWhitelistEntry entry : entries) {
            String status = entry.enabled() ? "activo" : "inactivo";
            String notes = entry.notes().isBlank() ? "" : " | " + entry.notes();
            System.out.println("- " + entry.alias() + " -> " + entry.repoPath() + " [" + status + "]" + notes);
        }
    }

    private record CliArguments(
        String author,
        LocalDate dateFrom,
        LocalDate dateTo,
        Path repoPath,
        String projectAlias,
        String outputFormat,
        boolean listProjects,
        Path whitelistFile,
        DistributionMode distributionMode
    ) {
    }
}
