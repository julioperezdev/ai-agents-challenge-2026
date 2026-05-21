# Cost Notes

## Modo local
Costo: USD 0.00.  
No requiere credenciales.

## Modo AI previsto
Proveedor: OpenAI  
API: Realtime API  
Modelo por defecto: `gpt-realtime-2`.  
Reasoning por defecto: `low`, recomendado para empezar con baja latencia.
Voz por defecto: `marin`

Variables:

```bash
OPENAI_API_KEY=
OPENAI_REALTIME_MODEL=gpt-realtime-2
OPENAI_REALTIME_VOICE=marin
OPENAI_REALTIME_REASONING_EFFORT=low
APP_PORT=3000
```

## Precios de referencia
Los precios deben verificarse antes de una demo real:
- Audio input: USD 32.00 por 1M tokens.
- Audio output: USD 64.00 por 1M tokens.
- Text input: USD 4.00 por 1M tokens.
- Text output: USD 24.00 por 1M tokens.

Ver calculos por minuto en `docs/realtime-integration.md`.

## Guardrail
Antes de iniciar una sesion AI larga se debe estimar costo. Si supera USD 1.00, la UI o el backend deben pedir confirmacion.

La UI web usa WebRTC y negocia la sesion por `POST /api/realtime/session`. La API key solo debe estar en el backend mediante `OPENAI_API_KEY`.
