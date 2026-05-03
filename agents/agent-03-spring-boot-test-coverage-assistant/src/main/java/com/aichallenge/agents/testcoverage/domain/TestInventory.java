package com.aichallenge.agents.testcoverage.domain;

import java.util.List;

public record TestInventory(List<TestClass> tests) {
    public boolean hasAnyTests() {
        return !tests.isEmpty();
    }

    public boolean covers(String productionClassName) {
        return tests.stream().anyMatch(test -> test.referencedProductionClasses().contains(productionClassName));
    }

    public boolean hasWebMvcCoverage(String productionClassName) {
        return tests.stream()
            .filter(test -> test.referencedProductionClasses().contains(productionClassName))
            .anyMatch(test -> test.uses("WebMvcTest") || test.uses("MockMvc"));
    }

    public boolean hasSpringBootCoverage(String productionClassName) {
        return tests.stream()
            .filter(test -> test.referencedProductionClasses().contains(productionClassName))
            .anyMatch(test -> test.uses("SpringBootTest"));
    }

    public boolean hasDataJpaCoverage(String productionClassName) {
        return tests.stream()
            .filter(test -> test.referencedProductionClasses().contains(productionClassName))
            .anyMatch(test -> test.uses("DataJpaTest") || test.uses("Testcontainers"));
    }
}
