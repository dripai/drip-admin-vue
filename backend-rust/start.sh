#!/usr/bin/env bash
set -euo pipefail

cd "$(dirname "$0")"

MODE="${1:-dev}"

case "$MODE" in
  dev)
    exec cargo run
    ;;
  build)
    cargo build --release
    echo "Built: $PWD/target/release/drip-admin-rust"
    ;;
  *)
    echo "Usage: ./start.sh [dev|build]" >&2
    exit 2
    ;;
esac
