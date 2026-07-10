#!/usr/bin/env sh
set -eu

ROOT_DIR=$(CDPATH= cd -- "$(dirname -- "$0")" && pwd)
cd "$ROOT_DIR"

COMMAND=${1:-dev}
APP_MODULE=${APP_MODULE:-app.main:app}
APP_HOST=${APP_HOST:-0.0.0.0}
APP_PORT=${APP_PORT:-9001}
PYTHON_BIN=${PYTHON_BIN:-python}
VENV_DIR=${VENV_DIR:-.venv}

if [ -x "$VENV_DIR/Scripts/python.exe" ]; then
  PYTHON="$VENV_DIR/Scripts/python.exe"
elif [ -x "$VENV_DIR/bin/python" ]; then
  PYTHON="$VENV_DIR/bin/python"
else
  PYTHON="$PYTHON_BIN"
fi

case "$COMMAND" in
  dev)
    exec "$PYTHON" -m uvicorn "$APP_MODULE" --host "$APP_HOST" --port "$APP_PORT" --reload
    ;;
  prod)
    exec "$PYTHON" -m uvicorn "$APP_MODULE" --host "$APP_HOST" --port "$APP_PORT"
    ;;
  build)
    "$PYTHON" -m compileall -q app tests
    "$PYTHON" -m ruff check app tests
    "$PYTHON" -m pytest -q
    ;;
  *)
    printf 'Usage: %s [dev|prod|build]\n' "$0" >&2
    exit 2
    ;;
esac
