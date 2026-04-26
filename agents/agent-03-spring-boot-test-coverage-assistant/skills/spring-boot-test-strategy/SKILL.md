---
name: spring-boot-test-strategy
description: Use this skill when you need to analyze a Spring Boot feature, module, controller, or endpoint and decide which unit tests, integration tests, and basic load tests matter most first. It is for reducing testing friction, prioritizing meaningful coverage, and avoiding generic “test everything” advice in Spring Boot APIs.
compatibility: Diseñada para agentes que analizan proyectos Spring Boot locales y responden en texto accionable.
metadata:
  author: julio-perez
  version: "0.1"
---

# Spring Boot Test Strategy

## Propósito
Esta skill ayuda a decidir la estrategia de testing más útil para una feature Spring Boot.

## Checklist
- [ ] Identificar la feature o módulo bajo análisis.
- [ ] Detectar controllers, services, repositories y DTOs relevantes.
- [ ] Revisar tests existentes y huecos claros.
- [ ] Priorizar riesgos de negocio, validación, persistencia y contrato HTTP.
- [ ] Proponer unit, integration y load tests mínimos.

## Cuándo usarla
- Cuando el proyecto tiene cobertura parcial y no está claro qué falta.
- Cuando existe una feature nueva y se quiere definir el set mínimo de pruebas importante.
- Cuando el usuario quiere bajar fricción y evitar tests irrelevantes.

## Qué mirar
1. Controllers.
2. Services.
3. Repositories.
4. DTOs y validaciones.
5. Exceptions y manejo de errores.
6. Tests ya existentes.

## Qué producir
- Resumen del área analizada.
- Riesgos principales.
- Tests unitarios prioritarios.
- Tests de integración prioritarios.
- Tests de carga mínimos sugeridos.

## Reglas
- No recomendar pruebas duplicadas.
- Priorizar casos de negocio, errores esperados y flujos críticos.
- Si una capa no contiene lógica real, no forzar pruebas unitarias artificiales.
- Si un endpoint es crítico, sugerir al menos un smoke/load test básico.

## Gotchas
- No sugerir `@SpringBootTest` solo porque “cubre más”.
- No tratar cobertura porcentual como objetivo principal.
- No pedir tests para wiring o delegación trivial si no agregan valor.
- Si una validación vive en anotaciones del DTO y no en la lógica de negocio, distinguir entre test unitario y test HTTP.

## Heurística sugerida
- `Service` con lógica: normalmente requiere unit tests.
- `Controller` con contrato HTTP relevante: normalmente requiere integración HTTP.
- Persistencia relevante: evaluar `@DataJpaTest` o integración con DB.
- Endpoint crítico o de alto uso: sugerir carga mínima.

## Formato de salida recomendado
```text
Feature: <nombre>

Riesgos principales
- ...

Unit tests prioritarios
- ...

Integración prioritaria
- ...

Carga mínima sugerida
- ...
```
