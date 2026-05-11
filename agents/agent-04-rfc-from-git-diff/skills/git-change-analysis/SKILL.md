---
name: git-change-analysis
description: Use this skill when you need to inspect modified files from a Git diff and classify what changed before writing documentation or RFC content.
compatibility: Diseñada para agentes que usan Git CLI y necesitan un analisis estructurado de cambios locales.
metadata:
  author: julio-perez
  version: "0.1"
---

# Git Change Analysis

## Proposito
Esta skill ayuda a convertir la salida cruda de Git en un analisis estructurado de cambios.

## Qué mirar
1. Tipo de cambio por archivo:
   - agregado,
   - modificado,
   - eliminado,
   - renombrado.
2. Area del sistema:
   - aplicacion,
   - dominio,
   - infraestructura,
   - configuracion,
   - tests,
   - documentacion.
3. Senales de impacto:
   - APIs publicas,
   - contratos internos,
   - migraciones,
   - dependencias,
   - configuracion,
   - manejo de errores,
   - tests nuevos o eliminados.

## Comandos Git recomendados
```bash
git diff --name-status <range>
git diff --stat <range>
git diff --unified=80 <range>
```

## Heuristicas
- Cambios en `src/main` suelen afectar comportamiento productivo.
- Cambios en `src/test` ayudan a entender intencion, cobertura y riesgo.
- Cambios en `pom.xml`, `build.gradle`, Docker o config pueden alterar ejecucion aunque sean pequenos.
- Renames sin modificaciones deben documentarse como reorganizacion.
- Deletes requieren explicar que capacidad, configuracion o test desaparece.

## Formato de analisis recomendado
```text
Change scope
- Files changed:
- Main areas:
- Production files:
- Test files:
- Config/docs files:

Notable changes
- <path>: <what changed and why it matters>
```

## Reglas
- No asumir comportamiento nuevo solo por nombres de archivos.
- No ignorar archivos de documentacion si explican decisiones del cambio.
- No mezclar todos los archivos en una sola conclusion si hay areas distintas.
