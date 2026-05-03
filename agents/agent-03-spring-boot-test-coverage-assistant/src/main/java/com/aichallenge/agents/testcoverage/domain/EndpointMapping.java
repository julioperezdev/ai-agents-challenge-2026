package com.aichallenge.agents.testcoverage.domain;

public record EndpointMapping(String httpMethod, String path) {
    public String displayName() {
        if (path == null || path.isBlank()) {
            return httpMethod;
        }
        return httpMethod + " " + path;
    }
}
