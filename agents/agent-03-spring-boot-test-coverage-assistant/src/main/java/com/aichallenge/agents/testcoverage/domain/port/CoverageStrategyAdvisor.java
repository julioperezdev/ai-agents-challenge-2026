package com.aichallenge.agents.testcoverage.domain.port;

import com.aichallenge.agents.testcoverage.domain.CoverageAnalysis;
import com.aichallenge.agents.testcoverage.domain.CoverageStrategy;

import java.io.IOException;

public interface CoverageStrategyAdvisor {
    CoverageStrategy advise(CoverageAnalysis analysis) throws IOException;
}
