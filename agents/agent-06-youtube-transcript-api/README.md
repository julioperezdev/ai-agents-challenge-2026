# agent-06-youtube-transcript-api

Servicio backend para recibir URLs de YouTube, obtener captions publicas con `youtube-transcript-api`, persistirlas en PostgreSQL y reutilizarlas como base de una biblioteca personal de aprendizaje asistida por AI.

## Que resuelve
Muchos videos contienen conocimiento util, pero no siempre hay tiempo para verlos completos. Este agente permite guardar una URL, persistir la transcripcion original y dejar el contenido listo para futuras features de resumen, notas, ideas aplicables y aprendizaje personal en castellano.

## Enfoque
- API publica recomendada en `POST /api/v1/learning/youtube/videos`.
- Endpoint de transcript completo en `POST /api/v1/youtube/transcripts` mantenido como compatibilidad/debug.
- Swagger/OpenAPI disponible en `http://localhost:8080/swagger-ui/index.html`.
- Worker Python interno con FastAPI y `youtube-transcript-api`.
- PostgreSQL con Flyway para videos, transcripciones y segmentos.
- Cache por `videoId` e idioma, con fallback a cualquier transcript guardado del mismo video.
- Si no se envia `preferredLanguages`, usa directamente la transcripcion original disponible en YouTube.
- Si se envia `preferredLanguages` y esos idiomas no estan disponibles, cae a la transcripcion original disponible.
- El idioma se toma desde metadata de YouTube (`language_code`) cuando esta disponible.
- La respuesta incluye un bloque `insight` preparado para usar el transcript original como contexto de LLM y redactar la explicacion final en espanol.
- Analisis de aprendizaje en castellano con Bedrock por defecto y fallback local extractivo.
- Frontend React/TypeScript para ingesta, biblioteca, transcript, analisis y consumo aproximado de proxy.
- Metricas aproximadas de proxy persistidas para proyectar consumo y costo.
- `forceRefresh` para volver a consultar el provider.
- Errores funcionales para URL invalida, transcript no disponible, video no disponible y falla del provider.

## Ejecutar local
Levantar PostgreSQL y el worker Python:

```bash
docker compose up --build -d
```

Ejecutar Spring Boot:

```bash
mvn spring-boot:run
```

Frontend React:

```bash
npm install
npm run dev
```

Abrir:

```text
http://localhost:3006
```

Abrir Swagger:

```text
http://localhost:8080/swagger-ui/index.html
```

Request de prueba recomendado:

```json
{
  "url": "https://www.youtube.com/watch?v=FWEInOtngmM"
}
```

La primera ejecucion deberia responder `fromCache: false`. Al repetir el mismo request, si se encontro transcript, deberia responder `fromCache: true`.

## Endpoints
```http
POST /api/v1/learning/youtube/videos
```

Respuesta exitosa:

```json
{
  "status": "VIDEO_INGESTED",
  "videoId": "FWEInOtngmM",
  "url": "https://www.youtube.com/watch?v=FWEInOtngmM",
  "language": "en",
  "source": "YOUTUBE_CAPTIONS",
  "isGenerated": true,
  "fromCache": false,
  "transcriptStored": true,
  "segmentsStored": 357,
  "readyForAnalysis": true,
  "nextActions": [
    {
      "type": "ANALYZE_FOR_LEARNING",
      "method": "POST",
      "href": "/api/v1/learning/youtube/videos/FWEInOtngmM/analysis"
    }
  ],
  "reason": null
}
```

Endpoint de compatibilidad/debug:

```http
POST /api/v1/youtube/transcripts
```

Ese endpoint devuelve `fullText`, segmentos e `insight` completo. Es util para inspeccionar el transcript, pero la experiencia principal de producto debe usar la ingesta compacta.

Endpoint de analisis de aprendizaje:

```http
POST /api/v1/learning/youtube/videos/{videoId}/analysis
```

Request:

```json
{
  "forceRefresh": false
}
```

