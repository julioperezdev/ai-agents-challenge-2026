# agent-04-rfc-from-git-diff

Agente para generar documentacion tecnica y funcional en formato RFC a partir del diff real entre dos ramas, commits o rangos Git.

## Que resuelve
Cuando una rama se mergea, el conocimiento de que cambio y por que suele perderse entre commits, conversaciones y memoria del equipo. Este agente toma el diff real de Git y genera un RFC en Markdown listo para revision.

El objetivo no es reemplazar el criterio del equipo, sino entregar una primera version clara y revisable de la documentacion del cambio.

## Enfoque
El MVP esta pensado como una CLI local:
- lee cambios con Git CLI,
- construye un resumen estructurado del diff,
- genera un RFC local basico,
- opcionalmente usa AWS Bedrock para enriquecer el analisis tecnico-funcional.

## Casos que cubre
- Documentar una feature antes de mergear.
- Resumir cambios tecnicos entre una rama feature y `main`.
- Preparar contexto para una review de equipo.
- Identificar riesgos, dudas y puntos de revision a partir del diff.
- Crear una demo clara usando cambios reales del repositorio.

## Salida esperada
El agente debe generar un RFC con esta estructura:

```markdown
# RFC: <generated title>

## Summary

## Change Scope

## Technical Changes

## Functional Impact

## Risks & Considerations

## Open Questions

## Review Checklist
```

## Uso esperado
Comando principal recomendado:

```bash
./run.sh \
  --repo-path /path/to/target-repo \
  --source feature/my-change \
  --target main \
  --output RFC.md \
  --ai \
  --include-related-context
```

Este modo compara `main..feature/my-change`, envia el diff completo, agrega archivos modificados completos y agrega contexto relacionado encontrado por referencias simples. Antes de invocar Bedrock, estima el costo y pide confirmacion si supera USD 1.00.

Comparar dos ramas en modo local, sin costo de AI:

```bash
./run.sh \
  --repo-path /path/to/target-repo \
  --source feature/my-change \
  --target main \
  --output RFC.md
```

Usar un rango Git explicito:

```bash
./run.sh \
  --repo-path /path/to/target-repo \
  --range main..feature/my-change \
  --output RFC.md \
  --ai
```

Incluir archivos modificados completos sin buscar contexto relacionado:

```bash
./run.sh \
  --repo-path /path/to/target-repo \
  --source feature/my-change \
  --target main \
  --output RFC.md \
  --ai \
  --include-full-files
```

Limitar el diff textual, manteniendo contexto relacionado:

```bash
./run.sh \
  --repo-path /path/to/target-repo \
  --source feature/my-change \
  --target main \
  --output RFC.md \
  --ai \
  --include-related-context \
  --max-diff-lines 1200
```

## Demo
Este repositorio incluye una salida de ejemplo generada con contexto relacionado:

- [`RFC_DEMO.md`](RFC_DEMO.md)

## Modo AI
El modo AI usa AWS Bedrock.

Variables esperadas:

```bash
export AWS_REGION=us-east-1
export BEDROCK_MODEL_ID=openai.gpt-oss-20b-1:0
```

El modo AI recibe:
- lista de archivos modificados,
- tipo de cambio por archivo,
- estadisticas del diff,
- diff textual completo por defecto,
- fragmentos acotados del diff textual si se usa `--max-diff-lines`,
- contenido completo de archivos modificados si se usa `--include-full-files`,
- contexto relacionado si se usa `--include-related-context`,
- contexto del rango comparado.

La salida se orienta a un RFC listo para revision humana, no a una review automatica de bugs.

Antes de invocar Bedrock, el agente estima el costo maximo de la llamada usando el tamano aproximado del prompt y el limite de salida configurado. Si la estimacion supera USD 1.00, la CLI pide confirmacion interactiva. Si no se confirma, no llama a Bedrock y usa el fallback local.

## Costos estimados
El modo local no tiene costo de AI.

El modo `--ai` usa AWS Bedrock con el modelo por defecto:

```bash
BEDROCK_MODEL_ID=openai.gpt-oss-20b-1:0
```

