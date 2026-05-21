# Architecture

La implementacion actual tiene dos runtimes separados por carpeta:

- `frontend/`: React/Vite/TypeScript.
- `src/`: backend Spring Boot.

```text
agent-05-voice-companion-older-adults/
├── frontend/
│   ├── index.html
│   ├── main.tsx
│   └── style.css
├── src/
│   ├── main/
│   │   ├── java/com/aichallenge/agents/voicecompanion/
│   │   │   ├── application/
│   │   │   ├── domain/
│   │   │   └── infrastructure/
│   │   │       ├── ai/
│   │   │       ├── input/web/
│   │   │       └── output/
│   │   └── resources/
│   └── test/
│       └── java/com/aichallenge/agents/voicecompanion/
├── pom.xml
├── package.json
├── tsconfig.json
└── vite.config.ts
```

## Frontend

Responsabilidades:

- iniciar la demo local desde `/api/demo/default`;
- capturar microfono;
- diagnosticar dispositivos de audio;
- crear `RTCPeerConnection`;
- enviar SDP offer al backend;
- reproducir audio remoto;
- escuchar eventos Realtime por data channel.

## Backend

Responsabilidades:

- cargar perfil y demo script versionados;
- ejecutar demo local deterministica;
- generar resumen Markdown para demo local;
- construir prompt Realtime;
- negociar sesion OpenAI Realtime server-side;
- mantener la API key fuera del navegador.

Capas Java:

- `application`: casos de uso y orquestacion.
- `domain`: records puros de perfil, recordatorios, transcript y resumen.
- `infrastructure.ai`: gateway OpenAI Realtime y properties.
- `infrastructure.input.web`: controllers REST.
- `infrastructure.output`: renderer Markdown.

## Flujo Web Demo

1. Se inicia Spring Boot en `http://localhost:8080`.
2. Se inicia Vite en `http://localhost:3000`.
3. El frontend llama `/api/demo/default`.
4. Vite proxya `/api` al backend Spring Boot.
5. El backend carga ejemplos versionados.
6. El backend simula la conversacion y devuelve resumen + Markdown.

## Flujo Realtime

1. El usuario pulsa `Hablar con IA`.
2. React pide permiso de microfono.
3. React crea un `RTCPeerConnection`.
4. React agrega el audio track local.
5. React crea un data channel `oai-events`.
6. React genera un SDP offer.
7. React envia el SDP offer a `POST /api/realtime/session`.
8. Spring Boot combina el SDP con la configuracion de sesion.
9. Spring Boot llama a OpenAI Realtime usando `OPENAI_API_KEY` server-side.
10. La configuracion usa `gpt-realtime-2` y `reasoning.effort=low` por defecto.
11. Spring Boot devuelve el SDP answer.
12. React completa la conexion WebRTC, reproduce audio remoto y escucha eventos.

## Codigo retirado

La primera iteracion tenia un CLI Node/TypeScript con carpetas:

- `src/application`;
- `src/domain`;
- `src/infrastructure/ai`;
- `src/infrastructure/input/cli`;
- `src/infrastructure/output`.

Ese codigo fue eliminado porque el backend Spring Boot ya cubre esas responsabilidades. `src/` queda reservado para Spring Boot.
