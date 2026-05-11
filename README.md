# AI Agents Challenge 2026

## Objective
Construir 1 agente de AI por semana desde el 13 de abril de 2026 hasta el 31 de diciembre de 2026.

## Goal
38 agentes publicados con código abierto y contenido en Instagram.

## Repository Structure
- /agents
- /playbook
- /shared
- /docs

## Playbook
El repo incluye un playbook incubado en [`/playbook`](playbook/README.md) para estandarizar futuros agentes: ciclo de vida, arquitectura, CLI, Bedrock, costos, demos, documentacion, templates y skills internas reutilizables.

## AI Cost Notes
Los agentes deben poder ejecutarse en modo local cuando sea posible. Cuando un agente use AI externa, cada README debe documentar el proveedor, modelo y costo estimado.

Referencia actual para el Agente 04 (`agent-04-rfc-from-git-diff`):
- Proveedor: AWS Bedrock.
- Modelo: `openai.gpt-oss-20b-1:0`.
- Precio de referencia: USD 0.09 por 1M tokens de entrada y USD 0.39 por 1M tokens de salida.
- Ejecucion tipica con `--max-diff-lines 1200`: aproximadamente USD 0.0015 a USD 0.004 por RFC.
- Por defecto usa el 100% del diff disponible.
- El Agente 04 puede ampliar contexto con `--include-full-files` y `--include-related-context`, lo que aumenta tokens y costo.
- Si la estimacion previa a Bedrock supera USD 1.00, la CLI pide confirmacion antes de ejecutar la llamada.

Escala aproximada:

```text
100 RFCs:  USD 0.20 a USD 0.40
1000 RFCs: USD 2 a USD 4
```

Los valores son aproximados y pueden cambiar segun el tamano del diff, el largo de la respuesta y los precios vigentes del proveedor.

## Weekly Rules
- 1 agente funcional
- 1 README por agente
- 1 demo mínima
- 1 publicación en Instagram
- 1 push a GitHub

## Progress
- [X] Semana 01 / Agente 01: CLI para convertir commits en resúmenes diarios de avances
- [X] Semana 02 / Agente 02: MCP Server para explorar PostgreSQL, generar documentación y consultar datos con lenguaje natural
- [X] Semana 03 / Agente 03: asistente para priorizar testing unitario, de integración y de carga en Spring Boot
- [X] Semana 04 / Agente 04: generador de RFC técnico-funcional desde git diff entre ramas o rangos, con demo en `agents/agent-04-rfc-from-git-diff/RFC_DEMO.md`
- [ ] Semana 05 / Agente 05: PLANNED - acompañante de voz no médico para personas mayores con recordatorios simples
  ...
- [ ] Agente 38