Por defecto usa AWS Bedrock como provider LLM real (`provider=BEDROCK`, `model=openai.gpt-oss-20b-1:0`) y persiste el resultado en PostgreSQL. El generador local extractivo (`provider=LOCAL`, `model=extractive-v3`) queda como fallback sin costo si Bedrock falla o si se configura `LEARNING_ANALYSIS_PROVIDER=local`.

Respuesta resumida:

```json
{
  "status": "ANALYSIS_CREATED",
  "videoId": "FWEInOtngmM",
  "analysisLanguage": "es",
  "sourceLanguage": "en",
  "provider": "BEDROCK",
  "model": "openai.gpt-oss-20b-1:0",
  "summary": "Analisis en castellano...",
  "keyIdeas": [],
  "projectApplications": [],
  "importantSegments": [],
  "personalLearningNotes": [],
  "suggestedActions": [],
  "fromCache": false
}
```

## Frontend
La app React/TypeScript permite:

- pegar una URL de YouTube;
- ingerir el video con el endpoint recomendado;
- ver estado, idioma original, cache y cantidad de segmentos;
- abrir el video original;
- consultar el transcript completo desde el endpoint de debug;
- generar o reutilizar un analisis de aprendizaje en castellano;
- ver resumen, ideas clave, aplicaciones a proyectos, timestamps utiles, notas y acciones sugeridas.
- ver la biblioteca de videos ingeridos y elegir acciones por video.
- ver consumo aproximado de proxy por video, costo estimado y proyeccion por 1 GB.

Vite usa proxy local hacia Spring Boot:

```text
/api -> http://localhost:8080
```

## Comportamiento de idiomas
`preferredLanguages` es opcional. El caso de uso recomendado es enviar solo la URL para conservar la transcripcion original y dejar que el LLM interprete ese idioma.

Request comun:

```json
{
  "url": "https://www.youtube.com/watch?v=FWEInOtngmM"
}
```

Si el transcript original esta en ingles:

```json
{
  "language": "en",
  "insight": {
    "contextLanguage": "en",
    "outputLanguage": "es",
    "languageFallbackUsed": false
  }
}
```

Si queres priorizar idiomas especificos, `preferredLanguages` define prioridad, no una restriccion dura.

Ejemplo:

```json
{
  "url": "https://www.youtube.com/watch?v=FWEInOtngmM",
  "preferredLanguages": ["es"],
  "forceRefresh": false
}
```

Si el video no tiene transcript en espanol pero si tiene transcript publico en ingles, el worker devuelve el transcript original en ingles:

```json
{
  "language": "en",
  "insight": {
    "contextLanguage": "en",
    "outputLanguage": "es",
    "languageFallbackUsed": true
  }
}
```

La idea es que el LLM use `fullText` como contexto en el idioma original detectado y produzca la explicacion final en espanol. El fallback local extractivo queda disponible para pruebas sin costo.

## Configuracion Spring Boot
Los valores principales del backend quedan en `src/main/resources/application.yml`.

Por defecto Spring Boot usa:

```yaml
app:
  learning-analysis:
    provider: bedrock
    fallback-to-local: true
    bedrock:
      enabled: true
      region: us-east-1
      model-id: openai.gpt-oss-20b-1:0
      max-tokens: 4096
      temperature: 0.2
      max-transcript-chars: 140000
```

El cliente usa la cadena normal de credenciales del AWS SDK, sin setear credenciales en el agente: perfil activo del sistema, variables AWS si existen, SSO/cache local, o IAM role si corre en AWS. El modelo por defecto sigue el patron de los agentes anteriores: `openai.gpt-oss-20b-1:0`.

Si en algun entorno queres override sin editar el archivo, podes usar variables de entorno:

```bash
export LEARNING_ANALYSIS_PROVIDER=local
export BEDROCK_ANALYSIS_PROFILE=otro-perfil
export BEDROCK_ANALYSIS_MODEL_ID=otro-modelo
```

Chequeo rapido:

```bash
aws sts get-caller-identity
```

