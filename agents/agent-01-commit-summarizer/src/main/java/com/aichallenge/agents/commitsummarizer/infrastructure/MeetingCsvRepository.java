package com.aichallenge.agents.commitsummarizer.infrastructure;

import com.aichallenge.agents.commitsummarizer.domain.model.MeetingEntry;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.time.DayOfWeek;
import java.util.ArrayList;
import java.util.List;

public class MeetingCsvRepository {

    public List<MeetingEntry> load() throws IOException {
        try (InputStream stream = getClass().getClassLoader().getResourceAsStream("meetings.csv")) {
            if (stream == null) {
                throw new IllegalStateException("No se encontró meetings.csv en el classpath.");
            }
            return parse(stream);
        }
    }

    private List<MeetingEntry> parse(InputStream stream) throws IOException {
        List<MeetingEntry> entries = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8))) {
            String line;
            boolean firstLine = true;
            while ((line = reader.readLine()) != null) {
                String trimmed = line.trim();
                if (trimmed.isBlank() || trimmed.startsWith("#")) {
                    continue;
                }
                // Skip header
                if (firstLine && trimmed.startsWith("dayOfWeek,")) {
                    firstLine = false;
                    continue;
                }
                firstLine = false;
                String[] parts = trimmed.split(",", 2);
                if (parts.length < 2) {
                    continue;
                }
                DayOfWeek dayOfWeek = DayOfWeek.valueOf(parts[0].trim().toUpperCase());
                String name = parts[1].trim();
                entries.add(new MeetingEntry(dayOfWeek, name));
            }
        }
        return entries;
    }
}
