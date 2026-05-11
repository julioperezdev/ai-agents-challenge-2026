# Bedrock Agent Guidelines

## Variables
```bash
export AWS_REGION=us-east-1
export BEDROCK_MODEL_ID=openai.gpt-oss-20b-1:0
```

## Patron recomendado
```text
infrastructure/ai/
├── Bedrock<UseCase>Writer.java
├── Bedrock<UseCase>PromptBuilder.java
├── BedrockCostEstimator.java
├── BedrockCostGuard.java
└── BedrockCostEstimate.java
```

## Reglas
- AI debe ser opcional si el agente puede tener fallback local.
- El prompt builder debe ser testeable.
- El writer debe enfocarse en invocar Bedrock y parsear respuesta.
- Sanitizar respuesta: remover reasoning, code fences y espacios Unicode no deseados.
- Si Bedrock falla, entregar error claro o fallback local.
- Antes de llamar Bedrock, estimar costo.
- Si la estimacion supera USD 1.00, pedir confirmacion interactiva.

## Contexto
Preferir contexto delimitado:

```text
<change_scope>
...
</change_scope>

<files>
...
</files>

<diff_excerpts>
...
</diff_excerpts>
```

El modelo debe poder distinguir evidencia primaria, contexto adicional e instrucciones.
