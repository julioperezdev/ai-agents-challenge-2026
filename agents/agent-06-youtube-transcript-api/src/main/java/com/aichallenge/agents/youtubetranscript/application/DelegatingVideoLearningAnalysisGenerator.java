package com.aichallenge.agents.youtubetranscript.application;

import com.aichallenge.agents.youtubetranscript.domain.Transcript;
import com.aichallenge.agents.youtubetranscript.domain.VideoLearningAnalysis;
import com.aichallenge.agents.youtubetranscript.domain.port.VideoLearningAnalysisGenerator;
import com.aichallenge.agents.youtubetranscript.infrastructure.bedrock.BedrockVideoLearningAnalysisGenerator;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

@Primary
@Component
public class DelegatingVideoLearningAnalysisGenerator implements VideoLearningAnalysisGenerator {

    private final VideoLearningAnalysisGenerator localGenerator;
    private final ObjectProvider<BedrockVideoLearningAnalysisGenerator> bedrockGenerator;
    private final LearningAnalysisProperties properties;

    public DelegatingVideoLearningAnalysisGenerator(
            @Qualifier("localLearningAnalysisGenerator") VideoLearningAnalysisGenerator localGenerator,
            ObjectProvider<BedrockVideoLearningAnalysisGenerator> bedrockGenerator,
            LearningAnalysisProperties properties
    ) {
        this.localGenerator = localGenerator;
        this.bedrockGenerator = bedrockGenerator;
        this.properties = properties;
    }

    @Override
    public VideoLearningAnalysis analyze(Transcript transcript) {
        if (!"bedrock".equalsIgnoreCase(properties.getProvider())) {
            return localGenerator.analyze(transcript);
        }

        BedrockVideoLearningAnalysisGenerator generator = bedrockGenerator.getIfAvailable();
        if (generator == null) {
            if (properties.isFallbackToLocal()) {
                return localGenerator.analyze(transcript);
            }
            throw new IllegalStateException("Bedrock analysis provider is selected but BEDROCK_ANALYSIS_ENABLED is false.");
        }

        try {
            return generator.analyze(transcript);
        } catch (RuntimeException ex) {
            if (properties.isFallbackToLocal()) {
                return localGenerator.analyze(transcript);
            }
            throw ex;
        }
    }
}
