package dev.julioperez.postgresmcp.application;

import dev.julioperez.postgresmcp.domain.QueryResult;
import dev.julioperez.postgresmcp.shared.ConsoleTableFormatter;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;

class ConsoleTableFormatterTest {

    private final ConsoleTableFormatter formatter = new ConsoleTableFormatter();

    @Test
    void shouldRenderAsciiTable() {
        QueryResult result = new QueryResult(
            "SELECT 1",
            List.of("id", "full_name"),
            List.of(
                List.of("1", "Ana Torres"),
                List.of("2", "Carlos Rivas")
            )
        );

        String rendered = formatter.format(result);

        assertTrue(rendered.contains("| id |"));
        assertTrue(rendered.contains("Ana Torres"));
        assertTrue(rendered.contains("Carlos Rivas"));
    }
}
