package com.aichallenge.agents.youtubetranscript.domain;

public record TranscriptProxyUsage(
        String route,
        int requestCount,
        long requestBytes,
        long responseBytes,
        long totalBytes,
        double totalMb,
        double proxyPricePerGbUsd,
        double estimatedProxyCostUsd,
        String httpStatusesJson,
        double elapsedSeconds
) {
    public static TranscriptProxyUsage empty() {
        return new TranscriptProxyUsage("unknown", 0, 0, 0, 0, 0.0, 0.0, 0.0, "{}", 0.0);
    }
}
