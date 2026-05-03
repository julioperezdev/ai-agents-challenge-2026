package com.aichallenge.agents.testcoverage.infrastructure;

import com.aichallenge.agents.testcoverage.domain.ComponentCategory;
import com.aichallenge.agents.testcoverage.domain.EndpointMapping;
import com.aichallenge.agents.testcoverage.domain.SpringComponent;
import com.aichallenge.agents.testcoverage.domain.TestClass;
import com.aichallenge.agents.testcoverage.domain.TestInventory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

public class JavaSourceProjectScanner {
    private static final Pattern PACKAGE_PATTERN = Pattern.compile("\\bpackage\\s+([\\w.]+)\\s*;");
    private static final Pattern TYPE_PATTERN = Pattern.compile("\\b(class|interface|record|enum)\\s+(\\w+)");
    private static final Pattern ANNOTATION_PATTERN = Pattern.compile("@([A-Za-z][A-Za-z0-9_]*)");
    private static final Pattern MAPPING_PATTERN = Pattern.compile("@(GetMapping|PostMapping|PutMapping|DeleteMapping|PatchMapping|RequestMapping)\\s*(\\(([^)]*)\\))?");
    private static final Pattern PATH_ATTRIBUTE_PATTERN = Pattern.compile("(?:value|path)\\s*=\\s*\"([^\"]+)\"");
    private static final Pattern DIRECT_PATH_PATTERN = Pattern.compile("\"([^\"]+)\"");

    public List<SpringComponent> scanProductionComponents(Path projectPath, String target) throws IOException {
        Path sourceRoot = projectPath.resolve(Path.of("src", "main", "java"));
        if (!Files.isDirectory(sourceRoot)) {
            throw new IllegalArgumentException("No existe src/main/java en " + projectPath.toAbsolutePath());
        }

        try (Stream<Path> paths = Files.walk(sourceRoot)) {
            return paths
                .filter(path -> path.toString().endsWith(".java"))
                .filter(path -> matchesTarget(sourceRoot, path, target))
                .map(path -> readComponent(sourceRoot, path))
                .filter(component -> component.category() != ComponentCategory.OTHER)
                .toList();
        }
    }

    public TestInventory scanTests(Path projectPath, List<SpringComponent> components) throws IOException {
        Path testRoot = projectPath.resolve(Path.of("src", "test", "java"));
        if (!Files.isDirectory(testRoot)) {
            return new TestInventory(List.of());
        }

        try (Stream<Path> paths = Files.walk(testRoot)) {
            List<TestClass> tests = paths
                .filter(path -> path.toString().endsWith(".java"))
                .map(path -> readTestClass(path, components))
                .toList();
            return new TestInventory(tests);
        }
    }

    private SpringComponent readComponent(Path sourceRoot, Path path) {
        String content = readString(path);
        String packageName = findFirst(PACKAGE_PATTERN, content);
        String className = findTypeName(path, content);
        Set<String> annotations = findAnnotations(content);
        ComponentCategory category = classify(className, annotations, content);
        List<EndpointMapping> endpoints = findEndpoints(content);

        return new SpringComponent(
            packageName,
            className,
            sourceRoot.relativize(path),
            category,
            annotations,
            normalizeEndpoints(category, endpoints),
            hasBranchingLogic(content),
            content.contains("throw new ") || content.contains("@ExceptionHandler"),
            hasValidationAnnotations(content),
            hasPersistenceSpecificLogic(content)
        );
    }

    private TestClass readTestClass(Path path, List<SpringComponent> components) {
        String content = readString(path);
        String className = findTypeName(path, content);
        Set<String> annotations = findAnnotations(content);
        if (content.contains("MockMvc")) {
            annotations.add("MockMvc");
        }
        if (content.contains("Mockito") || content.contains("@Mock") || content.contains("@MockBean")) {
            annotations.add("Mockito");
        }
        if (content.contains("org.junit.jupiter") || content.contains("@Test")) {
            annotations.add("JUnit5");
        }

        Set<String> referencedProductionClasses = new HashSet<>();
        for (SpringComponent component : components) {
            if (className.startsWith(component.className())
                || content.contains(component.className())
                || content.contains(component.qualifiedName())) {
                referencedProductionClasses.add(component.className());
            }
        }

        return new TestClass(className, path, annotations, referencedProductionClasses);
    }

    private boolean matchesTarget(Path sourceRoot, Path path, String target) {
        if (target == null || target.isBlank()) {
            return true;
        }
        String normalizedTarget = target.toLowerCase(Locale.ROOT);
        String relativePath = sourceRoot.relativize(path).toString().toLowerCase(Locale.ROOT);
        String fileName = path.getFileName().toString().toLowerCase(Locale.ROOT);
        return relativePath.contains(normalizedTarget.replace('.', '/'))
            || relativePath.contains(normalizedTarget)
            || fileName.contains(normalizedTarget);
    }

