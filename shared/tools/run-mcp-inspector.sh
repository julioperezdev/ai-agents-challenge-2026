#!/usr/bin/env bash
set -euo pipefail

if [[ $# -lt 1 ]]; then
  echo "Uso: $0 <ruta-del-agente-mcp>" >&2
  exit 1
fi

AGENT_DIR_INPUT="$1"

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
REPO_ROOT="$(cd "${SCRIPT_DIR}/../.." && pwd)"

if [[ "${AGENT_DIR_INPUT}" = /* ]]; then
  AGENT_DIR="${AGENT_DIR_INPUT}"
else
  AGENT_DIR="${REPO_ROOT}/${AGENT_DIR_INPUT}"
fi

AGENT_DIR="$(cd "${AGENT_DIR}" && pwd)"
RUN_SCRIPT="${AGENT_DIR}/run.sh"
ENV_FILE="${AGENT_DIR}/.env"

if [[ ! -d "${AGENT_DIR}" ]]; then
  echo "Error: no existe la carpeta del agente: ${AGENT_DIR}" >&2
  exit 1
fi

if [[ ! -x "${RUN_SCRIPT}" ]]; then
  echo "Error: no existe un run.sh ejecutable en ${AGENT_DIR}" >&2
  exit 1
fi

if ! command -v npx >/dev/null 2>&1; then
  echo "Error: se requiere npx para abrir MCP Inspector." >&2
  exit 1
fi

if [[ -f "${ENV_FILE}" ]]; then
  set -a
  # shellcheck disable=SC1090
  source "${ENV_FILE}"
  set +a
fi

echo "Abriendo MCP Inspector para: ${AGENT_DIR}"
echo "Inspector publicará su URL exacta en la terminal."
echo "Comando MCP: ${RUN_SCRIPT}"

cd "${AGENT_DIR}"
exec npx -y @modelcontextprotocol/inspector -- "${RUN_SCRIPT}"
