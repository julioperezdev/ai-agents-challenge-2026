# Specification: agent-02-postgres-schema-mcp

## Problema que resuelve
Permite que un agente entienda un schema PostgreSQL demo y lo consulte con lenguaje natural de forma segura, sin habilitar SQL destructivo ni depender de una base productiva.

## Objetivo del MVP
Construir un MCP Server en Java que:
- inspeccione el schema `public`,
- genere documentación Markdown del schema,
- traduzca preguntas naturales a SQL de solo lectura,
- ejecute la consulta en PostgreSQL,
- devuelva resultados tabulares fáciles de leer.

## Alcance incluido
- Spring Boot + MCP Server por STDIO.
- PostgreSQL local con Docker Compose.
- Flyway con schema y seed data demo.
- Tools MCP: `get_public_schema`, `generate_schema_documentation`, `ask_database`, `explain_schema`, `health_check`.
- Modo `mock` para NL→SQL sin credenciales AWS.
- Integración opcional con AWS Bedrock para generación SQL.
- Validación de seguridad SQL.
- Documentación y ejemplos de uso.

## Alcance excluido
- UI web.
- Escritura en base de datos.
- Multi-tenant.
- Soporte multi-schema.
- Integración con bases productivas.
- Secrets Manager.

## Inputs
- Variables PostgreSQL: `POSTGRES_DB`, `POSTGRES_USER`, `POSTGRES_PASSWORD`, `POSTGRES_PORT`.
- Variables AI: `APP_AI_PROVIDER`, `AWS_REGION`, `BEDROCK_MODEL_ID`, `APP_MAX_QUERY_LIMIT`.
- Preguntas naturales del usuario.
- Parámetros opcionales por tool como `limit`, `showSql` u `outputPath` reservado.

## Outputs
- Metadata JSON del schema `public`.
- Archivo `docs/docs/DATABASE_SCHEMA_ddMMyy_hhmm.md`.
- Explicación textual del schema.
- Resultados de consultas en tablas ASCII.
- Estado básico del servidor y de la conexión.

## Flujo del usuario
1. Levantar PostgreSQL con Docker Compose.
2. Ejecutar la app Spring Boot.
3. Conectar el MCP desde un cliente compatible.
4. Usar tools para inspeccionar schema, generar documentación o consultar datos.
5. Recibir respuestas estructuradas o texto tabular.

## Criterios de aceptación
- `docker compose up -d` levanta PostgreSQL en `55432`.
- Flyway crea tablas y datos demo automáticamente.
- El server inicia y expone las tools requeridas.
- `get_public_schema` devuelve metadata real.
- `generate_schema_documentation` crea siempre `docs/docs/DATABASE_SCHEMA_ddMMyy_hhmm.md`.
- `ask_database` genera SQL seguro, lo valida y ejecuta.
- Las consultas destructivas se rechazan.
- Los resultados se muestran en formato tabla o `No rows found.`.

## Decisiones técnicas
- Java 21 + Spring Boot por cercanía con el stack backend del usuario.
- Spring AI MCP Server por simplicidad para exponer tools MCP.
- Transporte STDIO por compatibilidad simple con clientes MCP locales.
- PostgreSQL + Flyway para un demo repetible.
- `JdbcTemplate` en vez de JPA para mantener la solución liviana y explícita.
- `APP_AI_PROVIDER=mock` por defecto para que el demo funcione sin AWS.
- Bedrock opcional con modelo configurable por variable de entorno.

## Futuras extensiones
- Multi-schema.
- Exportar resultados a CSV.
- Diagramas Mermaid ERD.
- Soporte MySQL/Oracle.
- Explain plan.
- Testcontainers.
- Usuario PostgreSQL read-only estricto.
