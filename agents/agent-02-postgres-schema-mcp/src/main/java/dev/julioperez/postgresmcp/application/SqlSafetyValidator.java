package dev.julioperez.postgresmcp.application;

import dev.julioperez.postgresmcp.infrastructure.config.SqlProperties;
import org.springframework.stereotype.Component;

import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class SqlSafetyValidator {

    private static final Pattern LIMIT_PATTERN = Pattern.compile("(?i)\\blimit\\s+(\\d+)");
    private static final Pattern FORBIDDEN_PATTERN = Pattern.compile(
        "(?i)\\b(insert|update|delete|drop|alter|truncate|create|grant|revoke|copy|call|do|execute|merge)\\b"
    );

    private final SqlProperties sqlProperties;

    public SqlSafetyValidator(SqlProperties sqlProperties) {
        this.sqlProperties = sqlProperties;
    }

    public String validateAndNormalize(String sql, Integer requestedLimit) {
        String sanitized = sanitize(sql);
        String normalized = sanitized.toLowerCase(Locale.ROOT);

        if (!(normalized.startsWith("select") || normalized.startsWith("with"))) {
            throw new IllegalArgumentException("Only SELECT/WITH statements are allowed.");
        }

        if (containsMultipleStatements(sanitized)) {
            throw new IllegalArgumentException("Multiple SQL statements are not allowed.");
        }

        Matcher forbiddenMatcher = FORBIDDEN_PATTERN.matcher(sanitized);
        if (forbiddenMatcher.find()) {
            throw new IllegalArgumentException("Forbidden SQL keyword detected: " + forbiddenMatcher.group(1).toUpperCase(Locale.ROOT));
        }

        int effectiveLimit = sqlProperties.normalizedMaxQueryLimit();
        if (requestedLimit != null && requestedLimit > 0) {
            effectiveLimit = Math.min(effectiveLimit, requestedLimit);
        }

        return enforceLimit(sanitized, effectiveLimit);
    }

    private String sanitize(String sql) {
        if (sql == null || sql.isBlank()) {
            throw new IllegalArgumentException("Generated SQL is empty.");
        }

        return sql
            .replace("```sql", "")
            .replace("```", "")
            .trim();
    }

    private boolean containsMultipleStatements(String sql) {
        String trimmed = sql.trim();
        int semicolons = trimmed.length() - trimmed.replace(";", "").length();
        if (semicolons == 0) {
            return false;
        }
        return semicolons > 1 || !trimmed.endsWith(";");
    }

    private String enforceLimit(String sql, int limit) {
        String trimmed = sql.trim();
        boolean hasSemicolon = trimmed.endsWith(";");
        String withoutSemicolon = hasSemicolon ? trimmed.substring(0, trimmed.length() - 1).trim() : trimmed;

        Matcher matcher = LIMIT_PATTERN.matcher(withoutSemicolon);
        if (matcher.find()) {
            int currentLimit = Integer.parseInt(matcher.group(1));
            if (currentLimit > limit) {
                withoutSemicolon = matcher.replaceFirst("LIMIT " + limit);
            }
            return withoutSemicolon + ";";
        }

        return withoutSemicolon + " LIMIT " + limit + ";";
    }
}