Tambien tenes que tener acceso habilitado al modelo en Amazon Bedrock para la region elegida.

Variables opcionales si YouTube bloquea la IP del worker:

```bash
export YOUTUBE_TRANSCRIPT_WEBSHARE_USERNAME=
export YOUTUBE_TRANSCRIPT_WEBSHARE_PASSWORD=
export YOUTUBE_TRANSCRIPT_WEBSHARE_LOCATIONS=us,ar
export YOUTUBE_TRANSCRIPT_PROXY_PRICE_PER_GB_USD=3.50

# O usar un proxy generico
export YOUTUBE_TRANSCRIPT_PROXY_HTTP=
export YOUTUBE_TRANSCRIPT_PROXY_HTTPS=
```

Si aparece `TRANSCRIPT_PROVIDER_BLOCKED`, YouTube esta bloqueando la IP que consulta captions. La libreria `youtube-transcript-api` recomienda usar proxies, idealmente rotativos/residenciales, para ese escenario.

El worker Python loguea una estimacion de consumo por cada llamada a YouTube:

```text
proxy_usage_estimate={"videoId":"...","status":"TRANSCRIPT_FOUND","route":"webshare","requestCount":2,"totalMb":0.42,"estimatedProxyCostUsd":0.0014}
```

Campos principales:
- `route`: `webshare`, `generic_proxy` o `direct`.
- `requestCount`: cantidad de requests HTTP hacia YouTube.
- `requestBytes` / `responseBytes` / `totalBytes`: bytes aproximados observados por Python.
- `totalMb`: MB aproximados de trafico medido.
- `estimatedProxyCostUsd`: costo aproximado usando `YOUTUBE_TRANSCRIPT_PROXY_PRICE_PER_GB_USD`.

Esta medicion es una aproximacion desde Python. Webshare puede contabilizar algo mas por overhead de red, TLS, redirects, bloqueos o reintentos internos del proxy.

La misma metrica viaja en la respuesta Python -> Spring como `proxyUsage`, se devuelve en el endpoint de transcript/ingesta y queda persistida en `youtube_transcript`.

Columnas guardadas:

```text
proxy_route
proxy_request_count
proxy_request_bytes
proxy_response_bytes
proxy_total_bytes
proxy_total_mb
proxy_price_per_gb_usd
proxy_estimated_cost_usd
proxy_http_statuses_json
proxy_elapsed_seconds
```

Consulta util para proyectar plan de proxy:

```sql
SELECT
  COUNT(*) AS transcripts,
  ROUND(AVG(proxy_total_mb)::numeric, 4) AS avg_mb,
  ROUND(SUM(proxy_total_mb)::numeric, 4) AS total_mb,
  ROUND(SUM(proxy_estimated_cost_usd)::numeric, 6) AS estimated_cost_usd
FROM youtube_transcript
WHERE proxy_route IN ('webshare', 'generic_proxy');
```

## Costos
Hay dos costos separados:

1. Ingesta de transcript
2. Analisis LLM con Bedrock

### Ingesta de transcript
La ingesta usa captions publicas de YouTube via `youtube-transcript-api`.

Si el worker Python puede consultar YouTube sin proxy, el costo de ingesta es `USD 0`.

Si YouTube bloquea la IP y se usa Webshare/proxy residencial, el costo depende del trafico consumido por el proxy. Para el plan de Webshare Rotating Residential que evaluamos:

```text
1 GB:  USD 3.50 / mes
10 GB: USD 27.50 / mes
25 GB: USD 65.00 / mes
```

Estimacion practica por video:

```text
Uso bajo:       1-3 MB por video  => USD 0.0035 a USD 0.0105 con plan 1 GB
Uso moderado:  5-10 MB por video => USD 0.0175 a USD 0.0350 con plan 1 GB
```

El consumo real puede subir si YouTube devuelve bloqueos, redirects, captcha/sorry pages o si se reintenta varias veces. La cache evita volver a consultar YouTube para videos ya guardados, salvo que se use `forceRefresh`.

