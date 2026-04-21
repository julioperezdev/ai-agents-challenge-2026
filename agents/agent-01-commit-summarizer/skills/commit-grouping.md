# Skill: commit-grouping

## Propósito
Agrupar commits por día calendario para pasar de eventos técnicos aislados a bloques de trabajo diario.

## Input esperado
- Lista de commits con fecha normalizada.

## Output esperado
- Estructura agrupada por fecha.
- Cada fecha contiene la lista de commits de ese día.

## Reglas
- Agrupar por fecha local `YYYY-MM-DD`.
- Mantener orden cronológico ascendente por día.
- Mantener orden estable dentro de cada día.
- Soportar múltiples commits el mismo día.

## Edge cases
- Un solo commit en el rango.
  - Igual debe renderizarse como día independiente.
- Commits exactamente en los límites del rango.
  - Deben incluirse.
- Días sin commits.
  - No deben aparecer en la salida.

## Ejemplos
- Input:
  - `[{date: "2026-04-13", message: "fix: cesco payload"}, {date: "2026-04-13", message: "refactor save flow"}, {date: "2026-04-14", message: "feat: improve persistence"}]`
- Output:
  - `{"2026-04-13": [...2 commits...], "2026-04-14": [...1 commit...]}`
