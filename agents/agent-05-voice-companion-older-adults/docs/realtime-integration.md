# Realtime Integration Notes

Documento para continuar la iteracion del agente 05 desde la version actual.

## Estado implementado

La integracion Realtime funciona como una experiencia web full-stack:

- Frontend: React + Vite + TypeScript.
- Backend: Spring Boot.
- Transporte de voz: WebRTC.
- API AI: OpenAI Realtime.
- Modelo por defecto: `gpt-realtime-2`.
- Reasoning por defecto: `low`.
- Voz por defecto: `marin`.
- La API key queda solo en backend mediante `OPENAI_API_KEY`.

La demo local deterministica sigue existiendo en Spring Boot y no usa AI.

## Archivos principales

Backend:

- `src/main/java/com/aichallenge/agents/voicecompanion/infrastructure/input/web/RealtimeController.java`
- `src/main/java/com/aichallenge/agents/voicecompanion/application/RealtimeSessionService.java`
- `src/main/java/com/aichallenge/agents/voicecompanion/application/RealtimePromptBuilder.java`
- `src/main/java/com/aichallenge/agents/voicecompanion/infrastructure/ai/HttpOpenAIRealtimeGateway.java`
- `src/main/java/com/aichallenge/agents/voicecompanion/infrastructure/ai/OpenAIRealtimeProperties.java`
- `src/main/resources/application.properties`

Frontend:

- `frontend/main.tsx`
- `frontend/style.css`
- `vite.config.ts`

Docs relacionadas:

- `docs/architecture.md`
- `docs/prompt.md`
- `docs/safety.md`
- `docs/cost-notes.md`

## Flujo backend

1. El frontend captura microfono con `navigator.mediaDevices.getUserMedia`.
2. El frontend crea un `RTCPeerConnection`.
3. El frontend agrega el audio track local al peer connection.
4. El frontend crea un data channel `oai-events`.
5. El frontend genera un SDP offer.
6. El frontend envia el SDP offer a:

```text
POST /api/realtime/session
Content-Type: application/sdp
```

7. Spring Boot carga el perfil demo desde:

```text
examples/older-adult-profile.example.json
```

8. `RealtimePromptBuilder` arma instrucciones con:

- tono;
- limites no medicos;
- idioma;
- nombre preferido;
- recordatorios configurados.

9. `RealtimeSessionService` arma la configuracion de sesion:

- `model`;
- `instructions`;
- `reasoning.effort`;
- `output_modalities`;
- audio input;
- transcripcion;
- server VAD;
- audio output voice.

10. `HttpOpenAIRealtimeGateway` llama a OpenAI Realtime con la API key server-side.
11. OpenAI devuelve el SDP answer.
12. Spring Boot devuelve el SDP answer al frontend.
13. El frontend llama `setRemoteDescription`.
14. La sesion queda abierta para audio y eventos por data channel.

## Flujo frontend

La UI tiene dos caminos:

- `Demo local`: llama `/api/demo/default`, sin AI.
- `Hablar con IA`: abre WebRTC contra OpenAI via backend.

Para Realtime:

1. `Habilitar audio` intenta desbloquear audio remoto en navegadores estrictos.
2. `Dispositivos` lista inputs de audio.
3. `Probar micro` abre el microfono local sin conectar con OpenAI.
4. El medidor de microfono valida si el navegador entrega audio real.
5. `Hablar con IA` crea la sesion Realtime.
6. `Forzar respuesta` envia `input_audio_buffer.commit` y `response.create` por data channel.

La UI muestra:

- estado Realtime;
- nivel de microfono;
- track label;
- `live/ended`;
- `enabled/disabled`;
- `muted/unmuted`;
- sample rate;
- eventos Realtime recientes.

Esto existe porque el mayor cuello de botella observado fue captura local de microfono, no OpenAI.

## Configuracion

Variables esperadas:

```bash
export OPENAI_API_KEY="..."
export OPENAI_REALTIME_MODEL="gpt-realtime-2"
export OPENAI_REALTIME_VOICE="marin"
export OPENAI_REALTIME_REASONING_EFFORT="low"
```

Propiedades Spring:

```properties
openai.api-key=${OPENAI_API_KEY:}
openai.realtime.model=${OPENAI_REALTIME_MODEL:gpt-realtime-2}
openai.realtime.voice=${OPENAI_REALTIME_VOICE:marin}
openai.realtime.reasoning-effort=${OPENAI_REALTIME_REASONING_EFFORT:low}
```

