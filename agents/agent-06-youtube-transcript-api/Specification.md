# Specification — Personal YouTube Learning Ingestion Agent

## 1. Vision

Construir el primer paso de un sistema personal de aprendizaje asistido por AI.

El usuario debe poder enviar una URL de YouTube de un video que le interesa aprender, aunque no tenga tiempo de verlo completo. El sistema debe capturar y persistir la informacion base del video para que futuras features puedan convertir ese contenido en conocimiento util, accionable y conectado con sus proyectos.

Este agente no debe ser solamente un "transcriptor". Debe funcionar como puerta de entrada para una biblioteca personal de aprendizaje desde videos.

## 2. Problema

Mucho contenido valioso para aprender esta en videos largos. El usuario suele encontrar material interesante, pero no siempre tiene tiempo para verlo, tomar notas, extraer ideas, detectar secciones importantes o conectar ese conocimiento con proyectos reales.

El sistema debe permitir:

- Guardar rapidamente una URL de YouTube.
- Obtener la transcripcion original disponible.
- Persistir la data necesaria para futuros analisis.
- Evitar volver a consultar YouTube si el video ya fue ingerido.
- Preparar el contenido para que un LLM lo interprete en su idioma original.
- Generar, en futuras iteraciones, outputs en castellano para aprendizaje personal.

## 3. Principio de idioma

El caso de uso comun es enviar solamente la URL.

```json
{
  "url": "https://www.youtube.com/watch?v=dtAJ2dOd3ko"
}
```

Cuando no se envia `preferredLanguages`, el sistema debe tomar la transcripcion original disponible en YouTube.

Razonamiento:

- El transcript original conserva mejor el contenido.
- El LLM puede interpretar multiples idiomas mejor que una preferencia manual fija.
- El output final de aprendizaje debe ser en castellano, pero el contexto debe mantenerse en el idioma original.

Si el usuario envia `preferredLanguages`, esos idiomas son una preferencia explicita, no una restriccion dura. Si no existe transcript en esos idiomas, el sistema debe caer a la transcripcion original disponible.

## 4. Alcance actual

Incluido hoy:

- Endpoint REST para recibir una URL de YouTube.
- Extraccion de `videoId`.
- Worker Python con `youtube-transcript-api`.
- Obtencion de captions publicas.
- Persistencia en PostgreSQL de video, transcript completo y segmentos.
- Cache por `videoId` e idioma.
- Fallback a transcript original disponible.
- Listado de videos ingeridos.
- Analisis de aprendizaje en castellano persistido.
- Bedrock como provider LLM por defecto con fallback local extractivo.
- Metricas aproximadas de proxy persistidas junto al transcript.
- Frontend React/TypeScript para ingesta, biblioteca, transcript, analisis y consumo de proxy.
- Swagger/OpenAPI.

Este MVP ya funciona como capa inicial de ingesta y aprendizaje personal.

## 5. Nuevo objetivo de producto

Reformular el agente como:

```text
Personal YouTube Learning Ingestion Agent
```

El endpoint principal debe servir para "registrar/ingerir un video" y no para devolver una transcripcion gigante como respuesta principal de producto.

La transcripcion completa debe quedar guardada en base de datos para futuras features, pero la respuesta HTTP del endpoint de ingesta debe ser compacta.

## 6. Endpoint propuesto — Ingestar video

### 6.1 Endpoint

```http
POST /api/v1/learning/youtube/videos
```

### 6.2 Request comun

```json
{
  "url": "https://www.youtube.com/watch?v=dtAJ2dOd3ko"
}
```

### 6.3 Request avanzado

```json
{
  "url": "https://www.youtube.com/watch?v=dtAJ2dOd3ko",
  "preferredLanguages": ["es", "en"],
  "forceRefresh": false,
  "tags": ["ai-agents", "coding", "skills"],
  "notes": "Video para revisar ideas de handoff entre agentes"
}
```

### 6.4 Campos

| Campo | Tipo | Requerido | Descripcion |
|---|---:|---:|---|
| `url` | string | Si | URL de YouTube. |
| `preferredLanguages` | array string | No | Preferencia explicita de idioma. Si no se envia, usar transcript original disponible. |
| `forceRefresh` | boolean | No | Si es `true`, volver a consultar provider aunque exista cache. Default `false`. |
| `tags` | array string | No | Etiquetas personales para organizar aprendizaje. |
| `notes` | string | No | Nota personal inicial sobre por que interesa el video. |

