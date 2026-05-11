---
name: rfc-impact-review
description: Use this skill when you need to transform technical Git changes into RFC sections covering functional impact, risks, considerations, and open questions.
compatibility: Diseñada para agentes que redactan documentacion tecnica revisable en Markdown.
metadata:
  author: julio-perez
  version: "0.1"
---

# RFC Impact Review

## Proposito
Esta skill ayuda a redactar las secciones de impacto, riesgos y preguntas abiertas de un RFC generado desde un diff.

## Separacion esperada
- `Technical Changes`: que cambio en codigo, configuracion, dependencias o estructura.
- `Functional Impact`: que comportamiento cambia para usuarios, sistemas o procesos.
- `Risks & Considerations`: que puede romperse, quedar incompleto o requerir atencion.
- `Open Questions`: que decisiones deberia revisar el equipo.

## Checklist
- [ ] Identificar si hay cambios productivos.
- [ ] Identificar si hay cambios de tests.
- [ ] Identificar si hay cambios de configuracion o dependencias.
- [ ] Inferir impacto funcional solo cuando hay evidencia suficiente.
- [ ] Marcar incertidumbre cuando el diff no permite concluir.
- [ ] Proponer preguntas abiertas concretas.

## Riesgos frecuentes
- Breaking changes en APIs o contratos internos.
- Cambios de configuracion no documentados.
- Tests eliminados o cobertura reducida.
- Cambios sin migracion o compatibilidad.
- Edge cases no cubiertos.
- Comportamiento nuevo sin tests visibles.

## Formato recomendado
```markdown
## Functional Impact
- ...

## Risks & Considerations
- ...

## Open Questions
- ...
```

## Reglas
- Evitar frases genericas como "podria haber riesgos".
- Explicar por que un riesgo existe.
- Si no hay evidencia de impacto funcional, escribirlo claramente.
- Priorizar puntos que ayuden a revisar el merge.
