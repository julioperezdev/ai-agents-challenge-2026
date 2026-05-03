# agent-03-spring-boot-test-coverage-assistant

Agente especializado en Spring Boot para reducir fricción de testing y ayudar a cubrir lo más importante de una API con pruebas unitarias, de integración y de carga básica.

## Qué resuelve
Este agente está pensado para un problema real de backend: muchas veces existen endpoints y features funcionando, pero la cobertura importante queda incompleta o desordenada. El agente ayuda a identificar qué conviene testear primero y qué tipo de prueba aplica mejor en cada caso.

## Enfoque
El foco del MVP no es “escribir todos los tests automáticamente”, sino:
- analizar el código de una API Spring Boot,
- revisar los tests actuales,
- detectar gaps relevantes,
- proponer una estrategia mínima y útil de testing con una capa AI opcional.

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
├── pom.xml
├── run.sh
├── specification.md
├── README.md
├── AGENTS.md
├── src/
│   ├── main/java/com/aichallenge/agents/testcoverage/
│   │   ├── application/
│   │   ├── cli/
│   │   ├── domain/
│   │   ├── infrastructure/
│   │   └── presentation/
│   └── test/java/com/aichallenge/agents/testcoverage/
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

## Uso
Desde esta carpeta:

```bash
./run.sh --project-path ../agent-02-postgres-schema-mcp
```

Analizar una clase, paquete, feature o módulo específico:

```bash
./run.sh --project-path ../agent-02-postgres-schema-mcp --target schema
./run.sh --project-path /ruta/a/mi-api --target UserController
./run.sh --project-path /ruta/a/mi-api --max-items 12
./run.sh --project-path /ruta/a/mi-api --target users --ai
```

Ver ayuda:

```bash
./run.sh --help
```

## Modo AI
El modo local usa heurísticas determinísticas y sirve como inventario rápido.

Para que funcione como agente, usar `--ai`. Ese modo envía el análisis estático a Bedrock y genera un prompt de implementación listo para pegarle a otro agente coding.

La AI recibe:
- resumen del inventario local,
- controllers, services y repositories relevantes,
- recomendaciones candidatas del analizador,
- snippets acotados de las clases más prioritarias.

Los snippets se limitan por cantidad y tamaño para no enviar el proyecto completo ni saturar el contexto. Sirven para que el modelo proponga tests con métodos, branches, exceptions, queries y asserts más concretos.

El output AI queda orientado a implementación:
- ruta del proyecto y target,
- alcance inicial chico,
- tests concretos a implementar,
- reglas para no inventar paths, mensajes ni fixtures,
- comandos de verificación sugeridos,
- fuera de alcance.

Variables soportadas:

```bash
export AWS_REGION=us-east-1
export BEDROCK_MODEL_ID=openai.gpt-oss-20b-1:0
```

Luego:

```bash
./run.sh --project-path /ruta/a/mi-api --target tenant --ai
```

Si no se usa `--ai`, la CLI no invoca ningún modelo y funciona solo como analizador local.

## Costo aproximado del modo AI
El costo depende de la región, tier de Bedrock, modelo configurado, tamaño del target, snippets enviados y longitud de la respuesta.

Con `openai.gpt-oss-20b-1:0`, el precio público de Bedrock ronda:
- input: USD 0.07-0.09 por 1M tokens,
- output: USD 0.30-0.39 por 1M tokens.

Una ejecución típica de este agente suele enviar aproximadamente:
- input: 5k-12k tokens,
- output: 700-2k tokens.

Ejemplo estimado:

```text
input:  10,000 tokens * USD 0.09 / 1,000,000  = USD 0.0009
output:  1,500 tokens * USD 0.39 / 1,000,000  = USD 0.000585

total aproximado: USD 0.0015 por ejecución
```

Orden de magnitud:
- 1 ejecución: USD 0.001-0.003,
- 100 ejecuciones: USD 0.10-0.30,
- 1000 ejecuciones: USD 1-3.

El scanner local no tiene costo. Solo se cobra una llamada a Bedrock cuando se usa `--ai`.

Arquitectura:
- `application` orquesta el análisis y la estrategia.
- `domain.port.CoverageStrategyAdvisor` define la frontera AI.
- `infrastructure.ai.BedrockCoverageStrategyAdvisor` implementa la estrategia con Bedrock.
- `presentation.LocalCoverageStrategyAdvisor` mantiene el fallback local.

La salida de Bedrock se sanitiza antes de imprimirse para eliminar bloques internos como `<reasoning>...</reasoning>`.

Si Bedrock devuelve solo razonamiento interno y ninguna respuesta final visible, la CLI no falla: imprime un aviso y usa el plan local como fallback.

## Qué analiza el MVP
- Componentes de producción en `src/main/java`.
- Tests existentes en `src/test/java`.
- Anotaciones y nombres típicos de Spring Boot:
  - `@RestController`, `@Controller`
  - `@Service`
  - `@Repository`
  - DTOs `Request`, `Response`, `Dto`, `DTO`
- Endpoints declarados con `@GetMapping`, `@PostMapping`, `@PutMapping`, `@DeleteMapping`, `@PatchMapping` y `@RequestMapping`.
- Señales de lógica de negocio, exceptions, validaciones y queries custom.
- Tests con `JUnit 5`, `Mockito`, `MockMvc`, `@WebMvcTest`, `@SpringBootTest`, `@DataJpaTest` y `Testcontainers`.
- Generación AI opcional de un prompt de implementación agrupado y menos repetitivo.

## Salida actual
En modo local, la CLI imprime:
- resumen de componentes y tests detectados,
- riesgos principales,
- unit tests prioritarios,
- integración prioritaria,
- carga mínima sugerida,
- notas para evitar recomendaciones genéricas.

En modo `--ai`, imprime un texto pensado como handoff para un agente implementador.

Por defecto muestra hasta 8 recomendaciones por sección para mantener el plan accionable. Se puede ampliar con `--max-items`.

## Estado actual
MVP funcional implementado como CLI Java/Maven.

Incluye:
- análisis estático simple de proyectos Spring Boot locales,
- modo AI opcional vía Bedrock,
- recomendaciones priorizadas por riesgo y costo/beneficio,
- separación pragmática por capas,
- tests unitarios del scanner, analyzer, planner y renderer.

## Roadmap
1. Mejorar parsing de rutas combinando `@RequestMapping` de clase + método.
2. Generar skeletons opcionales de tests.
3. Agregar plantillas para `MockMvc`, `@DataJpaTest` y `Testcontainers`.
4. Exportar el plan a Markdown.
5. Leer reportes de cobertura existentes como señal secundaria, no como criterio único.
