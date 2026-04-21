# Specification: agent-01-commit-summarizer

## Problema que resuelve
Registrar avances diarios en un time tracker manual a partir del historial real de trabajo suele ser lento, repetitivo y propenso a omisiones. Este agente toma los commits de Git de un autor en un rango de fechas y los transforma en un resumen diario breve, profesional y fácil de pegar manualmente.

## Objetivo del MVP
Construir una CLI local que lea commits desde un repositorio Git, filtre por autor y rango de fechas, agrupe por día y genere una salida en español orientada a time tracking usando IA de forma obligatoria para el resumen.

## Alcance incluido
- Lectura de commits desde un repositorio local usando `git log`.
- Filtrado por autor.
- Filtrado por rango de fechas.
- Agrupación por día calendario.
- Limpieza básica de mensajes técnicos antes de resumir.
- Transformación de commits y contexto diario en avances laborales entendibles usando AWS Bedrock.
- Renderizado en formato principal `time-tracker`.
- Formato alternativo `raw` para auditoría rápida.
- Whitelist versionada de proyectos locales permitidos para trabajar desde la CLI.
- Documentación del agente, la especificación y los skills reutilizables.

## Alcance excluido
- Integración automática con la web del time tracker.
- Base de datos o persistencia propia.
- Interfaz web o frontend.
- Autenticación.
- Fallback local sin IA para el resumen principal.

## Inputs
- `author`: nombre o email del autor a filtrar. En esta versión también puede autodetectarse desde `git config user.name` o `git config user.email` si no se envía explícitamente.
- `from`: fecha inicial en formato `YYYY-MM-DD`.
- `to`: fecha final en formato `YYYY-MM-DD`.
- `repo-path` opcional: ruta al repositorio Git local. Por defecto usa el directorio actual.
- `project` opcional: alias de un proyecto guardado en la whitelist local.
- `output-format` opcional: `time-tracker`, `markdown` o `raw`.
- `AWS_REGION`: región de Bedrock. Valor recomendado para este MVP: `us-east-1`.
- `BEDROCK_MODEL_ID` opcional: modelo Bedrock a usar. Valor por defecto: `openai.gpt-oss-20b-1:0`.
- Credenciales AWS con permisos para invocar Bedrock Runtime.

## Outputs
- Texto en español listo para copiar y pegar.
- Salida principal agrupada por fecha con bullets de avances diarios generados por IA.
- Salida alternativa `raw` con fecha, hash corto y mensaje limpio por commit.

## Flujo del usuario
1. El usuario ejecuta la CLI indicando autor y rango de fechas.
2. La CLI resuelve el repositorio usando `--repo-path`, `--project` o el directorio actual.
3. El agente obtiene commits con `git log`.
4. El agente filtra, limpia y agrupa los commits por día.
5. El agente envía el contexto diario a AWS Bedrock para obtener un resumen profesional en español.
6. La CLI imprime el resumen final para copiar y pegar manualmente.

## Criterios de aceptación
- El comando corre localmente y usa AWS Bedrock para el resumen principal.
- El repositorio se puede indicar con `--repo-path` o usar el actual.
- El agente puede resolver repositorios por alias usando una whitelist guardada en el proyecto.
- El rango de fechas se valida y contempla los días completos.
- Los commits quedan agrupados por día.
- La salida principal está en español, en formato de bullets `- ...`, y es más útil para time tracking que el `git log` original.
- El agente no intenta interactuar con la web del trabajo.
- Si Bedrock falla o faltan credenciales, el agente falla explícitamente en los formatos que requieren resumen.
- La documentación deja claro el alcance, limitaciones y puntos de extensión.

## Decisiones técnicas
- Lenguaje elegido: Java.
  - Motivo: es el lenguaje dominado por el usuario, facilita mantenimiento futuro y permite una CLI robusta con dependencias mínimas.
- Versión objetivo: Java 25.
  - Motivo: el usuario ya dispone de Corretto 25 localmente y permite alinear el proyecto con la versión moderna que desea mantener.
- Interfaz elegida: CLI.
  - Motivo: el trabajo es lineal, local y orientado a productividad. Una CLI reduce fricción, evita sobrecosto de frontend y se integra bien con scripts futuros.
- Build tool: Maven.
  - Motivo: estandariza compilación, tests y empaquetado en un jar fácil de ejecutar localmente.
- Integración con Git: `ProcessBuilder` + `git log`.
  - Motivo: evita dependencias pesadas como librerías Git de terceros y mantiene el comportamiento alineado con Git real.
- Motor de resumen: AWS Bedrock Runtime.
  - Motivo: el usuario quiere que el uso de IA sea obligatorio y que el resumen convierta mensajes mixtos en inglés/español a un output profesional en español.
- Modelo por defecto: `openai.gpt-oss-20b-1:0`.
  - Motivo: ofrece una relación calidad/precio sólida para resumir mensajes de commit cortos con buena capacidad de reescritura y traducción.
- Región por defecto: `us-east-1`.
  - Motivo: simplifica la configuración inicial del MVP y alinea la implementación con la región elegida por el usuario.
- Arquitectura: `cli`, `application`, `domain`, `infrastructure`, `presentation`.
  - Motivo: separa responsabilidades sin sobreingeniería y deja un punto claro de crecimiento.

## Futuras extensiones
- Detectar automáticamente el autor por identidad Git con mejores reglas y confirmación opcional.
- Soportar plantillas fijas de salida para el formato exacto del trabajo.
- Exportar a `.txt` o `.md`.
- Soportar múltiples repositorios en una sola corrida.
- Agregar reglas configurables para limpiar prefijos, tickets y convenciones internas antes de invocar el modelo.
- Incorporar más contexto al prompt usando diff, ramas o metadatos del repo cuando sea útil.
- Permitir alta, baja y edición de la whitelist desde comandos CLI.
