---
name: bedrock-diff-context
description: Use this skill when preparing Git diff context to send to AWS Bedrock while keeping prompts bounded, auditable, and useful for RFC generation.
compatibility: Diseñada para agentes Java que invocan AWS Bedrock Runtime con diffs Git locales.
metadata:
  author: julio-perez
  version: "0.1"
---

# Bedrock Diff Context

## Proposito
Esta skill define como preparar contexto de un diff Git para AWS Bedrock sin saturar el modelo ni perder trazabilidad.

## Modelo esperado
```bash
BEDROCK_MODEL_ID=openai.gpt-oss-20b-1:0
```

## Contexto recomendado
Enviar al modelo:
- rango comparado,
- ramas origen/destino si existen,
- resumen `git diff --stat`,
- lista `git diff --name-status`,
- fragmentos acotados del diff,
- limite aplicado al diff.

No enviar:
- archivos binarios,
- secretos,
- archivos completos si el diff es suficiente,
- logs extensos,
- output repetitivo o generado.

## Estrategia de recorte
- Definir `--max-diff-lines`.
- Priorizar archivos productivos antes que generados.
- Mantener contexto de configuracion aunque sea pequeno.
- Incluir tests cuando expliquen comportamiento o riesgo.
- Marcar explicitamente cuando un diff fue truncado.

## Prompt guidance
Pedir al modelo:
- generar Markdown,
- usar la estructura RFC definida,
- basarse solo en evidencia visible,
- marcar incertidumbre,
- separar impacto tecnico y funcional,
- listar riesgos y preguntas abiertas.

## Reglas
- El prompt debe ser deterministico y auditable.
- El output debe poder usarse aunque el modelo sea conservador.
- Si Bedrock devuelve razonamiento interno o texto vacio, la CLI debe sanitizarlo y usar fallback claro.
- Los errores de credenciales o region deben ser entendibles para un developer.
