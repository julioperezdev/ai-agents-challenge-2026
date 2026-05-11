# agent-05-voice-companion-older-adults

Agente de acompanamiento por voz para personas mayores. Permite una conversacion calida y no medica, consulta recordatorios simples cargados localmente y genera un resumen para familiares o cuidadores.

## Que resuelve
Muchas personas mayores pasan largos periodos sin interaccion social frecuente. Este agente explora una interfaz mas natural y accesible: la voz.

El objetivo no es reemplazar familiares, cuidadores ni profesionales de salud. El agente funciona como apoyo cotidiano, compania ligera e inclusion digital.

## Enfoque
- Web app minima para iniciar una sesion de voz.
- CLI para demo local reproducible.
- Modo local sin credenciales usando conversacion simulada.
- Modo AI con OpenAI Realtime API.
- Recordatorios simples desde JSON.
- Resumen final en Markdown y JSON.
- Limites explicitos: no diagnostico, no recomendaciones clinicas, no emergencias.

## Comando principal
Demo local sin costo de AI:

```bash
npm run demo:local -- \
  --input examples/older-adult-profile.example.json \
  --demo-script examples/demo-script.example.md \
  --output output/conversation-summary.md
```

Modo AI:

```bash
npm run start -- \
  --input examples/older-adult-profile.example.json \
  --output output/conversation-summary.md \
  --ai
```

Web app:

```bash
npm run dev
```

## Modo AI
Proveedor: OpenAI  
API: Realtime API  
Modelo por defecto: `gpt-realtime-2`

Variables esperadas:

```bash
export OPENAI_API_KEY=
export OPENAI_REALTIME_MODEL=gpt-realtime-2
export OPENAI_REALTIME_VOICE=marin
export APP_PORT=3000
```

El modo AI debe:
- validar credenciales antes de iniciar,
- estimar costo de sesion,
- pedir confirmacion si la estimacion supera USD 1.00,
- iniciar sesion realtime,
- aplicar prompt de personalidad y seguridad,
- generar resumen al finalizar.

## Modo local
El modo local funciona sin credenciales y usa un guion de demo:

```bash
npm run demo:local -- \
  --input examples/older-adult-profile.example.json \
  --demo-script examples/demo-script.example.md \
  --output output/conversation-summary.md
```

Sirve para:
- probar parsing de inputs,
- validar recordatorios,
- generar resumen,
- crear una demo reproducible,
- mostrar el impacto del agente sin costo de AI.

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
- Si la estimacion previa supera USD 1.00, la CLI debe pedir confirmacion antes de iniciar la sesion AI.
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
Demo esperada:

```text
output/conversation-summary.md
```

Escenas minimas:
- conversacion cotidiana;
- humor o chiste;
- recordatorio de visita o actividad.

## Estructura prevista
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
└── src/
    ├── application/
    ├── domain/
    └── infrastructure/
        ├── input/
        ├── output/
        └── ai/
```

## Estado actual
Base documental inicial creada:
- `README.md`
- `AGENTS.md`
- `SKILLS.md`
- `Specification.md`

## Roadmap
1. Crear estructura TypeScript/Vite.
2. Implementar CLI local.
3. Crear ejemplos JSON y demo script.
4. Implementar modelos de dominio.
5. Implementar resumen Markdown/JSON.
6. Implementar cliente local deterministico.
7. Integrar OpenAI Realtime API.
8. Crear web app minima.
9. Agregar estimador de costos.
10. Versionar demo final.
