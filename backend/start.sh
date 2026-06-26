#!/usr/bin/env bash
set -euo pipefail

MODE="${1:-dev}"
ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "$ROOT_DIR"

case "$MODE" in
  dev)
    mvn spring-boot:run -Dspring-boot.run.profiles=dev
    ;;
  build)
    mvn clean package
    ;;
  *)
    echo "Usage: ./start.sh [dev|build]" >&2
    exit 2
    ;;
esac
