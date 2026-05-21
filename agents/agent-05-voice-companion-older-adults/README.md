# agent-05-voice-companion-older-adults

Agente de acompanamiento por voz para personas mayores. Permite una conversacion calida y no medica, consulta recordatorios simples cargados localmente y genera un resumen para familiares o cuidadores.

## Que resuelve
Muchas personas mayores pasan largos periodos sin interaccion social frecuente. Este agente explora una interfaz mas natural y accesible: la voz.

El objetivo no es reemplazar familiares, cuidadores ni profesionales de salud. El agente funciona como apoyo cotidiano, compania ligera e inclusion digital.

## Enfoque
- Web app React/Vite para iniciar una sesion de voz.
- Backend Spring Boot para demo local y negociacion Realtime.
- Demo local sin credenciales expuesta por API.
- Modo AI web con OpenAI Realtime API.
- Recordatorios simples desde JSON.
- Resumen final en Markdown para la demo local.
- Limites explicitos: no diagnostico, no recomendaciones clinicas, no emergencias.

## Documentacion tecnica

Para continuar la integracion Realtime, revisar:

- [`docs/realtime-integration.md`](docs/realtime-integration.md): arquitectura backend/frontend, flujo WebRTC, troubleshooting, costos, cuellos de botella y proximas mejoras.
- [`docs/architecture.md`](docs/architecture.md): estructura general.
- [`docs/prompt.md`](docs/prompt.md): prompt base y reglas de tono/seguridad.
- [`docs/safety.md`](docs/safety.md): limites no medicos.
- [`docs/cost-notes.md`](docs/cost-notes.md): notas de costo.

## Comando principal
Frontend:

```bash
npm install
npm run dev
```

Backend Spring Boot:

```bash
export OPENAI_API_KEY="..."
export OPENAI_REALTIME_MODEL="gpt-realtime-2"
export OPENAI_REALTIME_REASONING_EFFORT="low"
mvn spring-boot:run
```

Flujo full-stack recomendado:

```bash
# Terminal 1
mvn spring-boot:run

# Terminal 2
npm run dev
```

Abrir:

```text
http://localhost:3000
```

## Modo Realtime
Proveedor: OpenAI  
API: Realtime API  
Modelo por defecto: `gpt-realtime-2`

Variables esperadas:

```bash
export OPENAI_API_KEY=
export OPENAI_REALTIME_MODEL=gpt-realtime-2
export OPENAI_REALTIME_VOICE=marin
export OPENAI_REALTIME_REASONING_EFFORT=low
export APP_PORT=3000
```

La sesion Realtime se inicia desde el frontend con `Hablar con IA`. El backend:

- recibe el SDP offer en `/api/realtime/session`;
- arma instrucciones con perfil, recordatorios y reglas de seguridad;
- llama OpenAI Realtime con `OPENAI_API_KEY`;
- devuelve el SDP answer al navegador.

## Demo local
La demo local funciona sin credenciales y usa los ejemplos versionados:

- `examples/older-adult-profile.example.json`
- `examples/demo-script.example.md`

Endpoints:

```bash
curl http://localhost:8080/api/demo/default
curl http://localhost:8080/api/health
```

Sirve para:
- probar parsing de inputs,
- validar recordatorios,
- generar resumen,
- crear una demo reproducible,
- mostrar el impacto del agente sin costo de AI desde el frontend o por API.

## Costos estimados
Referencia oficial:
- https://openai.com/api/pricing/
- https://platform.openai.com/docs/guides/realtime-costs

Precio de referencia observado para `gpt-realtime-2`:
- Audio input: USD 32.00 por 1M tokens.
- Audio output: USD 64.00 por 1M tokens.
- Text input: USD 4.00 por 1M tokens.
- Text output: USD 24.00 por 1M tokens.

Estimacion inicial:
- Sesion corta de 2 a 3 minutos.
- Normalmente menor a USD 0.10 por demo.
- El costo real depende de duracion, turnos conversacionales, audio de entrada, audio de salida y precios vigentes.

Guardrail:
- Pendiente: estimar costo antes de iniciar una sesion Realtime larga.
- Modo local: USD 0.00.

## Seguridad y limites
- No diagnostica.
- No recomienda tratamientos.
- No gestiona emergencias reales.
- No reemplaza familiares, cuidadores ni profesionales.
- No debe fingir ser familiar.
- Debe ser transparente: es un acompanante de voz con AI.
- Si se mencionan emergencias, debe recomendar contactar servicios de emergencia o una persona de confianza inmediatamente.

## Demo
La web incluye:

- `Demo local`: conversacion deterministica sin AI.
- `Hablar con IA`: conversacion Realtime con OpenAI.
- `Probar micro`: diagnostico de captura local.
- `Dispositivos`: seleccion de input de audio.

## Estructura actual
```text
agent-05-voice-companion-older-adults/
├── README.md
├── AGENTS.md
├── SKILLS.md
├── Specification.md
├── package.json
├── .env.example
├── examples/
├── docs/
├── frontend/
│   ├── index.html
│   ├── main.tsx
│   └── style.css
└── src/
    ├── main/
    │   ├── java/
    │   └── resources/
    └── test/
        └── java/
```

## Estado actual
Primera base funcional full-stack creada:
- `README.md`
- `AGENTS.md`
- `SKILLS.md`
- `Specification.md`
- `package.json`
- `pom.xml`
- `.env.example`
- `examples/older-adult-profile.example.json`
- `examples/demo-script.example.md`
- `docs/`
- `src/`

Funciona end-to-end:
- Backend Spring Boot con `/api/health`, `/api/demo/default` y `/api/demo/local`.
- Backend Spring Boot con `/api/realtime/session` para negociar WebRTC con OpenAI Realtime sin exponer la API key.
- Frontend React/Vite/TypeScript que ejecuta la demo desde el backend y permite hablar con OpenAI Realtime.
- Carga de perfil y recordatorios desde JSON.
- Lectura de demo script Markdown.
- Consulta simple de recordatorios.
- Resumen Markdown.
- Tests Java para parser, caso de uso, prompt builder y writer.

Pendiente:
- Persistir resumen de una charla Realtime real.
- Agregar herramientas/function calling para consultar recordatorios desde el modelo en tiempo real.
- Estimacion interactiva de costo antes de sesiones AI reales.

## Troubleshooting Realtime
Si OpenAI responde:

```text
The model `gpt-realtime` does not exist or you do not have access to it.
```

probá primero con el modelo recomendado para voice agents:

```bash
export OPENAI_REALTIME_MODEL="gpt-realtime-2"
export OPENAI_REALTIME_REASONING_EFFORT="low"
mvn spring-boot:run
```

Si tu proyecto todavia no tiene acceso a Realtime 2, el error depende del acceso habilitado en el proyecto/API key.

## Roadmap
1. Persistir transcript Realtime.
2. Generar resumen al cortar una llamada real.
3. Agregar tools/function calling para recordatorios.
4. Agregar estimador de costos antes de sesiones largas.
5. Guardar ultimo microfono funcional y fallback automatico.
6. Mejorar compatibilidad Safari.
