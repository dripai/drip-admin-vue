#!/usr/bin/env bash
set -euo pipefail

cd "$(dirname "$0")"

MODE="${1:-dev}"
GO="${GO:-/d/tool/go/bin/go.exe}"
APP="$PWD/bin/drip-admin-go.exe"

require_go() {
  if [ ! -x "$GO" ]; then
    echo "Go not found: $GO" >&2
    exit 1
  fi
}

use_config() {
  export DRIP_GO_CONFIG="${DRIP_GO_CONFIG:-$PWD/config.yaml}"
  if [ ! -f "$DRIP_GO_CONFIG" ]; then
    echo "Config file not found: $DRIP_GO_CONFIG" >&2
    exit 1
  fi
}

case "$MODE" in
  dev)
    require_go
    use_config
    exec "$GO" run ./cmd/server
    ;;
  prod)
    use_config
    if [ ! -x "$APP" ]; then
      echo "Binary not found: $APP" >&2
      echo "Run ./start.sh build first." >&2
      exit 1
    fi
    exec "$APP"
    ;;
  build)
    require_go
    mkdir -p "$PWD/bin"
    "$GO" build -trimpath -ldflags="-s -w" -o "$APP" ./cmd/server
    echo "Built: $APP"
    ;;
  *)
    echo "Usage: ./start.sh [dev|prod|build]" >&2
    exit 2
    ;;
esac
