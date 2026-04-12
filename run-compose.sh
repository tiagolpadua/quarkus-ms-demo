#!/usr/bin/env sh

set -eu

echo "Gerando artefatos de produção..."
./run-build-prod.sh

echo "Subindo stack com docker compose..."
docker compose up --build "$@"
