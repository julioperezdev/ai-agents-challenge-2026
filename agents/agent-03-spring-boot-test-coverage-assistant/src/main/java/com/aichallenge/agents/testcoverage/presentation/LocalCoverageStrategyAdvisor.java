package com.aichallenge.agents.testcoverage.presentation;

import com.aichallenge.agents.testcoverage.domain.CoverageAnalysis;
import com.aichallenge.agents.testcoverage.domain.CoverageStrategy;
import com.aichallenge.agents.testcoverage.domain.port.CoverageStrategyAdvisor;

public class LocalCoverageStrategyAdvisor implements CoverageStrategyAdvisor {
    private final CoveragePlanRenderer renderer;

    public LocalCoverageStrategyAdvisor(CoveragePlanRenderer renderer) {
        this.renderer = renderer;
    }

    @Override
    public CoverageStrategy advise(CoverageAnalysis analysis) {
        return new CoverageStrategy(renderer.render(analysis), false);
    }
}
