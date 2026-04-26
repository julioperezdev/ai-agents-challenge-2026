---
name: integration-testing
description: Use this skill when you need to decide which Spring Boot integration tests are worth writing for controllers, HTTP contracts, persistence behavior, or database-specific logic. It helps choose between MockMvc, WebMvcTest, SpringBootTest, DataJpaTest, and Testcontainers without overtesting.
compatibility: Requiere contexto de proyectos Spring Boot y nociones de testing HTTP y persistencia.
metadata:
  author: julio-perez
  version: "0.1"
---

# Integration Testing

## Propósito
Ayudar a decidir qué integración conviene testear y con qué nivel de profundidad.

## Checklist
- [ ] Identificar si el riesgo principal es HTTP, contexto Spring o persistencia.
- [ ] Elegir el slice más chico que valide ese riesgo.
- [ ] Confirmar si hace falta base real o emulada.
- [ ] Priorizar happy path y errores esperados.
- [ ] Evitar usar el contexto completo sin necesidad.

## Cuándo usarla
- Cuando un endpoint tiene contrato HTTP importante.
- Cuando hay serialización, validación o manejo de errores en controllers.
- Cuando la persistencia o queries deben validarse con DB real o casi real.

## Qué evaluar
- `@WebMvcTest` para contrato HTTP y controller slice.
- `@SpringBootTest` para flujos integrales relevantes.
- `@DataJpaTest` para persistencia aislada.
- `Testcontainers` cuando la BD real importa de verdad.

## Reglas
- No usar `@SpringBootTest` para todo.
- Elegir el slice más pequeño que valide el riesgo importante.
- Priorizar happy path, errores esperados y contratos críticos.
- Si el repositorio usa SQL específico, considerar integración más realista.

## Gotchas
- `@WebMvcTest` suele ser mejor default para contrato HTTP que `@SpringBootTest`.
- Si el comportamiento depende de PostgreSQL, H2 puede no ser suficiente.
- Un repository simple heredado de JPA no siempre necesita test propio en el MVP.

## Edge cases
- Endpoints simples sin lógica: puede bastar un test HTTP mínimo.
- Persistencia con behavior dependiente de PostgreSQL: sugerir `Testcontainers`.
- Sistemas muy chicos: no sobrecargar con demasiados niveles de integración.

## Ejemplo
```text
Integración prioritaria
- POST /users responde 201 con payload válido.
- POST /users responde 400 con payload inválido.
- GET /users/{id} responde 404 si no existe.
- Repository de órdenes debe validarse con PostgreSQL real usando Testcontainers.
```
