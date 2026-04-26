# agent-03-spring-boot-test-coverage-assistant

Agente especializado en Spring Boot para reducir fricción de testing y ayudar a cubrir lo más importante de una API con pruebas unitarias, de integración y de carga básica.

## Qué resuelve
Este agente está pensado para un problema real de backend: muchas veces existen endpoints y features funcionando, pero la cobertura importante queda incompleta o desordenada. El agente ayuda a identificar qué conviene testear primero y qué tipo de prueba aplica mejor en cada caso.

## Enfoque
El foco del MVP no es “escribir todos los tests automáticamente”, sino:
- analizar el código de una API Spring Boot,
- revisar los tests actuales,
- detectar gaps relevantes,
- proponer una estrategia mínima y útil de testing.

## Casos que cubre
- lógica de negocio sin pruebas unitarias claras,
- endpoints sin cobertura HTTP,
- validaciones o errores no cubiertos,
- persistencia sin pruebas de integración relevantes,
- necesidad de smoke/load tests mínimos en endpoints críticos.

## Salida esperada
El agente debería poder devolver algo como:

```text
Feature: Users

Unit tests prioritarios
- Cubrir email duplicado en UserService.
- Cubrir usuario inexistente en UserService.
- Cubrir mapping a UserResponse.

Integración prioritaria
- POST /users responde 201 con payload válido.
- POST /users responde 409 si el email ya existe.
- GET /users/{id} responde 404 si no existe.

Carga mínima sugerida
- Smoke test para POST /users con 20 requests concurrentes.
- Verificar p95 < 300 ms en GET /users/{id}.
```

## Estructura
```text
agent-03-spring-boot-test-coverage-assistant/
├── specification.md
├── README.md
├── AGENTS.md
└── skills/
    ├── spring-boot-test-strategy/
    │   └── SKILL.md
    ├── unit-testing/
    │   └── SKILL.md
    ├── integration-testing/
    │   └── SKILL.md
    └── load-testing/
        └── SKILL.md
```

## Skills incluidas
- `spring-boot-test-strategy`
  Orquesta el análisis general de cobertura para un módulo o feature.
- `unit-testing`
  Ayuda a detectar y diseñar pruebas unitarias útiles.
- `integration-testing`
  Ayuda a decidir qué pruebas HTTP, DB o contexto Spring valen la pena.
- `load-testing`
  Ayuda a proponer smoke/load tests mínimos para endpoints críticos.

## Estado actual
La carpeta deja preparada la base documental del agente:
- visión del producto,
- especificación del MVP,
- guía interna del agente,
- skills en formato Agent Skills.

La implementación de código del agente queda como siguiente paso.

## Roadmap
1. CLI o interfaz inicial para analizar un proyecto Spring Boot local.
2. Detección de controllers, services, repositories y tests existentes.
3. Generación de plan priorizado de cobertura.
4. Soporte futuro para skeletons de tests.
5. Soporte futuro para recomendaciones de `k6` o `Testcontainers`.
