package com.aichallenge.agents.youtubetranscript.infrastructure.provider;

import java.util.Map;

public record PythonProxyUsageResponse(
        String route,
        Integer requestCount,
        Long requestBytes,
        Long responseBytes,
        Long totalBytes,
        Double totalMb,
        Double proxyPricePerGbUsd,
        Double estimatedProxyCostUsd,
        Map<String, Integer> httpStatuses,
        Double elapsedSeconds
) {
}
