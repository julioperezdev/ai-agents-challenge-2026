package com.aichallenge.agents.testcoverage.application;

import java.nio.file.Path;

public record CoverageAnalysisRequest(Path projectPath, String target, int maxRecommendationsPerSection) {
    public CoverageAnalysisRequest(Path projectPath, String target) {
        this(projectPath, target, 8);
    }
}
