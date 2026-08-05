#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$ROOT_DIR"

if [ ! -f .env.example ]; then
  echo "Error: no se encontro .env.example en la raiz del proyecto." >&2
  exit 1
fi

cp .env.example .env
echo "==> .env generado desde .env.example"

docker compose --env-file .env up -d --build "$@"

echo "==> Stack levantado. API: http://localhost:${SERVER_PORT:-8080}"
echo "==> Swagger: http://localhost:${SERVER_PORT:-8080}/swagger-ui.html"
