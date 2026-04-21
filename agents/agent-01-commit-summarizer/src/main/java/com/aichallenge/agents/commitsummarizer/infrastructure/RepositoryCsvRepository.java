package com.aichallenge.agents.commitsummarizer.infrastructure;

import com.aichallenge.agents.commitsummarizer.domain.model.RepositoryEntry;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class RepositoryCsvRepository {

    public List<RepositoryEntry> load() throws IOException {
        try (InputStream stream = getClass().getClassLoader().getResourceAsStream("repositories.csv")) {
            if (stream == null) {
                throw new IllegalStateException("No se encontró repositories.csv en el classpath.");
            }
            return parse(stream);
        }
    }

    private List<RepositoryEntry> parse(InputStream stream) throws IOException {
        List<RepositoryEntry> entries = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8))) {
            String line;
            boolean firstLine = true;
            while ((line = reader.readLine()) != null) {
                String trimmed = line.trim();
                if (trimmed.isBlank() || trimmed.startsWith("#")) {
                    continue;
                }
                // Skip header
                if (firstLine && trimmed.startsWith("name,")) {
                    firstLine = false;
                    continue;
                }
                firstLine = false;
                String[] parts = trimmed.split(",", 2);
                if (parts.length < 2) {
                    continue;
                }
                String name = parts[0].trim();
                Path path = Path.of(parts[1].trim());
                entries.add(new RepositoryEntry(name, path));
            }
        }
        return entries;
    }
}