### 6.5 Response esperada

```json
{
  "status": "VIDEO_INGESTED",
  "videoId": "dtAJ2dOd3ko",
  "url": "https://www.youtube.com/watch?v=dtAJ2dOd3ko",
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
      "href": "/api/v1/learning/youtube/videos/dtAJ2dOd3ko/analysis"
    }
  ]
}
```

### 6.6 Response desde cache

```json
{
  "status": "VIDEO_ALREADY_INGESTED",
  "videoId": "dtAJ2dOd3ko",
  "language": "en",
  "fromCache": true,
  "transcriptStored": true,
  "segmentsStored": 357,
  "readyForAnalysis": true
}
```

## 7. Endpoint propuesto — Obtener transcript guardado

Este endpoint es util para debug, auditoria o futuras UIs internas, pero no deberia ser la experiencia principal.

```http
GET /api/v1/learning/youtube/videos/{videoId}/transcript
```

Response:

```json
{
  "videoId": "dtAJ2dOd3ko",
  "language": "en",
  "fullText": "...",
  "segments": []
}
```

## 8. Endpoint actual — Analisis de aprendizaje

```http
POST /api/v1/learning/youtube/videos/{videoId}/analysis
```

Este endpoint usa la transcripcion original persistida como contexto para un LLM. Por defecto usa Bedrock con `openai.gpt-oss-20b-1:0`; si Bedrock falla o se configura modo local, usa el analisis extractivo sin costo.

Regla de prompt:

```text
Interpreta el transcript en su idioma original.
No traduzcas primero el contenido.
Extrae el entendimiento principal.
Devuelve el output final en castellano rioplatense claro y util.
Conecta las ideas con aprendizaje personal y proyectos de software cuando corresponda.
```

### Response

```json
{
  "videoId": "dtAJ2dOd3ko",
  "analysisLanguage": "es",
  "sourceLanguage": "en",
  "summary": "El video explica una tecnica para transferir contexto entre sesiones de agentes...",
  "keyIdeas": [
    "Un handoff permite pasar contexto util a otra sesion sin contaminar la conversacion actual.",
    "Los documentos markdown funcionan como interfaz portable entre agentes.",
    "Separar sesiones ayuda a evitar degradacion por exceso de contexto."
  ],
  "projectApplications": [
    {
      "idea": "Agregar handoff documents al playbook del repo de agentes.",
      "whyItMatters": "Permite continuar trabajo entre agentes semanales sin perder decisiones."
    }
  ],
  "importantSegments": [
    {
      "start": 29.039,
      "duration": 4.481,
      "reason": "Define la idea central del handoff skill."
    }
  ],
  "personalLearningNotes": [
    "Revisar como documentar handoffs en proyectos largos.",
    "Comparar handoff manual vs compactacion automatica."
  ],
  "suggestedActions": [
    "Crear una skill interna para handoff de agentes del challenge.",
    "Agregar un template de handoff al playbook."
  ]
}
```

## 9. Modelo de datos actual

Hoy ya existe:

### 9.1 `youtube_video`

Guarda:

- `video_id`
- `original_url`
- `created_at`
- `updated_at`

### 9.2 `youtube_transcript`

Guarda:

- `id`
- `video_id`
- `language`
- `source`
- `generated`
- `language_detection_method`
- `language_fallback_used`
- `full_text`
- `created_at`
- `updated_at`

### 9.3 `youtube_transcript_segment`

Guarda:

- `id`
- `transcript_id`
- `position`
- `start_time`
- `duration`
- `text`

### 9.4 `youtube_video_analysis`

Guarda:

- `id`
- `video_id`
- `transcript_id`
- `source_language`
- `output_language`
- `provider`
- `model`
- `summary`
- `key_ideas_json`
- `project_applications_json`
- `important_segments_json`
- `personal_learning_notes_json`
- `suggested_actions_json`
- `prompt_version`
- `created_at`

### 9.5 Metricas de proxy en `youtube_transcript`

