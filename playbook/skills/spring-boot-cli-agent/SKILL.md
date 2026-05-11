---
name: spring-boot-cli-agent
description: Use this skill when creating a Spring Boot CLI agent with layered architecture, local execution, tests, and run.sh.
compatibility: Java 17+, Maven, Spring Boot CLI agents.
metadata:
  author: julio-perez
  version: "0.1"
---

# Spring Boot CLI Agent

## Proposito
Estandarizar agentes CLI construidos con Spring Boot.

## Checklist
- [ ] Crear `pom.xml` con Spring Boot.
- [ ] Crear `run.sh` con `clean package`.
- [ ] Crear `application`, `domain`, `infrastructure/input/cli`, `infrastructure/output`.
- [ ] Implementar parser CLI con errores claros.
- [ ] Implementar modo local sin credenciales.
- [ ] Agregar tests para parser y output.
- [ ] Documentar comando principal.

## Reglas
- `CommandLineRunner` debe delegar al caso de uso.
- `application` no debe depender de detalles de CLI.
- Los adapters externos viven en `infrastructure`.
- El README debe mostrar primero el comando recomendado.
