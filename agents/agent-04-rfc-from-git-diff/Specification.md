# Specification: agent-04-rfc-from-git-diff

## Problema que resuelve
Cuando una rama se mergea, gran parte del conocimiento sobre que cambio, por que cambio y que impacto tiene queda distribuido entre commits, pull requests, conversaciones y memoria del equipo.

Este agente reduce esa friccion generando automaticamente documentacion tecnica y funcional en formato RFC a partir del diff real entre dos ramas, commits o rangos Git.

## Objetivo del MVP
Construir una CLI local en Java que analice los cambios entre una rama origen y una rama destino, obtenga el diff mediante Git CLI y genere un RFC en Markdown listo para revision de equipo usando AWS Bedrock.

El MVP debe producir una documentacion clara, revisable y util para developers, sin intentar reemplazar el criterio tecnico del equipo.

## Modelo AI
El agente usa AWS Bedrock como proveedor de LLM.

Modelo por defecto:

```bash
BEDROCK_MODEL_ID=openai.gpt-oss-20b-1:0
```

Variables esperadas:

```bash
AWS_REGION=us-east-1
BEDROCK_MODEL_ID=openai.gpt-oss-20b-1:0
```

## Alcance incluido
- CLI local en Java.
- Lectura de cambios mediante Git CLI.
- Soporte para comparar:
  - rama origen contra rama destino,
  - rango explicito de commits.
- Obtencion de:
  - archivos modificados,
  - tipo de cambio por archivo,
  - estadisticas del diff,
  - diff textual acotado.
- Analisis del cambio con LLM usando AWS Bedrock.
- Generacion de RFC en Markdown.
- Modo local basico sin AI como fallback o auditoria rapida.
- README con instrucciones de uso.
- Demo usando un diff real del propio repositorio.

## Alcance excluido
- Integracion automatica con GitHub, GitLab o Bitbucket.
- Creacion automatica de pull requests.
- Comentarios automaticos en pull requests.
- Review de seguridad o deteccion exhaustiva de bugs.
- Analisis de archivos binarios.
- Indexacion completa del repositorio.
- Persistencia en base de datos.
- Interfaz web.

## Inputs
- `--source`: rama origen del cambio.
- `--target`: rama destino contra la cual comparar.
- `--range`: rango Git alternativo, por ejemplo `main..feature/foo`.
- `--repo-path`: ruta del repositorio local. Opcional, por defecto usa el directorio actual.
- `--output`: archivo Markdown de salida. Opcional.
- `--ai`: activa generacion enriquecida con AWS Bedrock.
- `--max-diff-lines`: limite opcional de lineas de diff enviadas al modelo. Si se omite o se usa `0`, se envia el diff completo.
- `--include-full-files`: incluye contenido completo de archivos modificados no eliminados.
- `--include-related-context`: incluye archivos modificados completos y archivos relacionados encontrados por referencias simples.

Ejemplos:

```bash
./run.sh --source feature/rfc-demo --target main --ai
./run.sh --range main..feature/rfc-demo --output RFC.md --ai
./run.sh --repo-path /path/to/repo --source feature/foo --target main
./run.sh --repo-path /path/to/repo --source feature/foo --target main --ai --include-full-files
./run.sh --repo-path /path/to/repo --source feature/foo --target main --output RFC.md --ai --include-related-context
```

## Outputs
- RFC en Markdown impreso por consola o guardado en archivo.
- Resumen de archivos modificados.
- Secciones tecnicas y funcionales listas para revision.

## Formato del RFC
El RFC generado debe seguir esta estructura:

```markdown
# RFC: <generated title>

## Summary
Descripcion breve de que se hizo y por que.

## Change Scope
- Source:
- Target:
- Files changed:
- Additions:
- Deletions:
- Main areas touched:

## Technical Changes
Descripcion de los cambios tecnicos relevantes:
- archivos modificados,
- clases o metodos afectados,
- configuracion,
- dependencias,
- contratos internos.

## Functional Impact
Descripcion del impacto funcional:
- comportamiento nuevo,
- comportamiento modificado,
- impacto para usuarios,
- impacto para otros sistemas,
- cambios operativos.

## Risks & Considerations
- Breaking changes posibles.
- Edge cases.
- Riesgos tecnicos.
- Deuda tecnica.
- Tests faltantes o recomendados.

## Open Questions
Preguntas o decisiones pendientes para revision del equipo.

## Review Checklist
- [ ] El comportamiento esperado esta claro.
- [ ] Los riesgos principales fueron revisados.
- [ ] Los tests relevantes fueron considerados.
- [ ] La documentacion es suficiente para revisar el cambio.
```

## Flujo de ejecucion
1. Validar argumentos de entrada.
2. Resolver el repositorio local.
3. Validar que el directorio sea un repositorio Git.
4. Resolver el rango de comparacion:
   - si se reciben `--source` y `--target`, construir comparacion entre ramas;
   - si se recibe `--range`, usar ese rango directamente.
