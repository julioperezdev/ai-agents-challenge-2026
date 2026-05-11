# Architecture Guidelines

## Estructura recomendada para agentes Java/Spring Boot CLI
```text
src/main/java/<base-package>/
├── application/
├── domain/
└── infrastructure/
    ├── input/
    │   └── cli/
    ├── output/
    ├── ai/
    └── <external-provider>/
```

## Responsabilidades
- `application`: orquesta casos de uso. No debe saber detalles de CLI, Bedrock, Git, base de datos o HTTP.
- `domain`: modelos, conceptos y puertos del agente.
- `infrastructure.input`: adapters de entrada, por ejemplo CLI o controllers.
- `infrastructure.output`: adapters de salida, renderers, writers locales, serializacion final.
- `infrastructure.ai`: integraciones con LLMs, prompt builders, costo y parsing de respuesta.
- `infrastructure.<provider>`: integraciones concretas como Git CLI, PostgreSQL, filesystem, APIs externas.

## Reglas
- Preferir puertos reales cuando hay mas de una implementacion o una frontera externa clara.
- Evitar interfaces decorativas si no agregan desacoplamiento.
- Separar prompt building de invocation del modelo.
- Separar renderer/output del caso de uso.
- Mantener el modo local deterministico cuando sea posible.

## Tests esperados
- Parser CLI.
- Caso de uso principal.
- Renderers/output.
- Prompt builders.
- Estimadores o guardrails de costo.
- Adapters externos con tests unitarios o integracion ligera cuando sea razonable.
