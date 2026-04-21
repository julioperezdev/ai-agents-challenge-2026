#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"

JAVA_25_HOME="$(/usr/libexec/java_home -v 25 2>/dev/null || true)"

if [[ -n "${JAVA_25_HOME}" ]]; then
  export JAVA_HOME="${JAVA_25_HOME}"
fi

if [[ -z "${JAVA_HOME:-}" ]]; then
  echo "Error: no se encontró Java 25. Configura JAVA_HOME apuntando a un JDK 25." >&2
  exit 1
fi

export PATH="${JAVA_HOME}/bin:${PATH}"

cd "${ROOT_DIR}"
mvn -q -DskipTests package
java -jar "${ROOT_DIR}/target/agent-01-commit-summarizer-0.1.0.jar" "$@"
