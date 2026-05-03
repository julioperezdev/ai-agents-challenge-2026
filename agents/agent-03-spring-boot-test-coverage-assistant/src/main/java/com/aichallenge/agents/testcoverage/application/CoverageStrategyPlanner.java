package com.aichallenge.agents.testcoverage.application;

import com.aichallenge.agents.testcoverage.domain.CoverageAnalysis;
import com.aichallenge.agents.testcoverage.domain.CoverageStrategy;
import com.aichallenge.agents.testcoverage.domain.port.CoverageStrategyAdvisor;

import java.io.IOException;

public class CoverageStrategyPlanner {
    private final SpringBootTestCoverageAnalyzer analyzer;
    private final CoverageStrategyAdvisor strategyAdvisor;

    public CoverageStrategyPlanner(SpringBootTestCoverageAnalyzer analyzer, CoverageStrategyAdvisor strategyAdvisor) {
        this.analyzer = analyzer;
        this.strategyAdvisor = strategyAdvisor;
    }

    public CoverageStrategy plan(CoverageAnalysisRequest request) throws IOException {
        CoverageAnalysis analysis = analyzer.analyze(request);
        return strategyAdvisor.advise(analysis);
    }
}