Guarda una estimacion aproximada del consumo del worker Python:

- `proxy_route`
- `proxy_request_count`
- `proxy_request_bytes`
- `proxy_response_bytes`
- `proxy_total_bytes`
- `proxy_total_mb`
- `proxy_price_per_gb_usd`
- `proxy_estimated_cost_usd`
- `proxy_http_statuses_json`
- `proxy_elapsed_seconds`

## 10. Cambios propuestos en base de datos

Para convertir el agente en una base de aprendizaje personal, agregar:

### 10.1 En `youtube_video`

```sql
ALTER TABLE youtube_video
ADD COLUMN title TEXT,
ADD COLUMN channel_name TEXT,
ADD COLUMN thumbnail_url TEXT,
ADD COLUMN published_at TIMESTAMP,
ADD COLUMN duration_seconds INTEGER,
ADD COLUMN ingestion_status VARCHAR(40) NOT NULL DEFAULT 'INGESTED',
ADD COLUMN user_notes TEXT;
```

Motivo:

- Mejor UI futura.
- Mejor organizacion personal.
- Saber que videos ya estan listos para analisis.
- Guardar intencion del usuario.

### 10.2 Nueva tabla `youtube_video_tag`

```sql
CREATE TABLE youtube_video_tag (
    id BIGSERIAL PRIMARY KEY,
    video_id VARCHAR(50) NOT NULL,
    tag VARCHAR(80) NOT NULL,
    created_at TIMESTAMP NOT NULL,
    CONSTRAINT fk_youtube_video_tag_video
        FOREIGN KEY (video_id)
        REFERENCES youtube_video(video_id)
        ON DELETE CASCADE,
    CONSTRAINT uk_youtube_video_tag
        UNIQUE (video_id, tag)
);
```

Motivo:

- Organizar videos por temas personales: `ai-agents`, `spring`, `productivity`, `architecture`.

### 10.3 Tabla implementada `youtube_video_analysis`

```sql
CREATE TABLE youtube_video_analysis (
    id BIGSERIAL PRIMARY KEY,
    video_id VARCHAR(50) NOT NULL,
    transcript_id BIGINT NOT NULL,
    source_language VARCHAR(10) NOT NULL,
    output_language VARCHAR(10) NOT NULL,
    provider VARCHAR(40) NOT NULL,
    model VARCHAR(120) NOT NULL,
    summary TEXT NOT NULL,
    key_ideas_json JSONB NOT NULL,
    project_applications_json JSONB NOT NULL,
    important_segments_json JSONB NOT NULL,
    personal_learning_notes_json JSONB NOT NULL,
    suggested_actions_json JSONB NOT NULL,
    prompt_version VARCHAR(40) NOT NULL,
    created_at TIMESTAMP NOT NULL,
    CONSTRAINT fk_youtube_video_analysis_video
        FOREIGN KEY (video_id)
        REFERENCES youtube_video(video_id)
        ON DELETE CASCADE,
    CONSTRAINT fk_youtube_video_analysis_transcript
        FOREIGN KEY (transcript_id)
        REFERENCES youtube_transcript(id)
        ON DELETE CASCADE
);
```

Motivo:

- Guardar outputs generados por LLM.
- Permitir regenerar analisis con otro prompt/modelo.
- Mantener historial de analisis.

### 10.4 Nueva tabla futura `youtube_video_learning_note`

Opcional para una etapa posterior:

```sql
CREATE TABLE youtube_video_learning_note (
    id BIGSERIAL PRIMARY KEY,
    video_id VARCHAR(50) NOT NULL,
    note TEXT NOT NULL,
    source VARCHAR(40) NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    CONSTRAINT fk_youtube_video_learning_note_video
        FOREIGN KEY (video_id)
        REFERENCES youtube_video(video_id)
        ON DELETE CASCADE
);
```

Motivo:

- Guardar notas manuales o editadas por el usuario.
- Diferenciar notas generadas por AI de notas personales.

## 11. Cambios propuestos en codigo

### 11.1 Renombrar intencion del caso de uso

Actual:

```text
GetYoutubeTranscriptUseCase
```

Propuesto:

```text
IngestYoutubeLearningVideoUseCase
```

