#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "$0")/.." && pwd)"
PROFILE_ARGS=()

if [[ "${1:-}" == "--profile" ]]; then
  if [[ -z "${2:-}" ]]; then
    echo "Missing profile name after --profile" >&2
    exit 1
  fi
  PROFILE_ARGS=(--profile "$2")
fi

"$ROOT_DIR/scripts/run-gc-baseline.sh" --jvm-profile baseline "${PROFILE_ARGS[@]}"
"$ROOT_DIR/scripts/run-gc-baseline.sh" --jvm-profile tuned "${PROFILE_ARGS[@]}"