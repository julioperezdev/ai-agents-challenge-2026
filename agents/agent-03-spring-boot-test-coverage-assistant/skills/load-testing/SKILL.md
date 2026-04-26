---
name: load-testing
description: Use this skill when a Spring Boot API needs minimal, practical load-testing recommendations for critical endpoints. It focuses on smoke load, simple concurrency checks, and basic response-time expectations without turning the project into a heavy performance-testing program.
compatibility: Diseñada para análisis y recomendación; no requiere ejecutar carga real en el MVP.
metadata:
  author: julio-perez
  version: "0.1"
---

# Load Testing

## Propósito
Proponer pruebas mínimas de carga que aporten tranquilidad sin meter demasiada complejidad.

## Checklist
- [ ] Confirmar si el endpoint es realmente crítico o de alto uso.
- [ ] Proponer un escenario de carga simple.
- [ ] Sugerir concurrencia inicial razonable.
- [ ] Sugerir una métrica básica como p95 o tasa de error.
- [ ] Mantener el alcance en smoke/load test mínimo.

## Cuándo usarla
- Cuando un endpoint es crítico para negocio.
- Cuando hay endpoints con alto tráfico esperado.
- Cuando se quiere al menos un smoke test de performance.

## Input esperado
- Endpoint o feature.
- Contexto de criticidad, si existe.

## Output esperado
- Sugerencias de smoke/load tests básicos.
- Recomendaciones de concurrencia mínima.
- Métricas sugeridas como p95 o tasa de error.

## Reglas
- No proponer suites complejas de performance si el sistema todavía no lo necesita.
- Empezar por smoke load tests simples.
- Priorizar endpoints críticos o de alto volumen.
- Expresar el output como recomendaciones accionables.

## Gotchas
- No recomendar carga temprana para endpoints internos sin criticidad real.
- No fijar thresholds agresivos sin baseline mínima.
- Un smoke test chico ya puede ser suficiente para el MVP.

## Edge cases
- Sistemas internos con poco tráfico: puede no justificar carga temprana.
- Endpoints costosos con acceso a terceros: sugerir pruebas controladas.
- Features nuevas sin baseline: proponer thresholds iniciales conservadores.

## Ejemplo
```text
Carga mínima sugerida
- Ejecutar smoke test con 20 requests concurrentes sobre POST /users.
- Verificar p95 menor a 300 ms en GET /users/{id}.
- Confirmar tasa de error 0% en el escenario base.
```
