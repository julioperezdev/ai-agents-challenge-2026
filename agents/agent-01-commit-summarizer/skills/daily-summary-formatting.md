# Skill: daily-summary-formatting

## Propósito
Convertir mensajes de commit técnicos en avances diarios breves, claros y profesionales para reportes manuales.

## Input esperado
- Lista de mensajes de commit limpios para un mismo día.

## Output esperado
- Lista de bullets en español que describen avances laborales.

## Reglas
- Transformar verbos técnicos en lenguaje entendible para negocio o seguimiento interno.
- Mantener el resumen breve.
- Evitar repetir tickets, hashes y prefijos técnicos si no agregan valor.
- Priorizar verbos como `Implementación`, `Correcciones`, `Mejoras`, `Refactor`, `Validaciones`, `Optimización`.
- Si varios commits son muy similares, intentar resumirlos con redacción más general.

## Edge cases
- Mensajes muy escuetos como `fix`, `wip`, `tmp`.
  - Devolver una redacción genérica pero útil.
- Mensajes con prefijos como `feat:`, `fix:`, `refactor:`, `chore:`.
  - Usarlos como señal semántica, no como texto literal.
- Mensajes con tickets tipo `ABC-123`.
  - Eliminar el ticket del resumen si no aporta contexto.

## Ejemplos
- Input:
  - `feat: update office sync logic`
  - `fix: cesco payload validation`
  - `refactor: save flow checks`
- Output:
  - `Implementación de mejoras en la lógica de sincronización de oficinas.`
  - `Correcciones para la validación del payload de Cesco.`
  - `Refactor y validaciones en el flujo de guardado.`
