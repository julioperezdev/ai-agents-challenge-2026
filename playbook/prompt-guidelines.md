# Prompt Guidelines

## Reglas base
- Pedir solo el resultado final.
- Prohibir reasoning, analysis notes, XML tags no solicitados y code fences.
- Prohibir placeholders como `<your-name>` si no estan en la evidencia.
- Pedir espacios ASCII normales; evitar NBSP y NNBSP.
- Exigir que no invente endpoints, tablas, campos, metodos o config keys.
- Si un dato no esta visible, debe decir `not visible in diff excerpts` o equivalente.
- Separar instrucciones de evidencia con delimitadores claros.

## Estructura recomendada
```text
You are generating <artifact> from evidence.

Rules:
- ...

Required output structure:
- ...

Evidence:
<scope>
...
</scope>

<primary_evidence>
...
</primary_evidence>

<additional_context>
...
</additional_context>
```

## Tests minimos
El prompt builder debe testear:
- reglas de no invencion,
- estructura requerida,
- presencia de evidencia clave,
- delimitadores,
- escape de atributos si se usan tags,
- manejo de contexto vacio.
