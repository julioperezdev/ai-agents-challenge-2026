package com.aichallenge.agents.commitsummarizer.cli;

import com.aichallenge.agents.commitsummarizer.application.MultiRepoSummaryRequest;
import com.aichallenge.agents.commitsummarizer.application.MultiRepoSummaryUseCase;
import com.aichallenge.agents.commitsummarizer.domain.model.DistributionMode;
import com.aichallenge.agents.commitsummarizer.domain.model.MultiRepoSummaryResult;
import com.aichallenge.agents.commitsummarizer.domain.model.RepositoryEntry;
import com.aichallenge.agents.commitsummarizer.domain.service.CommitSummaryService;
import com.aichallenge.agents.commitsummarizer.infrastructure.GitCommitReader;
import com.aichallenge.agents.commitsummarizer.infrastructure.MeetingCsvRepository;
import com.aichallenge.agents.commitsummarizer.infrastructure.RepositoryCsvRepository;
import com.aichallenge.agents.commitsummarizer.infrastructure.bedrock.BedrockAiDailyWorkSummarizer;
import com.aichallenge.agents.commitsummarizer.infrastructure.bedrock.BedrockConfig;
import com.aichallenge.agents.commitsummarizer.presentation.MultiRepoSummaryRenderer;
import org.jline.reader.LineReader;
import org.jline.reader.LineReaderBuilder;
import org.jline.terminal.Attributes;
import org.jline.terminal.Terminal;
import org.jline.terminal.TerminalBuilder;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

public class InteractiveMode {

    public void run() throws IOException, InterruptedException {
        RepositoryCsvRepository repoCsvRepository = new RepositoryCsvRepository();
        List<RepositoryEntry> availableRepos = repoCsvRepository.load();

        try (Terminal terminal = TerminalBuilder.builder().system(true).build()) {
            LineReader reader = LineReaderBuilder.builder().terminal(terminal).build();

            // Step 1: Dates
            LocalDate dateFrom = promptDateFrom(reader, terminal);
            LocalDate dateTo = promptDateTo(reader, terminal, dateFrom);

            // Step 2: Repo selection (JLine3 checkbox)
            List<RepositoryEntry> selectedRepos = promptRepos(terminal, availableRepos);

            // Step 3: Include meetings
            boolean includeMeetings = promptMeetings(reader, terminal);

            // Step 4: Distribution mode
            DistributionMode mode = promptDistributionMode(terminal);

            // Step 5: Summary before processing
            printSummary(terminal, dateFrom, dateTo, selectedRepos, includeMeetings, mode);

            // Execute
            try (BedrockAiDailyWorkSummarizer aiSummarizer = new BedrockAiDailyWorkSummarizer(BedrockConfig.fromEnvironment())) {
                MultiRepoSummaryUseCase useCase = new MultiRepoSummaryUseCase(
                    new GitCommitReader(),
                    new CommitSummaryService(),
                    new MeetingCsvRepository(),
                    aiSummarizer
                );

                MultiRepoSummaryResult result = useCase.execute(new MultiRepoSummaryRequest(
                    dateFrom, dateTo, selectedRepos, includeMeetings, mode
                ));

                MultiRepoSummaryRenderer renderer = new MultiRepoSummaryRenderer();
                terminal.writer().println();
                terminal.writer().println(renderer.render(result));
                terminal.writer().flush();
            }
        }
    }

    // -----------------------------------------------------------------------
    // Step 1: Date from
    // -----------------------------------------------------------------------
    private LocalDate promptDateFrom(LineReader reader, Terminal terminal) {
        while (true) {
            String input = reader.readLine("📅 Fecha desde (YYYY-MM-DD): ").trim();
            if (input.isBlank()) {
                terminal.writer().println("  La fecha desde es obligatoria.");
                terminal.writer().flush();
                continue;
            }
            try {
                return LocalDate.parse(input);
            } catch (DateTimeParseException e) {
                terminal.writer().println("  Formato inválido, usá YYYY-MM-DD.");
                terminal.writer().flush();
            }
        }
    }

