package com.aichallenge.agents.testcoverage.domain;

import java.nio.file.Path;
import java.util.Set;

public record TestClass(
    String className,
    Path sourcePath,
    Set<String> annotations,
    Set<String> referencedProductionClasses
) {
    public boolean uses(String annotationOrTool) {
        return annotations.contains(annotationOrTool);
    }
}
