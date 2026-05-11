# CLI Agent Guidelines

## Flags recomendados
- `--repo-path`: cuando el agente analiza otro proyecto local.
- `--output`: archivo de salida opcional.
- `--ai`: activa enriquecimiento con AI.
- `--max-*`: limites configurables para contexto, filas, archivos o tokens.
- `--help`: muestra uso.

## Comportamiento
- El modo local debe funcionar sin credenciales.
- El comando principal debe ser reproducible desde `run.sh`.
- Si se genera un archivo, imprimir la ruta final.
- Los errores de argumentos deben ser claros y devolver codigo distinto de cero.
- Evitar comandos destructivos.

## `run.sh`
Recomendado para proyectos Maven:

```bash
#!/usr/bin/env bash
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "$SCRIPT_DIR"

./mvnw -q -DskipTests clean package 2>/dev/null || mvn -q -DskipTests clean package

java -jar target/<artifact>-<version>.jar "$@"
```

Usar `clean package` evita clases viejas despues de mover paquetes.
