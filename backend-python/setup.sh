#!/usr/bin/env sh
set -eu

ROOT_DIR=$(CDPATH= cd -- "$(dirname -- "$0")" && pwd)
cd "$ROOT_DIR"

PYTHON_VERSION=${PYTHON_VERSION:-3.11}
VENV_DIR=${VENV_DIR:-.venv}

if ! command -v uv >/dev/null 2>&1; then
  printf 'uv is required. Install it first: https://docs.astral.sh/uv/getting-started/installation/\n' >&2
  exit 1
fi

uv venv "$VENV_DIR" --python "$PYTHON_VERSION"
uv pip install --python "$VENV_DIR" -r requirements.txt
