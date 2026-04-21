package com.aichallenge.agents.commitsummarizer.domain.model;

public enum DistributionMode {
    STRICT,
    DISTRIBUTED;

    public static DistributionMode from(String value) {
        if (value == null || value.isBlank()) {
            return STRICT;
        }
        return switch (value.trim().toLowerCase()) {
            case "strict", "riguroso" -> STRICT;
            case "distributed", "distribuido" -> DISTRIBUTED;
            default -> throw new IllegalArgumentException(
                "Modo de distribución inválido: '" + value + "'. Usa strict o distributed."
            );
        };
    }
}
