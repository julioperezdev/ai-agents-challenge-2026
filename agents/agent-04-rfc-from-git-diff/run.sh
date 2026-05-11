#!/usr/bin/env bash
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "$SCRIPT_DIR"

./mvnw -q -DskipTests clean package 2>/dev/null || mvn -q -DskipTests clean package

java -jar target/agent-04-rfc-from-git-diff-0.1.0.jar "$@"
