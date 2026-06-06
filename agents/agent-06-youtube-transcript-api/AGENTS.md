# AGENTS.md

## Rol
Agente para convertir URLs de YouTube en una biblioteca personal de aprendizaje: ingesta de captions publicas, persistencia de transcript original, analisis en castellano y visualizacion simple desde frontend.

## Reglas actuales
- Mantener Spring Boot como API principal.
- Mantener Python como provider interno para `youtube-transcript-api`.
- No agregar descarga de audio ni Whisper en esta version.
- Priorizar transcript original como contexto del LLM; el output de aprendizaje debe ser en castellano.
- Usar AWS Bedrock como provider de analisis por defecto y mantener fallback local extractivo sin costo.
- Usar credenciales AWS del sistema por la cadena default del SDK; no guardar credenciales en el repo.
- Mantener cache y persistencia estable antes de volver a consultar YouTube o regenerar analisis.
- Persistir metricas aproximadas de proxy para proyectar uso y costo.
- Exponer pruebas manuales via Swagger y una UI React simple para flujo de producto.

## Servicios
- Spring Boot: API publica, casos de uso, persistencia, Bedrock y respuestas al frontend.
- Python FastAPI: worker interno para consultar captions publicas de YouTube.
- PostgreSQL: videos, transcripts, segmentos, analisis y metricas de proxy.
- React/TypeScript: ingesta, biblioteca, transcript, analisis y consumo de proxy.

## Gotchas
- Si cambia el worker Python o su Dockerfile, reconstruir el servicio con Docker.
- Si cambia `application.yml`, Java o migraciones Flyway, reiniciar Spring Boot.
- Si una respuesta aparece como cache, no deberia volver a consumir proxy salvo que se use `forceRefresh`.
- Webshare/proxy se configura en el entorno del worker Python; Spring solo consume las metricas que el worker devuelve.
- El costo Bedrock depende de regeneraciones de analisis; reutilizar analisis cacheado no llama al modelo.

## Evolucion natural
1. Agregar fallback opcional con Whisper.
2. Guardar metadata enriquecida del video.
3. Exponer busqueda por transcript.
4. Agregar tags con AI y/o reglas personales.
5. Vectorizar segmentos para RAG.
6. Conectar ideas extraidas con specs, issues, playbooks o proyectos personales.
