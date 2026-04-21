# agent-01-commit-summarizer

CLI local para convertir commits de Git en un resumen diario de avances, pensado para copiar y pegar manualmente en el time tracker del trabajo.

## Qué problema resuelve
Evita reconstruir manualmente qué se hizo cada día. Toma los commits reales de un autor, los agrupa por fecha y los transforma con IA en un resumen breve y profesional en español.

## Instalación
Requisitos:
- Java 25
- Maven 3.8 o superior
- Git disponible en el sistema
- Credenciales AWS con acceso a Bedrock Runtime

Variables recomendadas:

```bash
export AWS_REGION=us-east-1
export BEDROCK_MODEL_ID=openai.gpt-oss-20b-1:0
```

Desde la carpeta del agente:

```bash
cd /Users/julio/developer/realProjects/ai-agents-challenge-2026/agents/agent-01-commit-summarizer
export JAVA_HOME=$(/usr/libexec/java_home -v 25)
export PATH="$JAVA_HOME/bin:$PATH"
mvn test
mvn package
```

Esto genera el jar ejecutable en `target/agent-01-commit-summarizer-0.1.0.jar`.

El resumen principal ahora usa AWS Bedrock de forma obligatoria. Si faltan credenciales o acceso al modelo, la ejecución falla con un error claro.

Chequeo rápido de credenciales AWS:

```bash
aws sts get-caller-identity
```

Si tienes Amazon Corretto 25 instalado en macOS, ese comando suele resolverlo automáticamente.

Si prefieres una ejecución rápida sin recordar el comando del jar:

```bash
./run.sh --help
```

## Cómo ejecutarlo

```bash
java -jar target/agent-01-commit-summarizer-0.1.0.jar \
  --author "Julio Perez" \
  --from "2026-04-13" \
  --to "2026-04-18"
```

Opciones principales:
- `--author`: nombre o email del autor. Si se omite, intenta usar `git config user.name` o `git config user.email`.
- `--from`: fecha inicial en formato `YYYY-MM-DD`.
- `--to`: fecha final en formato `YYYY-MM-DD`.
- `--repo-path`: ruta al repo Git a inspeccionar. Por defecto usa el directorio actual.
- `--project`: alias de un proyecto guardado en la whitelist del agente.
- `--output-format`: `time-tracker`, `markdown` o `raw`. Por defecto `time-tracker`.
- `--list-projects`: lista los proyectos guardados en la whitelist.
- `--whitelist-file`: permite usar otro archivo de whitelist si lo necesitas.

Notas sobre el output:
- `time-tracker` y `markdown` usan IA obligatoriamente para generar el resumen diario.
- `raw` no llama a la IA y sirve como salida de auditoría del material base.

## Prueba rápida en terminal

```bash
cd /Users/julio/developer/realProjects/ai-agents-challenge-2026/agents/agent-01-commit-summarizer
export JAVA_HOME=$(/usr/libexec/java_home -v 25)
export PATH="$JAVA_HOME/bin:$PATH"
export AWS_REGION=us-east-1
export BEDROCK_MODEL_ID=openai.gpt-oss-20b-1:0
./run.sh --project rag-sales-offhours --from "2026-03-09" --to "2026-03-22"
```

Qué debería ocurrir:
- `run.sh` compila el proyecto si hace falta y ejecuta la CLI con Java 25.
- El agente lee los commits del repo indicado.
- Agrupa la actividad por día.
- Envía el contexto diario a Bedrock.
- Imprime un resumen final en español, con bullets `- ...`, listo para copiar y pegar.

## Whitelist de proyectos

El agente ahora incluye una whitelist versionada en:

`config/project-whitelist.csv`

Formato:

```text
# alias,path,enabled,notes
mi-proyecto,/Users/julio/developer/realProjects/mi-proyecto,true,Proyecto activo
otro-proyecto,/Users/julio/developer/realProjects/otro-proyecto,false,En pausa
```

Cada fila define:
- `alias`: nombre corto para usar en CLI
- `path`: ruta local al repositorio
- `enabled`: `true` o `false`
- `notes`: contexto opcional

Para ver la whitelist actual:

```bash
./run.sh --list-projects
```

Para ejecutar sobre un proyecto de la whitelist:

```bash
./run.sh --project ai-agents-challenge-2026 --author "Julio Perez" --from "2026-04-13" --to "2026-04-18"
```

## Script de ejecución rápida

```bash
./run.sh --author "Julio Perez" --from "2026-04-13" --to "2026-04-18"
```

`run.sh` intenta usar Java 25 automáticamente. Si quieres fijarlo manualmente:

```bash
export JAVA_HOME=$(/usr/libexec/java_home -v 25)
export PATH="$JAVA_HOME/bin:$PATH"
./run.sh --help
```

Antes de usar el resumen con IA, asegúrate de exportar la región:

```bash
export AWS_REGION=us-east-1
```

## Ejemplo de uso

```bash
java -jar target/agent-01-commit-summarizer-0.1.0.jar \
  --project "rag-sales-offhours" \
  --from "2026-04-13" \
  --to "2026-04-14" \
  --author "Julio Perez"
```

Ejemplo multi-repo:

```bash
./run.sh --from "2026-03-09" --to "2026-04-09" --mode strict
```

Ejemplo de auditoría sin IA:

```bash
./run.sh --project rag-sales-offhours \
  --from "2026-03-09" \
  --to "2026-03-22" \
  --output-format raw
```

## Ejemplo de output

```text
13 de abril de 2026
- Ajustes en la lógica de actualización de oficinas.
- Correcciones en el manejo de datos relacionados con Cesco.
- Refactor y validaciones para reducir inconsistencias del proceso.

14 de abril de 2026
- Mejoras en persistencia y estructura del módulo de guardado.
- Ajustes orientados a la estabilidad del flujo principal.
```

Formato `raw`:

```text
2026-04-13
- a1b2c3d feat: update office sync logic
- d4e5f6g fix: cesco payload validation
```

## Limitaciones actuales
- El resumen principal depende de AWS Bedrock y de credenciales válidas.
- No analiza diffs; resume a partir de mensajes de commit, reuniones y contexto diario.
- No envía información al time tracker; el pegado sigue siendo manual.
- La calidad del resumen depende de la calidad del mensaje del commit y del contexto disponible.
- `raw` no usa IA porque está orientado a revisión y auditoría.
- La salida está en español simple y consistente; todavía no usa plantillas específicas por empresa.
- La llamada real a Bedrock depende de que tu cuenta tenga acceso al modelo `openai.gpt-oss-20b-1:0` en la región configurada.

## Roadmap
- Plantillas de salida configurables para distintos trabajos.
- Exportación a archivo `.txt` o `.md`.
- Soporte para múltiples repositorios.
- Reglas configurables para limpiar tickets, prefijos y convenciones internas antes de enviar a la IA.
- Soporte configurable para elegir modelo Bedrock sin tocar variables manualmente.
- Gestión de whitelist desde la propia CLI sin editar el archivo manualmente.
