#!/usr/bin/env sh

set -eu

IMAGE_NAME="${IMAGE_NAME:-quarkus-ms-demo:jvm}"
CONTAINER_NAME="${CONTAINER_NAME:-quarkus-ms-demo}"
HOST_PORT="${HOST_PORT:-8080}"

if [ ! -d "target/quarkus-app" ]; then
  echo "Artefatos de produção não encontrados em target/quarkus-app."
  echo "Executando build de produção antes de iniciar o container..."
  ./run-build-prod.sh
fi

docker build -f src/main/docker/Dockerfile.jvm -t "$IMAGE_NAME" .
docker run --rm --name "$CONTAINER_NAME" -p "${HOST_PORT}:8080" "$IMAGE_NAME"
