package com.aichallenge.agents.testcoverage.domain;

import java.nio.file.Path;
import java.util.List;

public record CoverageAnalysis(
    Path projectPath,
    String target,
    int maxRecommendationsPerSection,
    List<SpringComponent> components,
    TestInventory testInventory,
    List<TestRecommendation> recommendations
) {
}
