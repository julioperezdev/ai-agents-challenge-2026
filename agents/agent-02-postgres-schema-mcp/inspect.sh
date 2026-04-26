#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
REPO_ROOT="$(cd "${ROOT_DIR}/../.." && pwd)"

export APP_AI_PROVIDER="${APP_AI_PROVIDER:-mock}"

cd "${ROOT_DIR}"
docker compose up -d >/dev/null

exec "${REPO_ROOT}/shared/tools/run-mcp-inspector.sh" "${ROOT_DIR}"