No poner API keys reales en `application.properties`.

## Como levantar

Backend:

```bash
export OPENAI_API_KEY="..."
export OPENAI_REALTIME_MODEL="gpt-realtime-2"
export OPENAI_REALTIME_VOICE="marin"
export OPENAI_REALTIME_REASONING_EFFORT="low"
mvn spring-boot:run
```

Frontend:

```bash
npm run dev
```

Abrir:

```text
http://localhost:3000
```

Diagnosticar config backend:

```bash
curl http://localhost:8080/api/realtime/config
```

Respuesta esperada:

```json
{
  "model": "gpt-realtime-2",
  "voice": "marin",
  "reasoningEffort": "low",
  "apiKeyConfigured": true
}
```

## Cuellos de botella observados

### 1. Acceso al modelo

Sintoma:

```text
model_not_found
```

Causa:

- El proyecto/API key no tiene acceso al modelo configurado.
- La variable `OPENAI_REALTIME_MODEL` no fue tomada porque Spring no se reinicio.

Diagnostico:

```bash
curl http://localhost:8080/api/realtime/config
```

### 2. Captura de microfono

Sintoma:

```text
AirPods (live, enabled, muted, 48000 Hz)
```

Interpretacion:

- El navegador abrio un track.
- El track esta vivo y habilitado.
- El dispositivo no entrega audio real.

Esto no es un problema de OpenAI.

Acciones:

- Usar `Probar micro`.
- Cambiar dispositivo.
- Usar el microfono interno o un telefono como input.
- Evitar AirPods si aparecen `muted`.
- Revisar permisos de macOS y navegador.
- Cerrar apps que puedan tomar el microfono.

### 3. Safari

Safari es mas estricto con:

- autoplay;
- permisos de microfono;
- `AudioContext`;
- seleccion de dispositivo;
- WebRTC en contextos no seguros.

La UI tiene `Habilitar audio` para forzar gesto de usuario antes de reproducir audio remoto.

Recomendacion:

- Para desarrollo principal, validar primero en Chrome.
- Compatibilizar Safari al final.

### 4. VAD y turnos

La sesion usa server-side VAD:

```json
{
  "type": "server_vad",
  "threshold": 0.45,
  "prefix_padding_ms": 300,
  "silence_duration_ms": 700,
  "idle_timeout_ms": 6000,
  "create_response": true,
  "interrupt_response": true
}
```

Si el microfono se mueve pero el modelo no responde:

- mirar eventos `speech_started` / `speech_stopped`;
- usar `Forzar respuesta`;
- ajustar `threshold` y `silence_duration_ms`;
- revisar si el audio llega demasiado bajo.

### 5. Latencia

Factores:

- microfono/browser;
- WebRTC handshake;
- roundtrip backend -> OpenAI;
- `reasoning.effort`;
- longitud del prompt;
- longitud del contexto acumulado;
- duracion de la respuesta hablada.

Default actual:

```bash
OPENAI_REALTIME_REASONING_EFFORT=low
```

Mantener `low` para demos. Subirlo solo si el caso requiere razonamiento mas complejo.

## Costos

Referencia oficial:

- OpenAI pricing: https://openai.com/api/pricing/
- Realtime costs: https://platform.openai.com/docs/guides/realtime-costs
- Anuncio GPT-Realtime-2: https://openai.com/index/advancing-voice-intelligence-with-new-models-in-the-api/

Precios de referencia observados para `gpt-realtime-2` al 2026-05-15:

- Audio input: USD 32.00 por 1M tokens.
- Audio output: USD 64.00 por 1M tokens.
- Cached audio input: USD 0.40 por 1M tokens.

Segun la guia de costos de Realtime:

- audio de usuario: 1 token por 100 ms;
- audio del asistente: 1 token por 50 ms.

Formula aproximada:

```text
input_audio_tokens = user_speaking_seconds * 10
output_audio_tokens = assistant_speaking_seconds * 20

input_cost = input_audio_tokens / 1_000_000 * 32
output_cost = output_audio_tokens / 1_000_000 * 64
total = input_cost + output_cost
```

Ejemplo conservador de 1 minuto con 30 segundos de usuario y 30 segundos de asistente:

```text
input_audio_tokens = 30 * 10 = 300
output_audio_tokens = 30 * 20 = 600

input_cost = 300 / 1_000_000 * 32 = USD 0.0096
output_cost = 600 / 1_000_000 * 64 = USD 0.0384
total ~= USD 0.048 por minuto
```

