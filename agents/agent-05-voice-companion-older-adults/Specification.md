# Specification: agent-05-voice-companion-older-adults

## Problema que resuelve
Muchas personas mayores viven solas o pasan largos periodos del dia sin interaccion social frecuente. Esta situacion puede generar sensacion de soledad, desconexion, baja estimulacion conversacional y dificultad para recordar actividades cotidianas simples como visitas, llamadas familiares o turnos no urgentes.

Al mismo tiempo, muchas soluciones digitales actuales dependen de pantallas, formularios, texto escrito o interfaces complejas. Para algunas personas mayores, esas interfaces pueden ser una barrera. La voz, en cambio, es una forma mas natural, cercana y accesible de interaccion.

Este agente busca ofrecer una herramienta de acompanamiento no medico basada en voz, capaz de escuchar, conversar, responder con calidez, seguir el humor cotidiano y recordar eventos simples previamente configurados.

El agente no reemplaza a familiares, cuidadores ni profesionales de salud. Funciona como una herramienta complementaria de compania, inclusion digital y apoyo cotidiano.

## Objetivo del MVP
Construir un acompanante de voz con inteligencia artificial para personas mayores que permita mantener una conversacion simple y calida, responder a historias o chistes, y recordar eventos cotidianos previamente configurados.

La primera version usable debe permitir:
- Iniciar una sesion de voz.
- Conversar con el agente en espanol.
- Mantener un tono paciente, respetuoso y no infantilizante.
- Consultar recordatorios simples cargados localmente.
- Simular avisos de eventos proximos, como una visita medica o una llamada familiar.
- Generar un resumen basico de la conversacion para un familiar o cuidador.
- Ejecutar una demo minima reproducible.

## Modelo AI
Proveedor: OpenAI  
API: Realtime API  
Modelo por defecto:

```bash
OPENAI_REALTIME_MODEL=gpt-realtime-2
```

Variables esperadas:

```bash
OPENAI_API_KEY=
OPENAI_REALTIME_MODEL=gpt-realtime-2
OPENAI_REALTIME_VOICE=marin
APP_PORT=3000
```

Variables opcionales para futuras extensiones:

```bash
AWS_REGION=us-east-1
BEDROCK_MODEL_ID=openai.gpt-oss-20b-1:0
```

Notas:
- El MVP principal estara basado en OpenAI Realtime API.
- Bedrock queda reservado como extension futura para analisis posterior, generacion de reportes o comparacion de proveedores.
- El modo local debe funcionar sin credenciales de AI usando respuestas simuladas.

## Alcance incluido
- Conversacion de voz en tiempo real con una persona mayor.
- Personalidad calida, paciente y respetuosa.
- Respuestas breves, claras y faciles de entender.
- Capacidad de reaccionar de forma amable ante chistes o historias.
- Recordatorios simples cargados desde un archivo local JSON.
- Consulta de proximos eventos.
- Simulacion de aviso de evento proximo.
- Generacion de resumen de conversacion en Markdown.
- Generacion opcional de resumen estructurado en JSON.
- Modo local sin AI para demo deterministica.
- Modo AI usando OpenAI Realtime API.
- Documentacion de costos estimados.
- Demo minima versionada en el repositorio.
- Prompt del agente documentado.
- Limites de seguridad explicitos: no medico, no diagnostico, no emergencias.

## Alcance excluido
- Diagnostico medico.
- Recomendaciones clinicas.
- Administracion de medicacion critica.
- Gestion real de emergencias.
- Reemplazo de familiares, cuidadores o profesionales.
- Integracion real con WhatsApp, llamadas telefonicas o SMS.
- Integracion real con calendarios externos.
- Login de usuarios.
- Base de datos persistente.
- Multiusuario.
- Panel avanzado para familiares.
- Deteccion clinica de estado emocional.
- Grabacion permanente de audio.
- Deploy productivo.
- Cumplimiento formal de normativas medicas o sanitarias.

## Inputs
- `--input`: archivo JSON con perfil de usuario, contactos familiares simulados y recordatorios.
- `--output`: archivo Markdown donde se escribira el resumen de conversacion.
- `--json-output`: archivo JSON opcional con resumen estructurado.
- `--ai`: activa el modo AI con OpenAI Realtime API.
- `--local`: ejecuta el flujo en modo local sin credenciales de AI.
- `--demo-script`: archivo opcional con una conversacion simulada para generar una demo reproducible.
- `--language`: idioma principal de la sesion. Por defecto `es-AR`.

Ejemplo de input:

```json
{
  "user": {
    "name": "Roberto",
    "preferredName": "Don Roberto",
    "language": "es-AR"
  },
  "familyContacts": [
    {
      "name": "Laura",
      "relation": "hija",
      "phone": "+54..."
    }
  ],
  "reminders": [
    {
      "id": "reminder-001",
      "title": "Visita con el medico",
      "date": "2026-05-15",
      "time": "16:00",
      "type": "appointment"
    },
    {
      "id": "reminder-002",
      "title": "Llamar a Laura",
      "date": "2026-05-15",
      "time": "19:00",
      "type": "family_call"
    }
  ]
}
```

