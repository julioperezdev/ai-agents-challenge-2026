package com.aichallenge.agents.commitsummarizer.infrastructure;

import com.aichallenge.agents.commitsummarizer.domain.model.ProjectWhitelistEntry;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

public class ProjectWhitelistRepository {
    public List<ProjectWhitelistEntry> load(Path whitelistFile) throws IOException {
        Path resolved = whitelistFile.toAbsolutePath().normalize();
        if (!Files.exists(resolved)) {
            throw new IllegalArgumentException("No se encontró la whitelist en '" + resolved + "'.");
        }

        List<ProjectWhitelistEntry> entries = new ArrayList<>();
        List<String> lines = Files.readAllLines(resolved, StandardCharsets.UTF_8);
        for (String rawLine : lines) {
            String line = rawLine.trim();
            if (line.isBlank() || line.startsWith("#")) {
                continue;
            }

            String[] parts = line.split(",", 4);
            if (parts.length < 3) {
                throw new IllegalArgumentException("Línea inválida en la whitelist: " + rawLine);
            }

            String alias = parts[0].trim();
            Path repoPath = Path.of(parts[1].trim()).toAbsolutePath().normalize();
            boolean enabled = Boolean.parseBoolean(parts[2].trim());
            String notes = parts.length == 4 ? parts[3].trim() : "";

            entries.add(new ProjectWhitelistEntry(alias, repoPath, enabled, notes));
        }

        entries.sort(Comparator.comparing(ProjectWhitelistEntry::alias));
        return entries;
    }

    public ProjectWhitelistEntry findEnabledByAlias(Path whitelistFile, String alias) throws IOException {
        String normalizedAlias = alias.trim().toLowerCase(Locale.ROOT);

        return load(whitelistFile).stream()
            .filter(ProjectWhitelistEntry::enabled)
            .filter(entry -> entry.alias().toLowerCase(Locale.ROOT).equals(normalizedAlias))
            .findFirst()
            .orElseThrow(() -> new IllegalArgumentException(
                "No se encontró un proyecto habilitado con alias '" + alias + "' en la whitelist."
            ));
    }
}
