# Specification: <agent-name>

## Problema que resuelve
<Describe el problema concreto y por que duele.>

## Objetivo del MVP
<Describe que debe lograr el agente en su primera version usable.>

## Modelo AI
<Proveedor/modelo si aplica. Si no aplica, indicar "No aplica".>

Variables esperadas:

```bash
AWS_REGION=us-east-1
BEDROCK_MODEL_ID=openai.gpt-oss-20b-1:0
```

## Alcance incluido
- <Item>

## Alcance excluido
- <Item>

## Inputs
- `--input`: <descripcion>
- `--repo-path`: <opcional si aplica>
- `--output`: <archivo de salida opcional>
- `--ai`: <si aplica>

## Outputs
- <Archivo, Markdown, JSON, consola, etc.>

## Formato de salida
```markdown
# <Title>

## Summary
...
```

## Flujo de ejecucion
1. Validar argumentos.
2. Resolver contexto local.
3. Ejecutar caso de uso.
4. Generar salida local.
5. Si `--ai` esta activo, estimar costo y pedir confirmacion si supera USD 1.00.
6. Invocar AI si corresponde.
7. Sanitizar salida.
8. Escribir consola o archivo.

## Arquitectura esperada
```text
src/main/java/<base-package>/
├── application/
├── domain/
└── infrastructure/
    ├── input/
    │   └── cli/
    ├── output/
    └── ai/
```

## Componentes principales
- `<Main>`: entrada CLI.
- `<Request>`: contrato de entrada.
- `<UseCase>`: orquestador principal.
- `<DomainModel>`: modelo de dominio.
- `<LocalWriter>`: fallback deterministico.
- `<BedrockWriter>`: enriquecimiento AI opcional.

## Criterios de aceptacion
- El modo local funciona sin credenciales.
- El modo AI documenta proveedor/modelo/costo.
- El comando principal genera output util.
- La demo queda versionada.
- Los errores principales son claros.
- Hay tests para parser, use case, output y prompt builder si aplica.

## Decisiones tecnicas
- Lenguaje:
- Build tool:
- Interfaz:
- AI:
- Salida:
- Arquitectura:

## Costos
<Indicar precio de referencia, estimacion por ejecucion y guardrail.>

## Demo
<Comando reproducible y archivo demo esperado.>

## Futuras extensiones
- <Item>
