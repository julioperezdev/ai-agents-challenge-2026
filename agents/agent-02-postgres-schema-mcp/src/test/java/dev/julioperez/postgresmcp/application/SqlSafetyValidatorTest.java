package dev.julioperez.postgresmcp.application;

import dev.julioperez.postgresmcp.infrastructure.config.SqlProperties;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class SqlSafetyValidatorTest {

    private final SqlSafetyValidator validator = new SqlSafetyValidator(new SqlProperties(50));

    @Test
    void shouldAppendLimitWhenMissing() {
        String sql = validator.validateAndNormalize("SELECT * FROM customers", 20);
        assertEquals("SELECT * FROM customers LIMIT 20;", sql);
    }

    @Test
    void shouldRejectDeleteStatements() {
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> validator.validateAndNormalize("DELETE FROM customers", 10)
        );

        assertEquals("Only SELECT/WITH statements are allowed.", exception.getMessage());
    }

    @Test
    void shouldReduceExistingLimitToConfiguredMaximum() {
        String sql = validator.validateAndNormalize("SELECT * FROM customers LIMIT 100;", 80);
        assertEquals("SELECT * FROM customers LIMIT 50;", sql);
    }
}