Ejemplo de 5 minutos con esa dinamica:

```text
5 * 0.048 = USD 0.24
```

Caso peor simple: asistente habla durante todo el minuto:

```text
output_audio_tokens = 60 * 20 = 1200
output_cost = 1200 / 1_000_000 * 64 = USD 0.0768 por minuto
```

Estos calculos no incluyen:

- tokens de texto;
- transcripcion separada;
- tool calls;
- contexto acumulado;
- retries;
- precios futuros.

Los costos reales deben medirse con usage/logging de la API.

## Como mejorar el contexto

Contexto actual:

- perfil local;
- idioma;
- nombre preferido;
- recordatorios;
- instrucciones de seguridad;
- tono conversacional.

Mejoras recomendadas:

1. Incluir preferencias de comunicacion:
   - velocidad;
   - formalidad;
   - temas frecuentes;
   - nivel de detalle.

2. Agregar memoria corta de sesion:
   - ultimos temas;
   - recordatorios ya mencionados;
   - estado de la conversacion.

3. Agregar memoria persistente no sensible:
   - preferencias del usuario;
   - contactos frecuentes;
   - rutinas.

4. Pasar contexto resumido, no transcript completo:
   - menor costo;
   - menor latencia;
   - menor riesgo de arrastrar errores.

5. Separar contexto de seguridad:
   - reglas no medicas siempre presentes;
   - perfil y recordatorios como evidencia;
   - resumen de sesion como contexto dinamico.

## Como mejorar retroalimentacion del modelo

Pendiente principal: tools/function calling.

Hoy el modelo recibe recordatorios en el prompt, pero no llama herramientas reales. Para mejorar:

1. Exponer tool `list_reminders`.
2. Exponer tool `find_reminders`.
3. Exponer tool `log_conversation_event`.
4. Exponer tool `flag_safety_signal`.
5. Exponer tool `create_summary_draft`.

Ventajas:

- El modelo no necesita recordar todo en prompt.
- Se puede auditar que recordatorios consulto.
- Se puede generar resumen con evidencia estructurada.
- Se reducen alucinaciones.

Tambien conviene enviar feedback explicito por eventos:

- cuando el usuario habla pero no hay transcript;
- cuando VAD detecta voz;
- cuando VAD cierra turno;
- cuando el modelo responde;
- cuando hay error de dispositivo.

## Mejoras tecnicas recomendadas

### Backend

- Agregar endpoint para crear sesiones con perfil seleccionado, no solo demo default.
- Persistir transcript Realtime.
- Generar resumen Realtime al cortar llamada.
- Registrar usage/costo por sesion.
- Implementar tool calling para recordatorios.
- Agregar `OpenAI-Safety-Identifier` con un hash estable del usuario.
- Evitar stacktraces para errores esperados de OpenAI.
- Agregar tests de controller con gateway mock.

### Frontend

- Guardar ultimo microfono funcional en `localStorage`.
- Detectar `muted` y sugerir fallback automatico.
- Agregar boton `Probar siguiente micro`.
- Mostrar estado de conexion WebRTC:
  - `iceConnectionState`;
  - `connectionState`;
  - data channel state.
- Persistir eventos de diagnostico en modo debug.
- Agregar una pantalla compacta para demo publica.
- Mejorar Safari despues de estabilizar Chrome.

### Producto

- Agregar flujo de consentimiento.
- Explicar claramente que es AI.
- Agregar salida visible para familiares/cuidadores.
- Agregar modo de prueba sin microfono usando texto.
- Agregar limites de emergencia visibles.

## Known issues

- AirPods pueden aparecer como `live, enabled, muted`.
- Safari puede bloquear audio remoto hasta gesto explicito.
- La app aun no persiste transcript real de Realtime.
- El resumen Markdown actual corresponde a demo local, no a charla Realtime.
- No hay medicion real de usage/costo por sesion todavia.

## Checklist para continuar

1. Hacer que Chrome con microfono valido funcione end-to-end.
2. Guardar transcript Realtime.
3. Generar resumen al cortar llamada.
4. Agregar tools de recordatorios.
5. Agregar usage/cost telemetry.
6. Agregar fallback automatico de microfono.
7. Compatibilizar Safari.
8. Documentar una demo grabable de 2 minutos.