## Outputs
- Conversacion de voz en tiempo real.
- Transcripcion parcial o completa si el modo lo permite.
- Resumen en Markdown para familiar o cuidador.
- JSON opcional con eventos consultados y temas mencionados.
- Logs minimos de ejecucion.
- Archivo demo versionado.

Archivos esperados:

```text
output/conversation-summary.md
output/conversation-summary.json
output/demo-transcript.md
```

## Formato de salida
```markdown
# Resumen de conversacion

## Usuario
Don Roberto

## Fecha
2026-05-15

## Estado general observado
La persona converso de forma tranquila y participativa.

## Temas mencionados
- Recuerdos familiares.
- Chiste compartido durante la conversacion.
- Consulta sobre visita medica.

## Recordatorios consultados
- Visita con el medico a las 16:00.
- Llamar a Laura a las 19:00.

## Senales relevantes
No se detectaron menciones explicitas de emergencia o riesgo.

## Posibles acciones sugeridas
- Confirmar que la persona recuerde la visita.
- Realizar una llamada familiar durante la tarde.
- Mantener los recordatorios actualizados.

## Nota de seguridad
Este resumen no constituye evaluacion medica, diagnostico ni recomendacion clinica.
```

Formato JSON opcional:

```json
{
  "user": "Don Roberto",
  "date": "2026-05-15",
  "topics": ["recuerdos familiares", "humor", "visita medica"],
  "remindersConsulted": [
    {
      "title": "Visita con el medico",
      "time": "16:00"
    }
  ],
  "riskSignals": [],
  "suggestedActions": ["Confirmar visita medica", "Llamar durante la tarde"],
  "medicalDisclaimer": true
}
```

## Flujo de ejecucion
1. Validar argumentos.
2. Cargar configuracion local del usuario desde `--input`.
3. Cargar recordatorios desde el mismo archivo o desde seccion dedicada.
4. Resolver modo de ejecucion: `--local` o `--ai`.
5. Si el modo es local, ejecutar conversacion simulada desde `--demo-script`.
6. Si `--ai` esta activo, validar credenciales de OpenAI.
7. Estimar costo aproximado de la sesion si aplica.
8. Pedir confirmacion si la estimacion supera USD 1.00.
9. Crear sesion realtime.
10. Iniciar conversacion por voz.
11. Aplicar instrucciones de personalidad, seguridad y limites no medicos.
12. Permitir consulta de recordatorios configurados.
13. Detectar eventos relevantes de la conversacion.
14. Sanitizar salida.
15. Generar resumen local en Markdown y JSON.
16. Escribir consola o archivo.
17. Finalizar sesion.

## Arquitectura esperada
Para este agente, la arquitectura recomendada es TypeScript por la naturaleza de WebRTC y la Realtime API.

```text
agents/agent-05-voice-companion-older-adults/
├── README.md
├── AGENTS.md
├── SKILLS.md
├── Specification.md
├── package.json
├── .env.example
├── examples/
│   ├── older-adult-profile.example.json
│   ├── reminders.example.json
│   └── demo-script.example.md
├── docs/
│   ├── architecture.md
│   ├── prompt.md
│   ├── safety.md
│   └── cost-notes.md
└── src/
    ├── application/
    │   ├── startVoiceCompanionSession.ts
    │   ├── generateConversationSummary.ts
    │   └── estimateSessionCost.ts
    ├── domain/
    │   ├── OlderAdultProfile.ts
    │   ├── Reminder.ts
    │   ├── ConversationSummary.ts
    │   ├── SafetySignal.ts
    │   └── CompanionSession.ts
    └── infrastructure/
        ├── input/
        │   ├── cli/
        │   │   └── main.ts
        │   └── web/
        │       ├── index.html
        │       ├── main.ts
        │       └── style.css
        ├── output/
        │   ├── MarkdownSummaryWriter.ts
        │   └── JsonSummaryWriter.ts
        └── ai/
            ├── OpenAIRealtimeClient.ts
            ├── LocalCompanionClient.ts
            └── CompanionPromptBuilder.ts
```

## Componentes principales
- `Main`: entrada CLI para ejecutar demo local, generar resumen o iniciar servidor.
- `VoiceCompanionRequest`: contrato de entrada con perfil, recordatorios y modo de ejecucion.
- `StartVoiceCompanionSessionUseCase`: orquestador principal de la sesion.
- `GenerateConversationSummary`: caso de uso para generar Markdown/JSON final.
- `EstimateSessionCost`: estimador previo para modo AI.
- `OlderAdultProfile`: modelo de dominio con nombre, idioma y preferencias basicas.
- `Reminder`: modelo de dominio para eventos simples.
- `ConversationSummary`: modelo de dominio del resumen final.
- `SafetySignal`: modelo para registrar senales no clinicas de alerta.
- `CompanionSession`: estado minimo de una sesion.
- `ReminderResolver`: busca recordatorios por fecha, hora o intencion.
- `LocalCompanionClient`: fallback deterministico sin AI.
- `OpenAIRealtimeClient`: cliente para sesion realtime con OpenAI.
- `CompanionPromptBuilder`: construye instrucciones del agente.
- `MarkdownSummaryWriter`: escribe resumen en Markdown.
- `JsonSummaryWriter`: escribe resumen estructurado en JSON.
- `CostEstimator`: estima costo aproximado de la sesion AI.

