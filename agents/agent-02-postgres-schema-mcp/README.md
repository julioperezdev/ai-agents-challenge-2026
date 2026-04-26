# agent-02-postgres-schema-mcp

MCP Server en Java para explorar un schema PostgreSQL demo, generar documentación y responder preguntas en lenguaje natural con SQL seguro de solo lectura.

## Qué resuelve
Este agente sirve para aprender MCP con un caso realista: un cliente MCP puede pedir metadata del schema, generar un archivo `DATABASE_SCHEMA_ddMMyy_hhmm.md`, explicar la base en lenguaje humano o convertir preguntas naturales a SQL seguro y ejecutarlo contra PostgreSQL.

## Stack
- Java 21
- Spring Boot
- Spring AI MCP Server
- Spring Data JPA
- PostgreSQL 16
- Flyway
- AWS Bedrock opcional

## Persistencia
El proyecto usa un enfoque híbrido:
- `JPA` en `infrastructure` para el modelado de base de datos (`customers`, `products`, `orders`, `order_items`, `payments`)
- `JdbcTemplate` para introspección del schema `public` y para ejecutar SQL dinámico generado desde lenguaje natural

Cuando un caso de uso necesita subir información hacia `application`, se usa un modelo desacoplado de la persistencia y el mapping queda en `infrastructure`.

## Modo AI
El proyecto soporta dos modos:
- `APP_AI_PROVIDER=mock`: default. No necesita credenciales AWS y resuelve preguntas demo predefinidas.
- `APP_AI_PROVIDER=bedrock`: usa Bedrock para generar SQL desde lenguaje natural.

## Setup local

```bash
cd /Users/julio/developer/realProjects/ai-agents-challenge-2026/agents/agent-02-postgres-schema-mcp
cp .env.example .env
```

Si quieres usar Java 21 explícitamente:

```bash
export JAVA_HOME=$(/usr/libexec/java_home -v 21)
export PATH="$JAVA_HOME/bin:$PATH"
```

## Quick Start con Inspector

Si quieres validar el MCP lo más rápido posible, usa este flujo.

### 1. Preparar el entorno

```bash
cd /Users/julio/developer/realProjects/ai-agents-challenge-2026/agents/agent-02-postgres-schema-mcp
cp .env.example .env
```

### 2. Abrir MCP Inspector

```bash
./inspect.sh
```

Ese comando:
- levanta PostgreSQL si no está arriba
- usa `APP_AI_PROVIDER=mock` por defecto
- abre MCP Inspector
- conecta Inspector con este MCP por `stdio`

### 3. Abrir la URL que imprime la terminal

Inspector imprime algo como:

```text
MCP Inspector is up and running at:
http://localhost:6274/?MCP_PROXY_AUTH_TOKEN=...
```

Usa esa URL exacta en el navegador.

### 4. Probar las tools

Orden recomendado:
1. `health_check`
2. `get_public_schema`
3. `explain_schema`
4. `ask_database`
5. `generate_schema_documentation`

Payload recomendado para `ask_database`:

```json
{
  "question": "Muéstrame las órdenes pagadas con el nombre del cliente",
  "limit": 20,
  "showSql": true
}
```

Payload recomendado para `generate_schema_documentation`:

```json
{
  "outputPath": "se ignora",
  "includeQueryExamples": true
}
```

Ese tool siempre genera un archivo nuevo con este patrón:

```text
docs/docs/DATABASE_SCHEMA_ddMMyy_hhmm.md
```

### 5. Cerrar todo al terminar

- `Ctrl+C` en la terminal donde corriste `./inspect.sh`
- si quieres bajar PostgreSQL:

```bash
docker compose down
```

## Ejecutar la aplicación

```bash
mvn spring-boot:run
```

La app:
- se conecta a PostgreSQL local,
- ejecuta Flyway automáticamente,
- arranca el MCP Server por STDIO,
- deja disponibles las tools MCP.

Importante:
- este MCP usa `stdio`
- no se interactúa escribiendo prompts en la misma terminal donde corre `run.sh`
- para probarlo manualmente conviene usar MCP Inspector o un cliente MCP compatible

## Empaquetar

```bash
mvn package
java -jar target/agent-02-postgres-schema-mcp-0.1.0.jar
```

## Variables principales

```env
POSTGRES_DB=mcp_demo
POSTGRES_USER=mcp_user
POSTGRES_PASSWORD=mcp_password
POSTGRES_PORT=55432

APP_AI_PROVIDER=mock
AWS_REGION=us-east-1
BEDROCK_MODEL_ID=anthropic.claude-3-5-sonnet-20240620-v1:0
APP_MAX_QUERY_LIMIT=50
```

