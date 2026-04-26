package dev.julioperez.postgresmcp.shared;

import dev.julioperez.postgresmcp.domain.QueryResult;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class ConsoleTableFormatter {

    public String format(QueryResult result) {
        if (result.rows().isEmpty()) {
            return "No rows found.";
        }

        List<Integer> widths = new ArrayList<>();
        for (int index = 0; index < result.columns().size(); index++) {
            int width = result.columns().get(index).length();
            for (List<String> row : result.rows()) {
                width = Math.max(width, row.get(index).length());
            }
            widths.add(width);
        }

        StringBuilder builder = new StringBuilder();
        String separator = buildSeparator(widths);
        builder.append(separator).append("\n");
        builder.append(buildRow(result.columns(), widths)).append("\n");
        builder.append(separator).append("\n");
        for (List<String> row : result.rows()) {
            builder.append(buildRow(row, widths)).append("\n");
        }
        builder.append(separator);
        return builder.toString();
    }

    private String buildSeparator(List<Integer> widths) {
        StringBuilder builder = new StringBuilder("+");
        for (int width : widths) {
            builder.append("-".repeat(width + 2)).append("+");
        }
        return builder.toString();
    }

    private String buildRow(List<String> cells, List<Integer> widths) {
        StringBuilder builder = new StringBuilder("|");
        for (int index = 0; index < cells.size(); index++) {
            builder.append(" ")
                .append(padRight(cells.get(index), widths.get(index)))
                .append(" |");
        }
        return builder.toString();
    }

    private String padRight(String value, int width) {
        return value + " ".repeat(Math.max(0, width - value.length()));
    }
}
