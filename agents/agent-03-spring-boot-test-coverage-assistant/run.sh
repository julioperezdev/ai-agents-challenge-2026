#!/usr/bin/env bash
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "$SCRIPT_DIR"

mvn -q -DskipTests package
java -jar target/agent-03-spring-boot-test-coverage-assistant-0.1.0.jar "$@"
