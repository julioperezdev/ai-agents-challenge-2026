package com.aichallenge.agents.youtubetranscript.application;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "app.learning-analysis")
public class LearningAnalysisProperties {

    private String provider = "local";
    private boolean fallbackToLocal = true;
    private Bedrock bedrock = new Bedrock();

    public String getProvider() {
        return provider;
    }

    public void setProvider(String provider) {
        this.provider = provider;
    }

    public boolean isFallbackToLocal() {
        return fallbackToLocal;
    }

    public void setFallbackToLocal(boolean fallbackToLocal) {
        this.fallbackToLocal = fallbackToLocal;
    }

    public Bedrock getBedrock() {
        return bedrock;
    }

    public void setBedrock(Bedrock bedrock) {
        this.bedrock = bedrock;
    }

    public static class Bedrock {
        private boolean enabled;
        private String region = "us-east-1";
        private String profile = "";
        private String modelId = "openai.gpt-oss-20b-1:0";
        private int maxTokens = 4096;
        private double temperature = 0.2;
        private int maxTranscriptChars = 140000;

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        public String getRegion() {
            return region;
        }

        public void setRegion(String region) {
            this.region = region;
        }

        public String getProfile() {
            return profile;
        }

        public void setProfile(String profile) {
            this.profile = profile;
        }

        public String getModelId() {
            return modelId;
        }

        public void setModelId(String modelId) {
            this.modelId = modelId;
        }

        public int getMaxTokens() {
            return maxTokens;
        }

        public void setMaxTokens(int maxTokens) {
            this.maxTokens = maxTokens;
        }

        public double getTemperature() {
            return temperature;
        }

        public void setTemperature(double temperature) {
            this.temperature = temperature;
        }

        public int getMaxTranscriptChars() {
            return maxTranscriptChars;
        }

        public void setMaxTranscriptChars(int maxTranscriptChars) {
            this.maxTranscriptChars = maxTranscriptChars;
        }
    }
}
