#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$ROOT_DIR"

SPRING_ARGS=("--budget.seed-current-year=true")
if [[ "${1:-}" == "--no-seed" ]]; then
  SPRING_ARGS=()
fi

if [ ! -d "frontend/node_modules" ]; then
  npm --prefix frontend install
fi

SPRING_BOOT_ARGS="${SPRING_ARGS[*]:-}"
./mvnw spring-boot:run ${SPRING_BOOT_ARGS:+-Dspring-boot.run.arguments="$SPRING_BOOT_ARGS"} &
BACKEND_PID=$!

npm --prefix frontend run dev &
FRONTEND_PID=$!

cleanup() {
  kill "$BACKEND_PID" "$FRONTEND_PID" 2>/dev/null || true
}
trap cleanup EXIT INT TERM

wait
