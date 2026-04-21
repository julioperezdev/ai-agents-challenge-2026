# Skill: time-tracker-output-format

## Propósito
Renderizar el resumen diario final en un formato cómodo para copiar y pegar manualmente en una web de time tracking.

## Input esperado
- Fechas ya agrupadas.
- Bullets diarios en español.
- Formato de salida solicitado.

## Output esperado
- Texto plano o markdown listo para copy/paste.

## Reglas
- El formato principal debe privilegiar legibilidad y velocidad de pegado.
- Separar claramente cada día.
- Usar bullets simples.
- No incluir metadatos técnicos salvo que se pida el formato `raw`.
- Mantener una estructura consistente entre ejecuciones.

## Edge cases
- No hay commits.
  - Mostrar un mensaje breve y claro, sin stack traces.
- Un día con muchos commits.
  - Mantener bullets concisos y evitar párrafos largos.
- Formato `raw`.
  - Preservar hash corto y mensaje limpio para trazabilidad.

## Ejemplos
- Output `time-tracker`:
  - `13 de abril de 2026`
  - `- Ajustes en lógica de actualización de oficinas.`
  - `- Corrección de manejo de datos relacionados con Cesco.`
- Output `raw`:
  - `2026-04-13`
  - `- a1b2c3d fix: cesco payload validation`
