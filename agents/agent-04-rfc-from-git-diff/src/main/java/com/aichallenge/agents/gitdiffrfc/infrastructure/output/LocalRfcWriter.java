package com.aichallenge.agents.gitdiffrfc.infrastructure.output;

import com.aichallenge.agents.gitdiffrfc.domain.ChangeSet;
import com.aichallenge.agents.gitdiffrfc.domain.ChangedFile;
import com.aichallenge.agents.gitdiffrfc.domain.RfcDocument;
import com.aichallenge.agents.gitdiffrfc.domain.RfcWriter;
import java.util.Comparator;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

@Component
@Primary
public class LocalRfcWriter implements RfcWriter {

    @Override
    public RfcDocument write(ChangeSet changeSet) {
        String title = title(changeSet);
        StringBuilder markdown = new StringBuilder();
        markdown.append("# RFC: ").append(title).append("\n\n");
        markdown.append("## Summary\n");
        if (changeSet.files().isEmpty()) {
            markdown.append("No se detectaron cambios en el rango `").append(changeSet.range()).append("`.\n\n");
        } else {
            markdown.append("Este RFC resume los cambios detectados en `").append(changeSet.range())
                    .append("` a partir del diff real de Git. La version local es conservadora y no infiere motivaciones no visibles en el diff.\n\n");
        }
        appendScope(markdown, changeSet);
        appendTechnicalChanges(markdown, changeSet);
        appendFunctionalImpact(markdown, changeSet);
        appendRisks(markdown, changeSet);
        appendOpenQuestions(markdown, changeSet);
        appendChecklist(markdown);
        return new RfcDocument(markdown.toString());
    }

    private String title(ChangeSet changeSet) {
        if (changeSet.source().isPresent() && changeSet.target().isPresent()) {
            return "Cambios de " + changeSet.source().get() + " hacia " + changeSet.target().get();
        }
        return "Cambios en " + changeSet.range();
    }

    private void appendScope(StringBuilder markdown, ChangeSet changeSet) {
        markdown.append("## Change Scope\n");
        markdown.append("- Source: ").append(changeSet.source().orElse("N/A")).append("\n");
        markdown.append("- Target: ").append(changeSet.target().orElse("N/A")).append("\n");
        markdown.append("- Range: `").append(changeSet.range()).append("`\n");
        markdown.append("- Files changed: ").append(changeSet.filesChanged()).append("\n");
        markdown.append("- Additions: ").append(changeSet.additions()).append("\n");
        markdown.append("- Deletions: ").append(changeSet.deletions()).append("\n");
        markdown.append("- Main areas touched: ").append(mainAreas(changeSet)).append("\n\n");
    }

    private void appendTechnicalChanges(StringBuilder markdown, ChangeSet changeSet) {
        markdown.append("## Technical Changes\n");
        if (changeSet.files().isEmpty()) {
            markdown.append("- No hay archivos modificados para documentar.\n\n");
            return;
        }
        for (ChangedFile file : changeSet.files()) {
            markdown.append("- `").append(file.path()).append("`: ").append(file.type());
            file.previousPath().ifPresent(previous -> markdown.append(" desde `").append(previous).append("`"));
            markdown.append(".\n");
        }
        if (changeSet.diffTruncated()) {
            markdown.append("- El diff textual fue recortado a ").append(changeSet.maxDiffLines()).append(" lineas para mantener el contexto manejable.\n");
        } else if (changeSet.maxDiffLines() == 0) {
            markdown.append("- El diff textual se proceso completo, sin limite de lineas configurado.\n");
        }
        if (!changeSet.fullFiles().isEmpty()) {
            markdown.append("- Se incluyeron ").append(changeSet.fullFiles().size()).append(" archivos modificados completos como contexto adicional.\n");
        }
        if (!changeSet.relatedContextFiles().isEmpty()) {
            markdown.append("- Se incluyeron ").append(changeSet.relatedContextFiles().size()).append(" archivos relacionados como contexto adicional.\n");
        }
        markdown.append("\n");
    }