5. Ejecutar comandos Git para obtener:
   - archivos modificados,
   - estadisticas,
   - diff textual.
6. Construir un modelo interno `ChangeSet`.
7. Recortar o agrupar diffs grandes solo si se configura `--max-diff-lines`.
8. Si se configura `--include-full-files`, leer desde Git el contenido completo de los archivos modificados.
9. Si se configura `--include-related-context`, buscar referencias relacionadas y leer esos archivos desde Git.
10. Generar RFC local basico.
11. Si `--ai` esta activo, estimar el costo de Bedrock antes de invocar el modelo.
12. Si la estimacion supera USD 1.00, pedir confirmacion interactiva.
13. Enviar el contexto a Bedrock si corresponde.
14. Sanitizar la respuesta del modelo.
15. Renderizar el RFC final en Markdown.
16. Imprimir por consola o guardar en el archivo indicado.

## Arquitectura esperada

```text
agent-04-rfc-from-git-diff/
├── pom.xml
├── run.sh
├── README.md
├── AGENTS.md
├── SPECIFICATION.md
└── src/main/java/com/aichallenge/agents/gitdiffrfc/
    ├── application/
    ├── domain/
    ├── infrastructure/
    │   ├── ai/
    │   ├── git/
    │   ├── input/
    │   │   └── cli/
    │   └── output/
```

## Componentes principales
- `Main`: entrada CLI.
- `GenerateRfcRequest`: contrato de entrada del caso de uso.
- `GitDiffRfcGenerator`: orquesta el flujo principal.
- `ChangeSet`: representa el cambio completo.
- `ChangedFile`: representa un archivo modificado.
- `FileDiff`: representa el diff de un archivo.
- `CodeContextFile`: representa contexto adicional de codigo enviado al modelo.
- `GitDiffReader`: puerto para leer cambios desde Git.
- `GitCliDiffReader`: implementacion usando Git CLI.
- `RfcWriter`: puerto para generacion del RFC.
- `BedrockRfcWriter`: implementacion con AWS Bedrock.
- `LocalRfcWriter`: fallback deterministico.
- `MarkdownRfcRenderer`: renderiza salida final.

## Criterios de aceptacion
- El agente puede comparar dos ramas locales.
- El agente puede comparar un rango Git explicito.
- El agente lista archivos modificados con tipo de cambio.
- El agente genera un RFC Markdown con la estructura definida.
- El modo `--ai` usa AWS Bedrock con `BEDROCK_MODEL_ID=openai.gpt-oss-20b-1:0`.
- El modo local funciona sin credenciales AWS.
- Por defecto se considera el diff completo antes de enviar al modelo.
- Los diffs grandes pueden limitarse con `--max-diff-lines`.
- Puede incluir archivos modificados completos con `--include-full-files`.
- Puede incluir contexto relacionado con `--include-related-context`.
- Si el costo estimado de Bedrock supera USD 1.00, se pide confirmacion antes de ejecutar la llamada.
- La documentacion explica como correr una demo real.
- El codigo sigue una arquitectura clara por capas, sin sobreingenieria.

## Decisiones tecnicas
- Lenguaje: Java.
- Build tool: Maven.
- Interfaz: CLI.
- Git: uso de Git CLI mediante `ProcessBuilder`.
- AI: AWS Bedrock Runtime.
- Modelo por defecto: `openai.gpt-oss-20b-1:0`.
- Salida principal: Markdown.
- Arquitectura: `application`, `domain`, `infrastructure.input`, `infrastructure.output`, `infrastructure.git`, `infrastructure.ai`.

## Orden recomendado de desarrollo
1. Crear estructura base del agente en `/agents/agent-04-rfc-from-git-diff`.
2. Configurar `pom.xml`, `run.sh` y clase `Main`.
3. Implementar parsing y validacion de argumentos CLI.
4. Crear modelos de dominio: `ChangeSet`, `ChangedFile`, `FileDiff`.
5. Implementar `GitCliDiffReader` usando comandos Git.
6. Construir el caso de uso `GitDiffRfcGenerator`.
7. Implementar generacion local basica del RFC.
8. Implementar render Markdown.
9. Agregar integracion con AWS Bedrock.
10. Disenar prompt para analisis tecnico-funcional del diff.
11. Agregar limites de contexto para diffs grandes.
12. Agregar manejo de errores claro para Git, argumentos invalidos y Bedrock.
13. Agregar tests unitarios para parsing, reader, renderer y caso de uso.
14. Crear README con ejemplos de ejecucion.
15. Crear una demo real comparando una rama del propio repositorio.
16. Validar ejecucion local sin AI.
17. Validar ejecucion con `--ai`.
18. Pulir salida final para que sea clara, profesional y publicable.

## Futuras extensiones
- Integracion con pull requests.
- Comentarios automáticos en PRs con resumen del RFC.
- Exportacion a Confluence, Notion o Linear.
- Plantillas RFC configurables por equipo.
- Deteccion de ownership por directorio.
- Analisis separado por modulo o bounded context.
- Generacion de changelog para releases.
