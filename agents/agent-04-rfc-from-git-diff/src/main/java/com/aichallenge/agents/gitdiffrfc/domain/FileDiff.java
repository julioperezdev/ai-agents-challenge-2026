package com.aichallenge.agents.gitdiffrfc.domain;

public record FileDiff(String path, String content, boolean truncated) {
}