    private void appendFunctionalImpact(StringBuilder markdown, ChangeSet changeSet) {
        markdown.append("## Functional Impact\n");
        if (containsProductionFiles(changeSet)) {
            markdown.append("- Hay cambios en codigo productivo; el impacto funcional debe revisarse contra el comportamiento esperado del cambio.\n");
        } else {
            markdown.append("- No hay evidencia suficiente en el listado de archivos para afirmar un cambio funcional directo.\n");
        }
        if (containsConfigFiles(changeSet)) {
            markdown.append("- Hay cambios de configuracion o build que pueden afectar ejecucion, empaquetado o integracion.\n");
        }
        if (containsTestFiles(changeSet)) {
            markdown.append("- Hay cambios en tests que pueden documentar intencion o cobertura, pero deben revisarse junto al codigo productivo.\n");
        }
        markdown.append("\n");
    }

    private void appendRisks(StringBuilder markdown, ChangeSet changeSet) {
        markdown.append("## Risks & Considerations\n");
        if (changeSet.files().isEmpty()) {
            markdown.append("- El rango no contiene cambios; confirmar que la comparacion usada es la correcta.\n");
        } else {
            markdown.append("- Confirmar que los archivos listados cubren todo el alcance esperado del cambio.\n");
            if (containsDeletedFiles(changeSet)) {
                markdown.append("- Hay archivos eliminados; revisar compatibilidad, referencias rotas y capacidades removidas.\n");
            }
            if (containsConfigFiles(changeSet)) {
                markdown.append("- Los cambios de configuracion/build pueden requerir validacion operativa adicional.\n");
            }
            if (!containsTestFiles(changeSet)) {
                markdown.append("- No se detectaron cambios en tests en el alcance del diff.\n");
            }
        }
        markdown.append("\n");
    }

    private void appendOpenQuestions(StringBuilder markdown, ChangeSet changeSet) {
        markdown.append("## Open Questions\n");
        markdown.append("- Que comportamiento esperado deberia validar el equipo antes de mergear?\n");
        markdown.append("- Hay consumidores internos o externos afectados por estos cambios?\n");
        if (changeSet.diffTruncated()) {
            markdown.append("- El recorte del diff oculta algun cambio relevante que deba revisarse manualmente?\n");
        }
        markdown.append("\n");
    }

    private void appendChecklist(StringBuilder markdown) {
        markdown.append("## Review Checklist\n");
        markdown.append("- [ ] El comportamiento esperado esta claro.\n");
        markdown.append("- [ ] Los riesgos principales fueron revisados.\n");
        markdown.append("- [ ] Los tests relevantes fueron considerados.\n");
        markdown.append("- [ ] La documentacion es suficiente para revisar el cambio.\n");
    }

    private String mainAreas(ChangeSet changeSet) {
        Map<String, Long> counts = changeSet.files().stream()
                .map(file -> area(file.path()))
                .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));
        if (counts.isEmpty()) {
            return "N/A";
        }
        return counts.entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue(Comparator.reverseOrder()))
                .map(entry -> entry.getKey() + " (" + entry.getValue() + ")")
                .collect(Collectors.joining(", "));
    }

    private String area(String path) {
        if (path.startsWith("src/main/") || path.contains("/src/main/")) {
            return "application";
        }
        if (path.startsWith("src/test/") || path.contains("/src/test/")) {
            return "tests";
        }
        if (path.endsWith(".md") || path.contains("/docs/")) {
            return "documentation";
        }
        if (path.endsWith("pom.xml") || path.endsWith(".gradle") || path.contains("Dockerfile") || path.contains("application.")) {
            return "configuration";
        }
        return "other";
    }

    private boolean containsProductionFiles(ChangeSet changeSet) {
        return changeSet.files().stream().anyMatch(file -> file.path().startsWith("src/main/") || file.path().contains("/src/main/"));
    }

    private boolean containsTestFiles(ChangeSet changeSet) {
        return changeSet.files().stream().anyMatch(file -> file.path().startsWith("src/test/") || file.path().contains("/src/test/"));
    }

    private boolean containsConfigFiles(ChangeSet changeSet) {
        return changeSet.files().stream().anyMatch(file -> "configuration".equals(area(file.path())));
    }

    private boolean containsDeletedFiles(ChangeSet changeSet) {
        return changeSet.files().stream().anyMatch(file -> file.type().name().equals("DELETED"));
    }
}
