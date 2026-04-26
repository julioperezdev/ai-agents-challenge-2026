# AGENT.md

## Design Preference

Todos los agentes de este repositorio deben priorizar un estilo de proyecto `enterprise style`.

Esto implica, cuando aplique:
- arquitectura clara por capas
- separación explícita entre `application`, `domain`, `infrastructure`, `presentation` o equivalente
- configuración externa por variables de entorno y archivos de configuración
- código fácil de mantener, testear y extender
- nombres consistentes y orientados al dominio
- documentación suficiente para que otro developer entienda cómo correr y evolucionar el proyecto

## Enterprise Style, But Pragmatic

La preferencia por `enterprise style` no significa agregar complejidad innecesaria.

Reglas:
- evitar sobreingeniería en MVPs
- preferir una estructura limpia antes que una estructura maximalista
- usar abstracciones cuando aporten mantenibilidad real
- si una solución híbrida es mejor que una migración total, preferir la híbrida

## Persistence Guidance

Cuando el proyecto tenga modelo de negocio estable, se prefiere usar:
- `JPA` / `Spring Data JPA` para entidades y repositorios de dominio

Cuando el proyecto requiera consultas dinámicas, introspección de metadata o SQL altamente variable, se permite y se recomienda mantener:
- `JDBC`
- `JdbcTemplate`
- SQL explícito

En esos casos, la preferencia es un enfoque híbrido:
- `JPA` para entidades del dominio
- `JDBC` para metadata, reporting, ejecución dinámica o casos especiales

## Layering Rule for Persistence Models

Para este repositorio, una `Entity` JPA representa modelado de base de datos.
Por lo tanto:
- `Entity`
- `JpaRepository`
- mappers desde/hacia base de datos
- adapters que dependen directamente de JPA

deben vivir en `infrastructure`.

Solo debe existir un modelo separado en `application` o `domain` cuando haya un caso de uso que realmente necesite desacoplarse de la persistencia.

Si el flujo es directo desde una entrada de infraestructura hacia repositorios/adapters de infraestructura, sin necesidad de un modelo intermedio, se prefiere mantener todo dentro de `infrastructure`.

## DTOs, Models and Ports

`domain` puede contener piezas como:
- modelos de dominio
- DTOs cuando representan conceptos del dominio
- exceptions
- ports / interfaces

`application` también puede contener DTOs o modelos cuando:
- no forman parte del dominio
- representan contratos de entrada o salida de un caso de uso
- sirven para desacoplar `application` de detalles de infraestructura

Regla práctica:
- si el tipo expresa una verdad del negocio, tender a `domain`
- si el tipo expresa un contrato de caso de uso, tender a `application`
- si el tipo expresa persistencia, transporte técnico o detalle de adapter, dejarlo en `infrastructure`

## Ports Between Application and Infrastructure

Cuando una clase de `application` necesite colaborar con repositorios, readers o adapters, debe depender de una interfaz definida como `port` en `domain`.

Reglas:
- `application` no debe depender directamente de implementaciones concretas de `infrastructure`
- las interfaces usadas por `application` deben vivir en `domain`
- las implementaciones concretas deben vivir en `infrastructure`
- esto aplica tanto a repositorios de persistencia como a readers, gateways o cualquier adapter consumido desde `application`
- no crear `ports` o interfaces si no existe una frontera real que valga la pena abstraer

Ejemplo esperado:
- `domain.port.SchemaRepository`
- `infrastructure.postgres.PostgresSchemaRepository implements SchemaRepository`

La misma lógica aplica a cualquier otro repository o adapter utilizado por servicios de `application`.

## Avoid Unnecessary Interfaces

En este repositorio no se deben crear interfaces "por arquitectura" si no aportan valor real.

Reglas:
- evitar interfaces de servicio si solo existe una implementación y no hay una frontera técnica o de negocio que justificar
- preferir clases concretas en `application` para servicios y casos de uso
- usar interfaces principalmente en repositorios, gateways, readers o adapters que separan `application` de `infrastructure`
- si una abstracción no mejora testabilidad, reemplazo, claridad o desacoplamiento real, no agregarla

Regla práctica:
- `repository port`: sí, suele tener sentido
- `service interface`: en general no, salvo necesidad concreta

## Application Packaging

Dentro de `application`, evitar carpetas genéricas como:
- `model`
- `dto`
- `util`

Si un caso de uso necesita varias clases relacionadas, se prefiere agruparlas juntas por feature o caso de uso.

Ejemplo:
- `application/schema/documentation/...`
- `application/reporting/...`

## Naming Conventions

Los nombres de clases e interfaces deben ser semánticos y describir su responsabilidad real.

Reglas:
- evitar sufijos genéricos como `...Port` en interfaces de dominio
- evitar sufijos genéricos como `...Model` en clases que pueden nombrarse por su significado real
- preferir nombres que expresen el concepto o la capacidad concreta

Ejemplos preferidos:
- `SchemaRepository` en lugar de `SchemaRepositoryPort`
- `DailySummary` en lugar de `DailySummaryModel`

Sufijos que sí están permitidos o recomendados cuando agregan claridad:
- `...Exception`
- `...Entity`
- `...BaseJpaRepository` para interfaces base compartidas de Spring Data JPA
- `...JpaRepository` para implementaciones o adapters concretos asociados a JPA cuando ese nombre realmente describe su rol

Regla práctica:
- si el sufijo no agrega información útil, no usarlo
- si el sufijo aclara la naturaleza técnica o la responsabilidad, sí usarlo

## Mapping Guidance

Por ahora, no se deben crear clases `Mapper` dedicadas si no aportan una mejora real en claridad o mantenibilidad.

Reglas:
- si el mapping entre `Entity` y modelo es simple, mantenerlo dentro de la clase concreta de infraestructura, por ejemplo en `...JpaRepository`
- resolver esos mapeos con métodos privados semánticos
- solo extraer un `Mapper` dedicado cuando el mapping crezca lo suficiente como para justificar una clase aparte

Regla práctica:
- mapping simple: método privado dentro de la implementación concreta
- mapping complejo o muy reutilizado: evaluar extraer `Mapper`

## Response DTO Guidance

Cuando exista un DTO `Response` para exponer datos fuera del sistema, se permite que el objeto interno haga el mapeo hacia ese DTO directamente.

Reglas:
- si el mapping es simple y está claramente asociado al objeto, se puede implementar un método interno para construir el `Response`
- esto aplica especialmente a respuestas de `controller`, tools, adapters de salida o outputs del sistema
- no hace falta crear un mapper adicional solo para transformar un objeto interno en un `Response` simple

Ejemplos válidos:
- `toResponse()`

Regla práctica:
- output simple hacia afuera: el propio objeto puede mapearse al `Response`
- output complejo o compartido por muchos flujos: evaluar extraer el mapping

## Goal

El objetivo es que cada agente se vea y se sienta como un proyecto profesional, mantenible y cercano a un entorno real de backend, sin perder foco en entregar MVPs funcionales.
