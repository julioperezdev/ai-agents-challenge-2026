#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"

if [[ -z "${JAVA_HOME:-}" ]]; then
  if JAVA_CANDIDATE="$(/usr/libexec/java_home -v 21 2>/dev/null)"; then
    export JAVA_HOME="$JAVA_CANDIDATE"
    export PATH="$JAVA_HOME/bin:$PATH"
  fi
fi

cd "$ROOT_DIR"
./mvnw -q spring-boot:run