    // -----------------------------------------------------------------------
    // Step 1: Date to
    // -----------------------------------------------------------------------
    private LocalDate promptDateTo(LineReader reader, Terminal terminal, LocalDate dateFrom) {
        while (true) {
            String input = reader.readLine("📅 Fecha hasta  (YYYY-MM-DD) [Enter = hoy]: ").trim();
            LocalDate dateTo;
            if (input.isBlank()) {
                dateTo = LocalDate.now();
            } else {
                try {
                    dateTo = LocalDate.parse(input);
                } catch (DateTimeParseException e) {
                    terminal.writer().println("  Formato inválido, usá YYYY-MM-DD.");
                    terminal.writer().flush();
                    continue;
                }
            }
            if (dateTo.isBefore(dateFrom)) {
                terminal.writer().println("  La fecha hasta no puede ser anterior a la fecha desde.");
                terminal.writer().flush();
                continue;
            }
            return dateTo;
        }
    }

    // -----------------------------------------------------------------------
    // Step 2: Repo checkbox selection using raw terminal keys
    // -----------------------------------------------------------------------
    private List<RepositoryEntry> promptRepos(Terminal terminal, List<RepositoryEntry> repos) throws IOException {
        boolean[] selected = new boolean[repos.size()];
        // Select first by default
        if (!repos.isEmpty()) {
            selected[0] = true;
        }
        int cursor = 0;

        terminal.writer().println();
        terminal.writer().println("📁 Seleccioná repositorios (↑↓ navegar, espacio seleccionar, A todos, N ninguno, Enter confirmar):");
        terminal.writer().println();
        terminal.writer().flush();

        Attributes savedAttrs = terminal.enterRawMode();
        try {
            renderCheckboxList(terminal, repos, selected, cursor);

            while (true) {
                int c = terminal.reader().read();

                if (c == 27) { // ESC sequence
                    int next = terminal.reader().read();
                    if (next == '[') {
                        int arrow = terminal.reader().read();
                        if (arrow == 'A') { // Up
                            cursor = (cursor - 1 + repos.size()) % repos.size();
                        } else if (arrow == 'B') { // Down
                            cursor = (cursor + 1) % repos.size();
                        }
                    }
                } else if (c == ' ') { // Space: toggle
                    selected[cursor] = !selected[cursor];
                } else if (c == 'A' || c == 'a') {
                    for (int i = 0; i < selected.length; i++) selected[i] = true;
                } else if (c == 'N' || c == 'n') {
                    for (int i = 0; i < selected.length; i++) selected[i] = false;
                } else if (c == '\r' || c == '\n') { // Enter
                    // Validate at least one selected
                    boolean any = false;
                    for (boolean b : selected) if (b) { any = true; break; }
                    if (!any) {
                        // Show error and continue
                        clearCheckboxList(terminal, repos.size());
                        terminal.writer().println("  Seleccioná al menos un repositorio.");
                        terminal.writer().flush();
                        renderCheckboxList(terminal, repos, selected, cursor);
                        continue;
                    }
                    clearCheckboxList(terminal, repos.size());
                    break;
                }

                clearCheckboxList(terminal, repos.size());
                renderCheckboxList(terminal, repos, selected, cursor);
            }
        } finally {
            terminal.setAttributes(savedAttrs);
        }

        List<RepositoryEntry> result = new ArrayList<>();
        for (int i = 0; i < repos.size(); i++) {
            if (selected[i]) result.add(repos.get(i));
        }
        return result;
    }

    private void renderCheckboxList(Terminal terminal, List<RepositoryEntry> repos, boolean[] selected, int cursor) {
        for (int i = 0; i < repos.size(); i++) {
            String pointer = (i == cursor) ? "  ❯ " : "    ";
            String checkbox = selected[i] ? "◉" : "◯";
            terminal.writer().println(pointer + checkbox + " " + repos.get(i).name());
        }
        terminal.writer().flush();
    }

