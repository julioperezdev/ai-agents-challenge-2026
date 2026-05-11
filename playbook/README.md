# AI Agents Playbook

Este playbook esta incubado dentro de `ai-agents-challenge-2026`.

Su objetivo es estandarizar como se disenan, implementan, documentan y demo-ean los agentes del challenge. Cuando este estable, se puede migrar al repositorio dedicado de Playbook.

## Principios
- Cada agente debe resolver un problema claro y demostrable.
- Cada agente debe tener un modo local cuando sea posible.
- Si usa AI externa, debe documentar proveedor, modelo, costos y guardrails.
- La arquitectura debe separar caso de uso, dominio y adapters concretos.
- La demo debe ser reproducible y versionable.
- El README debe mostrar el comando principal antes que detalles secundarios.

## Estructura
```text
playbook/
├── README.md
├── agent-lifecycle.md
├── architecture.md
├── bedrock-agents.md
├── cli-agents.md
├── cost-controls.md
├── demos.md
├── documentation.md
├── prompt-guidelines.md
├── templates/
│   ├── AGENTS.template.md
│   ├── README.template.md
│   ├── SKILL.template.md
│   └── Specification.template.md
└── skills/
    ├── agent-docs/
    ├── bedrock-agent/
    └── spring-boot-cli-agent/
```

## Uso recomendado
1. Crear `Specification.md` desde `templates/Specification.template.md`.
2. Definir arquitectura usando `architecture.md`.
3. Si es CLI, seguir `cli-agents.md`.
4. Si usa Bedrock, seguir `bedrock-agents.md`, `prompt-guidelines.md` y `cost-controls.md`.
5. Crear README desde `templates/README.template.md`.
6. Crear demo siguiendo `demos.md`.
7. Capturar aprendizajes nuevos en este playbook.

## Skills internas
Las skills dentro de `playbook/skills` son patrones reutilizables para futuros agentes. No reemplazan la especificacion de cada agente; sirven como memoria operativa para acelerar implementaciones consistentes.