Referencia de precio on-demand usada para estimar:
- Input: USD 0.09 por 1M tokens.
- Output: USD 0.39 por 1M tokens.

Por defecto el agente envia el 100% del diff textual disponible. Para limitar el contexto se puede usar:

```bash
./run.sh --range main..feature/foo --ai --max-diff-lines 1200
```

Los modos de contexto expandido aumentan el costo porque agregan mas tokens:
- `--include-full-files`: agrega el contenido completo de archivos modificados no eliminados y de texto/codigo.
- `--include-related-context`: tambien busca referencias relacionadas por nombre de clase/simbolo y agrega hasta 20 archivos relacionados.

Con `--max-diff-lines 1200`, una ejecucion tipica envia aproximadamente entre 12k y 25k tokens de entrada y genera entre 1k y 3k tokens de salida.

Estimacion practica:

```text
1 RFC:     USD 0.0015 a USD 0.004
100 RFCs:  USD 0.20 a USD 0.40
1000 RFCs: USD 2 a USD 4
```

Ejemplo con 15k tokens de entrada y 2k tokens de salida:

```text
Input:  15,000 / 1,000,000 * 0.09 = USD 0.00135
Output:  2,000 / 1,000,000 * 0.39 = USD 0.00078
Total aproximado: USD 0.00213
```

Estos valores son aproximados. El costo real depende del tamano del diff, del valor de `--max-diff-lines`, del largo del RFC generado y de los precios vigentes de AWS Bedrock.

Regla de seguridad:
- Si la estimacion previa a Bedrock es menor o igual a USD 1.00, el agente ejecuta la llamada.
- Si la estimacion supera USD 1.00, el agente pregunta antes de gastar.
- Si el usuario no confirma, se genera el RFC local sin costo de AI.

## Modo local
El modo local debe funcionar sin credenciales AWS.

Sirve para:
- validar Git y argumentos,
- revisar el alcance del cambio,
- generar un RFC basico,
- tener una demo rapida sin costo.

## Estructura prevista

```text
agent-04-rfc-from-git-diff/
├── pom.xml
├── run.sh
├── README.md
├── AGENTS.md
├── Specification.md
├── src/
│   ├── main/java/com/aichallenge/agents/gitdiffrfc/
│   │   ├── application/
│   │   ├── domain/
│   │   ├── infrastructure/
│   │   │   ├── ai/
│   │   │   ├── git/
│   │   │   ├── input/
│   │   │   │   └── cli/
│   │   │   └── output/
│   └── test/java/com/aichallenge/agents/gitdiffrfc/
└── skills/
    ├── bedrock-diff-context/
    │   └── SKILL.md
    ├── git-change-analysis/
    │   └── SKILL.md
    ├── git-diff-rfc-generation/
    │   └── SKILL.md
    └── rfc-impact-review/
        └── SKILL.md
```

## Skills incluidas
- `git-diff-rfc-generation`: orquesta la generacion de un RFC desde un diff Git.
- `git-change-analysis`: guia el analisis de archivos modificados, renames, deletes y cambios por tipo.
- `rfc-impact-review`: ayuda a separar impacto tecnico, funcional, riesgos y preguntas abiertas.
- `bedrock-diff-context`: define como recortar y preparar el contexto enviado a Bedrock.

## Estado actual
Estructura Spring Boot CLI inicial creada.

Incluye:
- proyecto Maven con Spring Boot,
- entrada CLI con `CommandLineRunner`,
- parsing y validacion de argumentos,
- lectura de diffs mediante Git CLI,
- modelos de dominio `ChangeSet`, `ChangedFile` y `FileDiff`,
- generacion local deterministica del RFC,
- integracion inicial con AWS Bedrock Runtime,
- sanitizacion/render Markdown,
- tests unitarios iniciales.

## Roadmap
1. Endurecer parsing CLI y mensajes de error.
2. Ampliar tests de `GitCliDiffReader` con repositorios temporales.
3. Validar modo AI con credenciales reales de AWS Bedrock.
4. Preparar demo con un diff real del repositorio.
5. Ajustar prompt y heuristicas de impacto con feedback de uso.