### Analisis Bedrock
El analisis usa `openai.gpt-oss-20b-1:0` en AWS Bedrock. Cada vez que se toca `Regenerar` se llama a Bedrock y se cobra por tokens. Si se usa el analisis cacheado, no hay llamada a Bedrock y el costo es `USD 0`.

Caso real medido con el video `dhwmc4doBbQ`:

```text
Duracion aproximada: 1h26m
Segmentos transcript: 2637
Texto crudo transcript: 83.023 caracteres
Texto enviado con timestamps: ~114.667 caracteres
Input estimado: ~30k a 35k tokens
Output estimado: ~1.6k a 2.2k tokens
```

Con precios de referencia para `gpt-oss-20b` en Bedrock:

```text
Input:  USD 0.09 / 1M tokens
Output: USD 0.39 / 1M tokens
```

Estimacion para ese video:

```text
Input:  33.000 tokens * 0.09 / 1.000.000 = USD 0.00297
Output: 2.000 tokens * 0.39 / 1.000.000 = USD 0.00078
Total aproximado: USD 0.00375
```

Regla practica:

```text
Video largo de ~1h30: USD 0.003 a USD 0.005 por regeneracion Bedrock
100 regeneraciones similares: USD 0.30 a USD 0.50 aprox.
1000 regeneraciones similares: USD 3 a USD 5 aprox.
```

El analisis local (`provider=LOCAL`) no consume modelos pagos, pero es heuristico y no reemplaza la calidad del analisis con Bedrock.

### Tabla aproximada por duracion
Estimacion orientativa usando `openai.gpt-oss-20b-1:0` en Bedrock, transcript con timestamps y salida de analisis estructurada. Los costos reales dependen de velocidad del habla, calidad/cantidad de captions, idioma, output generado y reintentos.

| Duracion video | Input tokens aprox | Output tokens aprox | Bedrock aprox por regeneracion | Proxy bajo aprox | Proxy moderado aprox | Total aprox con proxy bajo | Total aprox con proxy moderado |
| --- | ---: | ---: | ---: | ---: | ---: | ---: | ---: |
| 5 min | 2k-3k | 800-1.2k | USD 0.0005-0.0008 | USD 0.0002-0.0006 | USD 0.0010-0.0020 | USD 0.0007-0.0014 | USD 0.0015-0.0028 |
| 10 min | 4k-6k | 1k-1.5k | USD 0.0008-0.0012 | USD 0.0004-0.0012 | USD 0.0020-0.0040 | USD 0.0012-0.0024 | USD 0.0028-0.0052 |
| 20 min | 8k-12k | 1.2k-1.8k | USD 0.0012-0.0018 | USD 0.0008-0.0024 | USD 0.0040-0.0080 | USD 0.0020-0.0042 | USD 0.0052-0.0098 |
| 30 min | 12k-18k | 1.4k-2k | USD 0.0017-0.0025 | USD 0.0012-0.0036 | USD 0.0060-0.0120 | USD 0.0029-0.0061 | USD 0.0077-0.0145 |
| 1 h | 24k-36k | 1.8k-2.5k | USD 0.0029-0.0042 | USD 0.0024-0.0072 | USD 0.0120-0.0240 | USD 0.0053-0.0114 | USD 0.0149-0.0282 |

Notas:
- `Bedrock aprox` se cobra cada vez que se usa `Regenerar`.
- Si se reutiliza un analisis guardado en cache, Bedrock cuesta `USD 0`.
- `Proxy bajo` asume aprox. 0.05-0.15 MB por minuto.
- `Proxy moderado` asume aprox. 0.25-0.50 MB por minuto, incluyendo overhead/reintentos leves.
- Si YouTube bloquea, redirige a captcha/sorry page o hay muchos reintentos, el proxy puede consumir mas.

## Limites
- No descarga audio.
- No usa Whisper ni providers STT.
- No soporta videos privados, con restriccion de edad o sin captions publicas.
- No hace scraping con navegador.
