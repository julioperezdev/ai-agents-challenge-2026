package com.aichallenge.agents.commitsummarizer.application;

import java.io.IOException;
import java.util.List;

public interface DailyWorkSummarizer {
    List<String> summarize(DailyWorkSummaryRequest request) throws IOException, InterruptedException;
}
