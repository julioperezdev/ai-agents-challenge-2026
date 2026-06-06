package com.aichallenge.agents.youtubetranscript.application;

import com.aichallenge.agents.youtubetranscript.domain.TranscriptProxyUsage;

public record TranscriptProxyUsageResponse(
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
    public static TranscriptProxyUsageResponse from(TranscriptProxyUsage usage) {
        return new TranscriptProxyUsageResponse(
                usage.route(),
                usage.requestCount(),
                usage.requestBytes(),
                usage.responseBytes(),
                usage.totalBytes(),
                usage.totalMb(),
                usage.proxyPricePerGbUsd(),
                usage.estimatedProxyCostUsd(),
                usage.httpStatusesJson(),
                usage.elapsedSeconds()
        );
    }
}