## Prompt base del agente
```text
You are a warm, patient and respectful voice companion for older adults.
Your purpose is to provide non-medical companionship, simple conversation, light humor and daily reminders.

Rules:
- Speak slowly and clearly.
- Use short, warm sentences.
- Do not sound childish.
- Do not use complex technical language.
- Do not provide medical diagnosis.
- Do not provide treatment advice.
- Do not replace family, caregivers or healthcare professionals.
- If the user seems sad, confused or distressed, respond with empathy and suggest contacting a trusted person.
- If the user mentions danger, self-harm, severe pain, a fall, chest pain, breathing difficulty, or an emergency, tell them to contact emergency services or a trusted caregiver immediately.
- You may remind the user about configured events such as visits, calls, meals or appointments.
- You may laugh gently at jokes and encourage positive conversation.
- You must never pretend to be a real family member.
- You must be transparent that you are an AI voice companion.
```

## Criterios de aceptacion
- El modo local funciona sin credenciales.
- El modo AI documenta proveedor, modelo y costo.
- El comando principal genera un output util.
- La demo queda versionada.
- Los errores principales son claros.
- Hay tests para parser, use case, output y prompt builder si aplica.
- El agente puede cargar un perfil local de persona mayor.
- El agente puede cargar recordatorios desde JSON.
- El agente puede responder una consulta sobre un recordatorio.
- El agente puede generar un resumen en Markdown.
- El agente incluye limites de seguridad no medicos.
- El agente no da diagnosticos ni recomendaciones clinicas.
- El README explica el impacto social del proyecto.
- El README explica por que la voz mejora la accesibilidad.
- La demo muestra al menos tres escenas:
  - conversacion cotidiana;
  - humor o chiste;
  - recordatorio de visita o actividad.

## Decisiones tecnicas
- Lenguaje: TypeScript.
- Build tool: Vite para frontend y `tsx`/`esbuild` para backend.
- Interfaz: web app minima con boton para iniciar sesion de voz y CLI para demo local.
- AI: OpenAI Realtime API usando `gpt-realtime-2`.
- Salida: Markdown y JSON.
- Arquitectura: Clean Architecture simplificada por carpetas `application`, `domain` e `infrastructure`.
- Modo local: cliente deterministico para demo sin credenciales.
- Persistencia: archivos locales JSON.
- Deploy: no incluido en MVP.
- Seguridad: limites explicitos para evitar uso medico o emergencias.
- Privacidad: no guardar audio por defecto.

## Costos
El costo depende de la duracion de la sesion, cantidad de audio de entrada, cantidad de audio de salida y tokens procesados por el modelo.

Referencia oficial:
- OpenAI pricing: https://openai.com/api/pricing/
- OpenAI Realtime cost guide: https://platform.openai.com/docs/guides/realtime-costs

Precio de referencia observado para `gpt-realtime-2`:
- Audio input: USD 32.00 por 1M tokens.
- Audio output: USD 64.00 por 1M tokens.
- Text input: USD 4.00 por 1M tokens.
- Text output: USD 24.00 por 1M tokens.

Estimacion inicial para demo:
- Sesion corta de 2 a 3 minutos.
- Costo esperado: normalmente menor a USD 0.10 por demo.
- El valor exacto depende del precio vigente, duracion del audio, turnos conversacionales y cantidad de respuesta generada.

Guardrail:
- Si la estimacion previa supera USD 1.00, la CLI debe pedir confirmacion antes de iniciar la sesion AI.

Modo local:
- Costo: USD 0.00.
- Requiere credenciales: No.

## Demo
Comando para demo local:

```bash
npm run demo:local -- \
  --input examples/older-adult-profile.example.json \
  --demo-script examples/demo-script.example.md \
  --output output/conversation-summary.md
```

Comando para modo AI:

```bash
npm run start -- \
  --input examples/older-adult-profile.example.json \
  --output output/conversation-summary.md \
  --ai
```

Comando para iniciar web app:

```bash
npm run dev
```

Archivo demo esperado:

```text
output/conversation-summary.md
```

## Futuras extensiones
- Integracion con Google Calendar.
- Integracion con WhatsApp para avisar a familiares.
- Panel simple para familiares o cuidadores.
- Recordatorios recurrentes.
- Modo llamada telefonica.
- Deteccion basica de palabras de alerta.
- Generacion automatica de notas para cuidadores.
- Integracion con centros de jubilados u organizaciones comunitarias.
- Soporte multiusuario.
- Soporte bilingue espanol/italiano o espanol/ingles.
- Integracion con AWS Bedrock para resumen posterior.
- Exportacion de metricas de uso no sensibles.
- Modo offline parcial con respuestas predefinidas.
- Adaptacion de voz, velocidad y tono segun preferencia del usuario.
- Validacion con usuarios reales bajo consentimiento informado.
