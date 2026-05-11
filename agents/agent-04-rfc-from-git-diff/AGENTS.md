# AGENTS.md

## Purpose
Este agente existe para convertir cambios reales de Git en documentacion tecnica y funcional con formato RFC, ayudando a que un equipo entienda que cambio, por que importa y que deberia revisar antes de mergear.

## Scope
- Comparar ramas locales o rangos Git explicitos.
- Leer archivos modificados, estadisticas y diffs usando Git CLI.
- Generar RFC Markdown para revision de equipo.
- Usar AWS Bedrock de forma opcional para enriquecer el analisis.
- Mantener un modo local deterministico que permita auditoria y demo sin credenciales AWS.

## Non-Goals
- No hacer code review exhaustivo.
- No prometer deteccion completa de bugs, vulnerabilidades o breaking changes.
- No comentar PRs automaticamente en el MVP.
- No indexar todo el repositorio.
- No analizar archivos binarios.
- No inventar decisiones tecnicas que no esten respaldadas por el diff, nombres, commits o contexto visible.

## Style
- Enterprise style, pero pragmatico.
- Priorizar salida clara, revisable y accionable.
- Usar nombres orientados al dominio Git/RFC.
- Evitar abstracciones si no separan una frontera real.
- Mantener la CLI simple y facil de correr.

## Architecture Guidelines
- `application` orquesta el caso de uso.
- `domain` contiene conceptos como `ChangeSet`, `ChangedFile`, `FileDiff` y puertos reales.
- `infrastructure.git` contiene integracion con Git CLI.
- `infrastructure.ai` contiene integracion con AWS Bedrock.
- `infrastructure.input` contiene adapters de entrada como la CLI.
- `infrastructure.output` contiene adapters de salida como Markdown/local RFC.
- No crear interfaces de servicio si no agregan desacoplamiento real.

## Git Diff Guidance
El agente debe basarse en Git real antes que en heuristicas propias.

Comandos recomendados:
- `git diff --name-status <range>`
- `git diff --stat <range>`
- `git diff --unified=<n> <range>`
- `git rev-parse --is-inside-work-tree`

La implementacion debe manejar:
- ramas inexistentes,
- repositorios invalidos,
- rangos invalidos,
- diffs vacios,
- archivos eliminados,
- archivos renombrados,
- diffs demasiado grandes.

## AI Guidance
El modo AI debe usar AWS Bedrock.

Modelo por defecto:

```bash
BEDROCK_MODEL_ID=openai.gpt-oss-20b-1:0
```

Reglas:
- Enviar contexto acotado.
- No enviar archivos completos si el diff basta.
- Incluir lista de archivos, estadisticas y fragmentos relevantes.
- Pedir salida Markdown con la estructura RFC definida.
- Sanitizar bloques internos o respuestas vacias antes de imprimir.
- Si Bedrock falla, mostrar error claro o fallback local segun el modo definido.

## Output Expectations
El RFC debe:
- explicar el cambio en lenguaje de equipo,
- separar impacto tecnico de impacto funcional,
- llamar riesgos y preguntas abiertas,
- no inflar conclusiones,
- dejar una checklist minima de revision.

## Gotchas To Preserve
- Un diff grande no significa automaticamente alto riesgo.
- Un archivo de configuracion pequeno puede tener mucho impacto funcional.
- Un rename sin cambios reales debe tratarse distinto a una modificacion de comportamiento.
- Los tests modificados son evidencia de intencion, pero no reemplazan la explicacion del cambio productivo.
- Si el cambio no permite inferir impacto funcional, decirlo explicitamente.

## Preferred Skill Patterns
- Checklists cortos.
- Templates Markdown.
- Reglas para limitar contexto.
- Heuristicas transparentes y auditables.
- Lenguaje de output listo para revision humana.
