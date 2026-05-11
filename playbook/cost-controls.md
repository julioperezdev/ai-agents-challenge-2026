# Cost Controls

## Documentacion obligatoria
Si un agente usa AI externa, su README debe incluir:
- proveedor,
- modelo,
- precio de referencia,
- estimacion por ejecucion,
- variables de entorno,
- modo sin costo si existe,
- guardrail de costo.

## Estimacion
Regla practica:

```text
tokens ~= chars / 4
input_cost = input_tokens / 1_000_000 * input_price
output_cost = max_output_tokens / 1_000_000 * output_price
```

Para `openai.gpt-oss-20b-1:0` en Bedrock:
- Input: USD 0.09 por 1M tokens.
- Output: USD 0.39 por 1M tokens.

## Guardrail recomendado
- Estimar costo antes de invocar el modelo.
- Si supera USD 1.00, pedir confirmacion.
- Si el usuario no confirma, usar fallback local o cancelar con mensaje claro.

## Contexto expandido
Flags como `--include-full-files` o `--include-related-context` aumentan costo. Deben documentarse explicitamente.
