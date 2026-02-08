#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$ROOT_DIR"

docker compose down --remove-orphans

BUDGET_SEED_CURRENT_YEAR=true
if [[ "${1:-}" == "--no-seed" ]]; then
  BUDGET_SEED_CURRENT_YEAR=false
fi

export BUDGET_SEED_CURRENT_YEAR
docker compose up --build
