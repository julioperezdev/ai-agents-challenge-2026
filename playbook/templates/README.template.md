# <agent-name>

<Descripcion breve del agente.>

## Que resuelve
<Problema que resuelve.>

## Enfoque
- <Modo local>
- <Modo AI si aplica>
- <Output principal>

## Uso esperado
Comando principal recomendado:

```bash
./run.sh \
  --repo-path /path/to/target-repo \
  --output OUTPUT.md \
  --ai
```

## Modo AI
Variables esperadas:

```bash
export AWS_REGION=us-east-1
export BEDROCK_MODEL_ID=openai.gpt-oss-20b-1:0
```

## Costos estimados
- Input: USD <x> por 1M tokens.
- Output: USD <y> por 1M tokens.
- Estimacion por ejecucion: USD <range>.
- Guardrail: confirmar si supera USD 1.00.

## Modo local
<Como correr sin AI.>

## Demo
- [`OUTPUT_DEMO.md`](OUTPUT_DEMO.md)

## Estructura
```text
<agent-name>/
├── pom.xml
├── run.sh
├── README.md
├── Specification.md
└── src/
```

## Estado actual
- <Item>

## Roadmap
1. <Item>