## Tools MCP

### `get_public_schema`
Devuelve metadata del schema `public`.

Parámetros opcionales:
- `includeIndexes`
- `includeConstraints`
- `includeSampleRows`

### `generate_schema_documentation`
Genera siempre un archivo nuevo con este patrón:

```text
docs/docs/DATABASE_SCHEMA_ddMMyy_hhmm.md
```

Parámetros opcionales:
- `outputPath`
  Actualmente reservado e ignorado para mantener una convención fija.
- `includeQueryExamples`

### `ask_database`
Convierte una pregunta natural a SQL seguro, la ejecuta y devuelve resultados.

Parámetros:
- `question`
- `limit`
- `showSql`

### `explain_schema`
Explica en lenguaje humano qué representa la base.

Parámetros:
- `detailLevel`: `short`, `medium`, `detailed`

### `health_check`
Verifica conexión a PostgreSQL y estado básico del MCP.

## Probar con MCP Inspector

La forma más simple de probar el servidor es abrir Inspector. El proyecto incluye:
- un helper compartido en [shared/tools/run-mcp-inspector.sh](/Users/julio/developer/realProjects/ai-agents-challenge-2026/shared/tools/run-mcp-inspector.sh)
- un wrapper local en [inspect.sh](/Users/julio/developer/realProjects/ai-agents-challenge-2026/agents/agent-02-postgres-schema-mcp/inspect.sh)

Comando recomendado:

```bash
cd /Users/julio/developer/realProjects/ai-agents-challenge-2026/agents/agent-02-postgres-schema-mcp
./inspect.sh
```

Eso hace esto:
- levanta PostgreSQL si no está arriba
- usa `APP_AI_PROVIDER=mock` por defecto
- abre MCP Inspector y muestra en terminal la URL exacta con token
- registra este agente usando su `run.sh`

También puedes usar el helper compartido directamente para futuros agentes MCP:

```bash
cd /Users/julio/developer/realProjects/ai-agents-challenge-2026
./shared/tools/run-mcp-inspector.sh agents/agent-02-postgres-schema-mcp
```

La guía recomendada de uso está en la sección `Quick Start con Inspector`.

## Preguntas demo
En modo `mock`, estas preguntas deberían funcionar:

- `Muéstrame los 10 clientes más recientes`
- `Muéstrame las órdenes pagadas con el nombre del cliente`
- `¿Cuáles son los productos más vendidos?`
- `¿Cuánto dinero se ha aprobado en pagos por cliente?`
- `Muéstrame las órdenes canceladas`

## Ejemplo de output

```text
Question:
Muéstrame las órdenes pagadas con el nombre del cliente

Generated SQL:
SELECT o.id, c.full_name, o.order_date, o.status, o.total_amount
FROM orders o
JOIN customers c ON c.id = o.customer_id
WHERE o.status = 'PAID'
ORDER BY o.order_date DESC
LIMIT 20;

Results:
+----+---------------+------------+--------+--------------+
| id | full_name     | order_date | status | total_amount |
+----+---------------+------------+--------+--------------+
| 12 | Mariana Diaz  | 2026-03-19 | PAID   | 200.00       |
| 10 | Carlos Rivas  | 2026-03-15 | PAID   | 505.00       |
+----+---------------+------------+--------+--------------+
```

## Registrar en un cliente MCP
Ejemplo conceptual para un cliente compatible con STDIO:

```json
{
  "mcpServers": {
    "postgres-schema-mcp": {
      "command": "java",
      "args": [
        "-jar",
        "/Users/julio/developer/realProjects/ai-agents-challenge-2026/agents/agent-02-postgres-schema-mcp/target/agent-02-postgres-schema-mcp-0.1.0.jar"
      ],
      "env": {
        "POSTGRES_PORT": "55432",
        "APP_AI_PROVIDER": "mock"
      }
    }
  }
}
```

## Limitaciones actuales
- El modo `mock` entiende solo preguntas demo.
- El SQL validator es deliberadamente simple y conservador.
- No hay soporte para múltiples schemas o múltiples bases.
- No existe escritura ni ejecución arbitraria de SQL.

## Roadmap
- Bedrock como flujo principal para NL→SQL.
- Exportar resultados a CSV.
- Mermaid ERD.
- Explain plan.
- Testcontainers.
