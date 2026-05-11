# Agent Lifecycle

## 1. Descubrimiento
- Definir el problema en una frase.
- Identificar quien usara el agente y que output espera.
- Separar MVP de futuras extensiones.
- Decidir si debe funcionar sin AI.

## 2. Specification
Todo agente debe tener `Specification.md` con:
- problema que resuelve,
- objetivo del MVP,
- inputs y outputs,
- flujo de ejecucion,
- arquitectura esperada,
- componentes principales,
- criterios de aceptacion,
- decisiones tecnicas,
- costos si usa AI,
- demo esperada.

## 3. Implementacion
- Crear estructura base.
- Implementar modo local primero.
- Agregar AI como enriquecimiento opcional.
- Mantener adapters externos en `infrastructure`.
- Agregar tests proporcionales al riesgo.

## 4. Verificacion
- Ejecutar tests.
- Ejecutar comando principal.
- Generar demo versionable.
- Revisar que README y Specification coincidan con el codigo real.

## 5. Publicacion
- Actualizar README general del repo.
- Adjuntar demo o salida ejemplo.
- Documentar costos y variables de entorno.
- Dejar roadmap con mejoras reales, no pendientes vagas.
