package com.aichallenge.agents.testcoverage.domain;

public record TestRecommendation(
    Priority priority,
    TestType type,
    String title,
    String detail,
    String suggestedTool
) {
}
