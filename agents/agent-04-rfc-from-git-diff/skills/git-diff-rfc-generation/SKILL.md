---
name: git-diff-rfc-generation
description: Use this skill when you need to generate a technical and functional RFC from a Git diff between branches, commits, or an explicit commit range.
compatibility: Diseñada para agentes CLI que leen repositorios Git locales y generan Markdown revisable por equipos de desarrollo.
metadata:
  author: julio-perez
  version: "0.1"
---

# Git Diff RFC Generation

## Proposito
Esta skill guia la generacion de un RFC a partir de un diff Git real.

## Cuándo usarla
- Cuando el usuario quiere documentar cambios entre ramas.
- Cuando existe un rango Git y se necesita explicar el impacto.
- Cuando se prepara una revision tecnica antes de mergear.

## Checklist
- [ ] Resolver `source`, `target` o `range`.
- [ ] Validar que el directorio sea un repositorio Git.
- [ ] Obtener archivos modificados con tipo de cambio.
- [ ] Obtener estadisticas del diff.
- [ ] Obtener diff textual acotado.
- [ ] Construir un `ChangeSet`.
- [ ] Generar RFC local o con AI.
- [ ] Renderizar Markdown final.

## Estructura RFC requerida
```markdown
# RFC: <generated title>

## Summary

## Change Scope

## Technical Changes

## Functional Impact

## Risks & Considerations

## Open Questions

## Review Checklist
```

## Reglas
- Basar el RFC en evidencia visible del diff.
- Separar cambios tecnicos de impacto funcional.
- No inventar motivaciones si no aparecen en commits, nombres o cambios.
- Si el impacto no se puede inferir, decirlo de forma explicita.
- Mantener la salida accionable y lista para revision humana.

## Gotchas
- No confundir volumen de cambios con importancia.
- No tratar archivos de tests como unico indicador de comportamiento.
- No omitir configuracion, migraciones o dependencias aunque el diff sea chico.
- No enviar diffs enormes completos al LLM.
