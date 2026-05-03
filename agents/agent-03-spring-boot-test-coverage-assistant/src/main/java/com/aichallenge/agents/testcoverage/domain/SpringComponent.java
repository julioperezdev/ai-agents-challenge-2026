package com.aichallenge.agents.testcoverage.domain;

import java.nio.file.Path;
import java.util.List;
import java.util.Set;

public record SpringComponent(
    String packageName,
    String className,
    Path sourcePath,
    ComponentCategory category,
    Set<String> annotations,
    List<EndpointMapping> endpoints,
    boolean hasBranchingLogic,
    boolean hasExceptionHandling,
    boolean hasValidationAnnotations,
    boolean hasPersistenceSpecificLogic
) {
    public String qualifiedName() {
        if (packageName == null || packageName.isBlank()) {
            return className;
        }
        return packageName + "." + className;
    }

    public boolean hasRealLogic() {
        return hasBranchingLogic || hasExceptionHandling || hasPersistenceSpecificLogic;
    }
}