    private void clearCheckboxList(Terminal terminal, int lines) {
        for (int i = 0; i < lines; i++) {
            terminal.writer().print("\033[A\033[2K"); // move up + clear line
        }
        terminal.writer().flush();
    }

    // -----------------------------------------------------------------------
    // Step 3: Include meetings
    // -----------------------------------------------------------------------
    private boolean promptMeetings(LineReader reader, Terminal terminal) {
        while (true) {
            String input = reader.readLine("📋 ¿Incluir reuniones periódicas? (S/n): ").trim();
            if (input.isBlank() || input.equalsIgnoreCase("S") || input.equalsIgnoreCase("s")) {
                return true;
            }
            if (input.equalsIgnoreCase("n")) {
                return false;
            }
            terminal.writer().println("  Respondé S o n.");
            terminal.writer().flush();
        }
    }

    // -----------------------------------------------------------------------
    // Step 4: Distribution mode (radio buttons)
    // -----------------------------------------------------------------------
    private DistributionMode promptDistributionMode(Terminal terminal) throws IOException {
        String[] options = {
            "Riguroso (fecha exacta del commit)",
            "Distribuido (equitativo entre días laborables)"
        };
        int cursor = 0;

        terminal.writer().println();
        terminal.writer().println("📊 Modo de distribución:");
        terminal.writer().flush();

        Attributes savedAttrs2 = terminal.enterRawMode();
        try {
            renderRadioList(terminal, options, cursor);

            while (true) {
                int c = terminal.reader().read();

                if (c == 27) {
                    int next = terminal.reader().read();
                    if (next == '[') {
                        int arrow = terminal.reader().read();
                        if (arrow == 'A') cursor = (cursor - 1 + options.length) % options.length;
                        else if (arrow == 'B') cursor = (cursor + 1) % options.length;
                    }
                } else if (c == '\r' || c == '\n') {
                    clearCheckboxList(terminal, options.length);
                    break;
                }

                clearCheckboxList(terminal, options.length);
                renderRadioList(terminal, options, cursor);
            }
        } finally {
            terminal.setAttributes(savedAttrs2);
        }

        return cursor == 0 ? DistributionMode.STRICT : DistributionMode.DISTRIBUTED;
    }

    private void renderRadioList(Terminal terminal, String[] options, int cursor) {
        for (int i = 0; i < options.length; i++) {
            String pointer = (i == cursor) ? "  ❯ " : "    ";
            String radio = (i == cursor) ? "◉" : "◯";
            terminal.writer().println(pointer + radio + " " + options[i]);
        }
        terminal.writer().flush();
    }

    // -----------------------------------------------------------------------
    // Step 5: Summary
    // -----------------------------------------------------------------------
    private void printSummary(
        Terminal terminal,
        LocalDate dateFrom,
        LocalDate dateTo,
        List<RepositoryEntry> repos,
        boolean includeMeetings,
        DistributionMode mode
    ) {
        String repoNames = repos.stream()
            .map(RepositoryEntry::name)
            .reduce((a, b) -> a + ", " + b)
            .orElse("(ninguno)");
        boolean isToday = dateTo.equals(LocalDate.now());
        String hastaLabel = dateTo + (isToday ? " (hoy)" : "");
        String modeLabel = mode == DistributionMode.STRICT ? "Riguroso" : "Distribuido";

        terminal.writer().println();
        terminal.writer().println("✅ Configuración:");
        terminal.writer().println("   Desde:        " + dateFrom);
        terminal.writer().println("   Hasta:        " + hastaLabel);
        terminal.writer().println("   Repos:        " + repoNames);
        terminal.writer().println("   Reuniones:    " + (includeMeetings ? "sí" : "no"));
        terminal.writer().println("   Distribución: " + modeLabel);
        terminal.writer().println();
        terminal.writer().println("Procesando...");
        terminal.writer().flush();
    }
}
