# Specification: agent-03-spring-boot-test-coverage-assistant

## Problema que resuelve
Ayuda a reducir la fricción de testing en APIs Spring Boot identificando qué pruebas unitarias, de integración y de carga realmente importan para una feature o endpoint, priorizando cobertura útil por encima de volumen de tests.

## Objetivo del MVP
Construir un agente especializado en Spring Boot que:
- analice código fuente y tests existentes,
- detecte gaps relevantes de cobertura,
- proponga una estrategia mínima de testing,
- sugiera casos unitarios, de integración y de carga,
- entregue una salida clara y accionable para developers backend.

## Alcance incluido
- Análisis estático de proyectos Spring Boot locales.
- Detección de componentes como `Controller`, `Service`, `Repository`, DTOs, mappers y exceptions.
- Revisión de tests existentes con `JUnit 5`, `Mockito`, `MockMvc`, `SpringBootTest`, `DataJpaTest` o similares.
- Generación de un plan priorizado de pruebas:
  - unitarias
  - integración
  - carga básica o smoke load
- Recomendaciones específicas por endpoint, caso de uso o módulo.
- Documentación y skills especializadas para testing en Spring Boot.

## Alcance excluido
- Generación completa y automática de todos los tests.
- Ejecución real de tests de carga en el MVP.
- Integración con CI/CD.
- Cobertura para frameworks backend no basados en Spring Boot.
- Mutación de código productivo sin confirmación explícita.

## Inputs
- Ruta de un proyecto Spring Boot local.
- Clase, paquete, feature o módulo a analizar.
- Código fuente de producción.
- Tests ya existentes.
- Opcionalmente `pom.xml` o `build.gradle` para inferir stack de testing.

## Outputs
- Diagnóstico de cobertura relevante.
- Lista priorizada de tests faltantes.
- Recomendaciones de tipo de test por caso:
  - unit test
  - integration test
  - load test
- Sugerencias concretas de herramientas o anotaciones Spring.
- Opcionalmente skeletons o ideas de estructura de test en futuras versiones.

## Flujo del usuario
1. El usuario apunta el agente a un proyecto o módulo Spring Boot.
2. El agente identifica endpoints, servicios, persistencia y tests ya presentes.
3. El agente detecta riesgos y cobertura faltante.
4. El agente devuelve un plan accionable de pruebas.
5. El usuario usa ese plan para implementar o completar el testing.

## Criterios de aceptación
- El agente reconoce estructura típica de proyectos Spring Boot.
- Diferencia correctamente cuándo conviene:
  - `JUnit` + `Mockito`
  - `@WebMvcTest`
  - `@SpringBootTest`
  - `@DataJpaTest`
  - `Testcontainers`
- La salida prioriza pruebas importantes y evita recomendaciones genéricas.
- El resultado es útil para un developer backend sin necesidad de reinterpretar demasiado.
- El agente puede distinguir entre:
  - lógica de negocio,
  - validaciones,
  - integración HTTP,
  - persistencia,
  - carga básica.

## Decisiones técnicas
- El agente se especializa solo en Spring Boot para maximizar precisión.
- La salida debe priorizar claridad, riesgo y costo/beneficio.
- El enfoque inicial será análisis y planificación, no generación masiva de código.
- La arquitectura debe seguir estilo enterprise, pero sin interfaces o capas innecesarias.
- Las skills deben seguir el formato Agent Skills (`SKILL.md` con YAML frontmatter) para que sean reutilizables y progresivas.

## Futuras extensiones
- Generación de test skeletons.
- Generación de casos `MockMvc` y `SpringBootTest`.
- Plantillas para `Testcontainers`.
- Exportación de checklist de testing a Markdown.
- Integración con cobertura existente (`JaCoCo`).
- Detección automática de gaps entre OpenAPI y tests implementados.
- Generación de escenarios básicos de carga para `k6`.
