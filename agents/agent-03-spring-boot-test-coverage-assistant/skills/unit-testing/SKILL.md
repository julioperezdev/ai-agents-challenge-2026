---
name: unit-testing
description: Use this skill when Spring Boot code needs meaningful unit test recommendations for services, validators, mappers, business rules, branching logic, or exception handling. It helps identify high-value unit tests without suggesting trivial tests or loading the full Spring context.
compatibility: Diseñada para análisis de código Java y proyectos Spring Boot.
metadata:
  author: julio-perez
  version: "0.1"
---

# Unit Testing

## Propósito
Ayudar a identificar pruebas unitarias que realmente agregan valor.

## Checklist
- [ ] Confirmar si la clase tiene lógica real o solo delega.
- [ ] Detectar branches, reglas y validaciones importantes.
- [ ] Revisar exceptions esperadas.
- [ ] Revisar tests existentes para evitar duplicación.
- [ ] Proponer solo casos con valor claro.

## Cuándo usarla
- Cuando un `Service` tiene lógica condicional.
- Cuando hay validaciones o branches importantes.
- Cuando existen mapeos con riesgo de regresión.
- Cuando hay manejo explícito de exceptions.

## Input esperado
- Clase o paquete a analizar.
- Tests unitarios existentes, si los hay.

## Output esperado
- Lista priorizada de unit tests faltantes.
- Justificación breve por cada caso.

## Reglas
- No pedir unit tests para getters/setters triviales.
- No testear wiring de Spring como si fuera lógica de negocio.
- Priorizar reglas, branches, errores y contratos de método.
- Si un mapper es simple y privado, cubrirlo indirectamente desde el comportamiento público.

## Gotchas
- Un `Service` que solo orquesta una llamada puede no justificar un unit test propio.
- Un mapper muy simple no necesita test aislado si ya queda cubierto desde el método público.
- No usar mocks de más si el caso realmente debería validarse por integración.

## Edge cases
- Services que solo delegan: puede no justificar unit tests propios.
- Validaciones distribuidas entre DTO y service: distinguir qué validar en cada capa.
- Código con demasiadas dependencias: sugerir primero reducir complejidad si el test sería muy costoso.

## Ejemplo
```text
Unit tests prioritarios
- Debe rechazar email duplicado.
- Debe lanzar excepción si el usuario no existe.
- Debe persistir correctamente cuando el payload es válido.
```