    private ComponentCategory classify(String className, Set<String> annotations, String content) {
        if (annotations.contains("RestController") || annotations.contains("Controller")) {
            return ComponentCategory.CONTROLLER;
        }
        if (annotations.contains("Service") || className.endsWith("Service") || className.endsWith("UseCase")) {
            return ComponentCategory.SERVICE;
        }
        if (annotations.contains("Repository") || className.endsWith("Repository") || content.contains("JpaRepository")) {
            return ComponentCategory.REPOSITORY;
        }
        if (className.endsWith("Request") || className.endsWith("Response") || className.endsWith("Dto") || className.endsWith("DTO")) {
            return ComponentCategory.DTO;
        }
        if (className.endsWith("Exception") || content.contains("extends RuntimeException") || content.contains("extends Exception")) {
            return ComponentCategory.EXCEPTION;
        }
        if (annotations.contains("Configuration")) {
            return ComponentCategory.CONFIGURATION;
        }
        return ComponentCategory.OTHER;
    }

    private List<EndpointMapping> findEndpoints(String content) {
        List<EndpointMapping> endpoints = new ArrayList<>();
        Matcher matcher = MAPPING_PATTERN.matcher(content);
        while (matcher.find()) {
            String annotation = matcher.group(1);
            String rawArguments = matcher.group(3);
            endpoints.add(new EndpointMapping(httpMethodFor(annotation), extractPath(rawArguments)));
        }
        return endpoints;
    }

    private List<EndpointMapping> normalizeEndpoints(ComponentCategory category, List<EndpointMapping> endpoints) {
        if (category != ComponentCategory.CONTROLLER || endpoints.isEmpty()) {
            return endpoints;
        }

        EndpointMapping first = endpoints.get(0);
        if (!"REQUEST".equals(first.httpMethod()) || first.path().isBlank() || endpoints.size() == 1) {
            return endpoints;
        }

        List<EndpointMapping> normalized = new ArrayList<>();
        for (int index = 1; index < endpoints.size(); index++) {
            EndpointMapping endpoint = endpoints.get(index);
            if ("REQUEST".equals(endpoint.httpMethod())) {
                normalized.add(endpoint);
                continue;
            }
            normalized.add(new EndpointMapping(endpoint.httpMethod(), joinPaths(first.path(), endpoint.path())));
        }
        return normalized.isEmpty() ? endpoints : normalized;
    }

    private String joinPaths(String basePath, String methodPath) {
        if (methodPath == null || methodPath.isBlank()) {
            return basePath;
        }
        if (basePath.endsWith("/") && methodPath.startsWith("/")) {
            return basePath + methodPath.substring(1);
        }
        if (!basePath.endsWith("/") && !methodPath.startsWith("/")) {
            return basePath + "/" + methodPath;
        }
        return basePath + methodPath;
    }

    private String httpMethodFor(String annotation) {
        return switch (annotation) {
            case "GetMapping" -> "GET";
            case "PostMapping" -> "POST";
            case "PutMapping" -> "PUT";
            case "DeleteMapping" -> "DELETE";
            case "PatchMapping" -> "PATCH";
            default -> "REQUEST";
        };
    }

    private String extractPath(String rawArguments) {
        if (rawArguments == null || rawArguments.isBlank()) {
            return "";
        }
        Matcher attributeMatcher = PATH_ATTRIBUTE_PATTERN.matcher(rawArguments);
        if (attributeMatcher.find()) {
            return attributeMatcher.group(1);
        }
        Matcher directMatcher = DIRECT_PATH_PATTERN.matcher(rawArguments);
        if (directMatcher.find()) {
            return directMatcher.group(1);
        }
        return "";
    }

    private boolean hasBranchingLogic(String content) {
        return Pattern.compile("\\b(if|switch|for|while|catch)\\b").matcher(content).find()
            || content.contains(".filter(")
            || content.contains(".map(")
            || content.contains("Optional<");
    }

    private boolean hasValidationAnnotations(String content) {
        return content.contains("@NotNull")
            || content.contains("@NotBlank")
            || content.contains("@NotEmpty")
            || content.contains("@Email")
            || content.contains("@Size")
            || content.contains("@Valid")
            || content.contains("@Validated")
            || content.contains("@Min")
            || content.contains("@Max");
    }

    private boolean hasPersistenceSpecificLogic(String content) {
        return content.contains("@Query")
            || content.contains("@Modifying")
            || content.contains("JdbcTemplate")
            || content.contains("EntityManager")
            || content.contains("NamedParameterJdbcTemplate")
            || content.contains("nativeQuery");
    }

    private Set<String> findAnnotations(String content) {
        Set<String> annotations = new HashSet<>();
        Matcher matcher = ANNOTATION_PATTERN.matcher(content);
        while (matcher.find()) {
            annotations.add(matcher.group(1));
        }
        return annotations;
    }

    private String findTypeName(Path path, String content) {
        String typeName = findFirst(TYPE_PATTERN, content);
        if (typeName != null) {
            return typeName;
        }
        String fileName = path.getFileName().toString();
        return fileName.substring(0, fileName.length() - ".java".length());
    }

    private String findFirst(Pattern pattern, String content) {
        Matcher matcher = pattern.matcher(content);
        if (!matcher.find()) {
            return null;
        }
        return matcher.group(matcher.groupCount());
    }

    private String readString(Path path) {
        try {
            return Files.readString(path);
        } catch (IOException exception) {
            throw new IllegalArgumentException("No se pudo leer " + path.toAbsolutePath(), exception);
        }
    }
}
