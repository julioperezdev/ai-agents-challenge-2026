# SKILLS.md

Este archivo define skills locales esperadas para implementar y mantener el agente 05.

## voice-companion-session
Use this skill when implementing or changing the voice companion session flow.

Checklist:
- [ ] Cargar perfil local.
- [ ] Cargar recordatorios.
- [ ] Resolver modo `--local` o `--ai`.
- [ ] Aplicar prompt de personalidad.
- [ ] Aplicar limites de seguridad no medicos.
- [ ] Permitir consulta de recordatorios.
- [ ] Capturar eventos relevantes para resumen.
- [ ] Finalizar con Markdown/JSON.

Rules:
- La sesion no debe guardar audio por defecto.
- La voz debe ser paciente, clara y no infantilizante.
- El agente debe decir que es AI si corresponde.

## non-medical-safety
Use this skill when writing prompts, summaries, responses or demos that mention health, risk or emergencies.

Checklist:
- [ ] No diagnosticar.
- [ ] No recomendar tratamientos.
- [ ] No ajustar medicacion.
- [ ] No reemplazar profesionales.
- [ ] Incluir disclaimer en resumen.
- [ ] Derivar emergencias a servicios de emergencia o persona de confianza.

Emergency examples:
- caida,
- dolor de pecho,
- dificultad para respirar,
- dolor severo,
- autolesion,
- peligro inmediato,
- confusion severa.

## reminder-companion
Use this skill when implementing reminder loading, lookup or response behavior.

Checklist:
- [ ] Leer recordatorios desde JSON.
- [ ] Resolver proximos eventos por fecha/hora.
- [ ] Responder de forma breve y clara.
- [ ] No tratar recordatorios como instrucciones clinicas.
- [ ] Registrar recordatorios consultados para el resumen.

## conversation-summary
Use this skill when generating Markdown or JSON summaries for caregivers/family.

Checklist:
- [ ] Incluir usuario y fecha.
- [ ] Incluir temas mencionados.
- [ ] Incluir recordatorios consultados.
- [ ] Incluir senales relevantes no clinicas.
- [ ] Incluir posibles acciones sugeridas.
- [ ] Incluir nota de seguridad.

Rules:
- No usar lenguaje clinico concluyente.
- No afirmar diagnosticos.
- No exagerar riesgo si no hay evidencia.
- Si no hubo senales relevantes, decirlo claramente.

## realtime-cost-control
Use this skill when implementing OpenAI Realtime API usage.

Checklist:
- [ ] Documentar modelo y precios.
- [ ] Estimar costo antes de iniciar sesion AI.
- [ ] Pedir confirmacion si supera USD 1.00.
- [ ] Permitir modo local USD 0.00.
- [ ] Registrar duracion aproximada de demo.

References:
- https://openai.com/api/pricing/
- https://platform.openai.com/docs/guides/realtime-costs
