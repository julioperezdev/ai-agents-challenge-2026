# Skill: git-commit-reader

## Propósito
Obtener commits desde un repositorio local usando Git de forma confiable, filtrando por autor y rango de fechas.

## Input esperado
- Ruta del repositorio local.
- Autor a buscar por nombre o email.
- Fecha inicial `YYYY-MM-DD`.
- Fecha final `YYYY-MM-DD`.

## Output esperado
- Lista ordenada de commits con:
  - hash corto o completo
  - fecha del commit
  - mensaje original

## Reglas
- Validar que la ruta apunte a un repositorio Git.
- Consultar commits con `git log`.
- Trabajar en zona horaria local del entorno donde se ejecuta la CLI.
- Tomar días completos para `from` y `to`.
- Excluir commits sin mensaje útil.
- Priorizar una salida determinística y fácil de auditar.

## Edge cases
- El autor no se pasa explícitamente.
  - Intentar autodetección con `git config user.name` o `git config user.email`.
- No hay commits en el rango.
  - Devolver una salida vacía controlada y un mensaje claro para el usuario.
- El repositorio no existe o no es Git.
  - Fallar con error entendible.
- `from` es posterior a `to`.
  - Rechazar la ejecución.

## Ejemplos
- Input:
  - repo: `/workspace/project`
  - author: `Julio Perez`
  - from: `2026-04-13`
  - to: `2026-04-18`
- Output:
  - `[{hash: "a1b2c3d", committed_at: "2026-04-13T10:22:00-0300", message: "feat: update office sync logic"}]`
