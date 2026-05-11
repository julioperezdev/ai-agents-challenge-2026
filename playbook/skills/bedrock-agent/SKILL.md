---
name: bedrock-agent
description: Use this skill when adding AWS Bedrock enrichment to an agent with prompt builders, cost estimation, and fallback behavior.
compatibility: Java agents using AWS Bedrock Runtime.
metadata:
  author: julio-perez
  version: "0.1"
---

# Bedrock Agent

## Proposito
Agregar AI opcional con Bedrock de forma auditable, testeable y con control de costos.

## Checklist
- [ ] Crear `Bedrock<UseCase>PromptBuilder`.
- [ ] Separar prompt builder de writer/client.
- [ ] Delimitar evidencia en el prompt.
- [ ] Prohibir reasoning, code fences, placeholders y Unicode spacing raro.
- [ ] Estimar costo antes de invocar.
- [ ] Pedir confirmacion si supera USD 1.00.
- [ ] Sanitizar respuesta.
- [ ] Usar fallback local si falla o se cancela.

## Reglas
- No enviar AI como unico camino si puede existir modo local.
- No inventar valores no visibles.
- Testear reglas clave del prompt.
- Documentar modelo, region y costo.