El use case debe devolver una respuesta compacta de ingesta, no el transcript completo.

### 11.2 Nuevo controller publico

Actual:

```http
POST /api/v1/youtube/transcripts
```

Propuesto:

```http
POST /api/v1/learning/youtube/videos
```

Mantener el endpoint actual temporalmente como compatibilidad o marcarlo como debug/deprecated.

### 11.3 Separar transcript retrieval de ingestion

Crear un endpoint especifico para ver transcript guardado:

```http
GET /api/v1/learning/youtube/videos/{videoId}/transcript
```

### 11.4 Agregar modelo de respuesta compacto

```java
public record YoutubeLearningVideoIngestionResponse(
    String status,
    String videoId,
    String url,
    String language,
    TranscriptSource source,
    boolean generated,
    boolean fromCache,
    boolean transcriptStored,
    int segmentsStored,
    boolean readyForAnalysis,
    List<NextActionResponse> nextActions
) {}
```

### 11.5 Analysis port implementado

```java
public interface VideoLearningAnalysisGenerator {
    VideoLearningAnalysis analyze(Transcript transcript, LearningAnalysisRequest request);
}
```

Implementaciones:

- `BedrockVideoLearningAnalysisGenerator`
- `LocalExtractiveVideoLearningAnalysisGenerator`

### 11.6 Prompt versionado

Crear:

```text
src/main/resources/prompts/youtube-learning-analysis-v1.md
```

El prompt debe:

- Usar transcript original como contexto.
- Responder en castellano.
- Extraer ideas, aplicaciones a proyectos, timestamps importantes y acciones sugeridas.
- Devolver JSON estructurado.

## 12. Estados propuestos

```text
VIDEO_INGESTED
VIDEO_ALREADY_INGESTED
TRANSCRIPT_NOT_AVAILABLE
VIDEO_UNAVAILABLE
INVALID_YOUTUBE_URL
PROVIDER_ERROR
ANALYSIS_CREATED
ANALYSIS_ALREADY_EXISTS
ANALYSIS_FAILED
```

## 13. Plan de implementacion y estado

### Fase 1 — Reposicionar ingesta

- Crear `POST /api/v1/learning/youtube/videos`.
- Responder compacto.
- Mantener persistencia actual.
- Mantener endpoint anterior como debug o compatibilidad.
- Estado: implementado.

### Fase 2 — Enriquecer datos personales

- Agregar `tags` y `notes`.
- Agregar migracion para columnas en `youtube_video`.
- Agregar tabla `youtube_video_tag`.
- Ajustar README y Swagger.
- Estado: pendiente.

### Fase 3 — Analisis con LLM

- Agregar `POST /api/v1/learning/youtube/videos/{videoId}/analysis`.
- Agregar tabla `youtube_video_analysis`.
- Crear prompt versionado.
- Empezar con provider configurable: `mock`, `openai` o `bedrock`.

Estado actual:

- Endpoint de analisis implementado.
- Tabla `youtube_video_analysis` implementada.
- Provider Bedrock implementado como default con `openai.gpt-oss-20b-1:0`.
- Provider `LOCAL/extractive-v3` disponible como fallback sin costo AI.
- Pendiente: prompt versionado externo como archivo separado si se quiere auditar prompts fuera del codigo.

### Fase 4 — Aprendizaje personal

- Agregar notas editables.
- Agregar busqueda por tags.
- Mejorar listado de videos ingeridos con metadata enriquecida.
- Agregar ranking de videos pendientes de revisar.
- Agregar conexiones con proyectos locales del repo.
- Estado: biblioteca inicial implementada; notas/tags/ranking pendientes.

## 14. Decisiones pendientes

Para siguientes iteraciones, decidir:

1. Si el endpoint viejo `/api/v1/youtube/transcripts` queda como compatibilidad permanente o se marca deprecated.
2. Si agregamos metadata del video desde YouTube en esta iteracion o solo tags/notas.
3. Si se agrega fallback Whisper/yt-dlp para videos sin captions publicas.
4. Si el output debe decir "castellano", "espanol" o permitir configuracion por usuario.
5. Si las metricas de costo Bedrock deben persistirse igual que las metricas de proxy.
