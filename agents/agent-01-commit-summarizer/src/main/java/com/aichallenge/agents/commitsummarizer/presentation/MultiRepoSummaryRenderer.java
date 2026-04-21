package com.aichallenge.agents.commitsummarizer.presentation;

import com.aichallenge.agents.commitsummarizer.domain.model.DailyEntry;
import com.aichallenge.agents.commitsummarizer.domain.model.MultiRepoSummaryResult;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

public class MultiRepoSummaryRenderer {

    private static final Map<Integer, String> MONTH_NAMES = new HashMap<>();
    private static final Map<DayOfWeek, String> DAY_NAMES = new HashMap<>();
    private static final Map<DayOfWeek, String> DAY_EMOJIS = new HashMap<>();

    static {
        MONTH_NAMES.put(1, "enero");
        MONTH_NAMES.put(2, "febrero");
        MONTH_NAMES.put(3, "marzo");
        MONTH_NAMES.put(4, "abril");
        MONTH_NAMES.put(5, "mayo");
        MONTH_NAMES.put(6, "junio");
        MONTH_NAMES.put(7, "julio");
        MONTH_NAMES.put(8, "agosto");
        MONTH_NAMES.put(9, "septiembre");
        MONTH_NAMES.put(10, "octubre");
        MONTH_NAMES.put(11, "noviembre");
        MONTH_NAMES.put(12, "diciembre");

        DAY_NAMES.put(DayOfWeek.MONDAY, "Lunes");
        DAY_NAMES.put(DayOfWeek.TUESDAY, "Martes");
        DAY_NAMES.put(DayOfWeek.WEDNESDAY, "Miércoles");
        DAY_NAMES.put(DayOfWeek.THURSDAY, "Jueves");
        DAY_NAMES.put(DayOfWeek.FRIDAY, "Viernes");
        DAY_NAMES.put(DayOfWeek.SATURDAY, "Sábado");
        DAY_NAMES.put(DayOfWeek.SUNDAY, "Domingo");

        DAY_EMOJIS.put(DayOfWeek.MONDAY, "📅");
        DAY_EMOJIS.put(DayOfWeek.TUESDAY, "📅");
        DAY_EMOJIS.put(DayOfWeek.WEDNESDAY, "📅");
        DAY_EMOJIS.put(DayOfWeek.THURSDAY, "📅");
        DAY_EMOJIS.put(DayOfWeek.FRIDAY, "📅");
        DAY_EMOJIS.put(DayOfWeek.SATURDAY, "📅");
        DAY_EMOJIS.put(DayOfWeek.SUNDAY, "📅");
    }

    public String render(MultiRepoSummaryResult result) {
        if (result.days().isEmpty()) {
            return "No se encontraron actividades en el rango " + result.dateFrom() + " - " + result.dateTo() + ".";
        }

        StringBuilder builder = new StringBuilder();
        boolean multiRepo = result.multiRepo();

        for (DailyEntry entry : result.days()) {
            builder.append(formatDayHeader(entry.date())).append("\n");

            if (!entry.summaryLines().isEmpty()) {
                for (String line : entry.summaryLines()) {
                    builder.append("- ").append(line).append("\n");
                }
            } else {
                for (String meeting : entry.meetings()) {
                    builder.append("- ").append(meeting).append("\n");
                }

                for (DailyEntry.RepoCommitLine commitLine : entry.commitLines()) {
                    if (multiRepo) {
                        builder.append("- [").append(commitLine.repoName()).append("] ")
                            .append(commitLine.commitMessage()).append("\n");
                    } else {
                        builder.append("- ").append(commitLine.commitMessage()).append("\n");
                    }
                }
            }

            builder.append("\n");
        }

        return builder.toString().trim();
    }

    private String formatDayHeader(LocalDate date) {
        DayOfWeek dow = date.getDayOfWeek();
        String emoji = DAY_EMOJIS.getOrDefault(dow, "📅");
        String dayName = DAY_NAMES.getOrDefault(dow, dow.name());
        String month = MONTH_NAMES.getOrDefault(date.getMonthValue(), String.valueOf(date.getMonthValue()));
        return emoji + " " + dayName + " " + date.getDayOfMonth() + " de " + month;
    }
}
