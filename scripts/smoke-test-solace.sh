#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "$0")/.." && pwd)"
COMPOSE_FILE="$ROOT_DIR/docker-compose.yml"
APP_URL="http://localhost:8081"
MAX_WAIT=150

cleanup() {
  echo "[solace-smoke] Tearing down Solace stack..."
  docker compose -f "$COMPOSE_FILE" rm -sf app-solace solace >/dev/null 2>&1 || true
}
trap cleanup EXIT

echo "[solace-smoke] Building project classes..."
pushd "$ROOT_DIR" >/dev/null
export SDKMAN_DIR="${SDKMAN_DIR:-$HOME/.sdkman}"
set +u
source "$HOME/.sdkman/bin/sdkman-init.sh"
sdk env >/dev/null
set -u
mvn -q -DskipTests package dependency:build-classpath -Dmdep.outputFile=target/solace.classpath
popd >/dev/null

echo "[solace-smoke] Starting Solace broker and app-solace..."
docker compose -f "$COMPOSE_FILE" up -d --build solace app-solace

echo "[solace-smoke] Waiting for app-solace health endpoint (max ${MAX_WAIT}s)..."
elapsed=0
until curl -sf "${APP_URL}/api/v1/system/live" >/dev/null 2>&1; do
  if [[ $elapsed -ge $MAX_WAIT ]]; then
    echo "[solace-smoke] FAIL: app-solace did not become healthy within ${MAX_WAIT}s"
    docker compose -f "$COMPOSE_FILE" logs app-solace solace
    exit 1
  fi
  sleep 5
  elapsed=$((elapsed + 5))
done

echo "[solace-smoke] Health endpoint OK after ${elapsed}s"
echo "[solace-smoke] Verifying quote endpoint..."
curl -sf -X POST "${APP_URL}/api/v1/trading/quotes" \
  -H "Content-Type: application/json" \
  -d '{
    "symbol": "EURUSD",
    "bid": 1.0810,
    "ask": 1.0812,
    "timestamp": "2026-03-15T00:00:00Z"
  }' >/dev/null

echo "[solace-smoke] Running direct Solace round-trip CLI..."
CLASSPATH="target/classes:$(cat "$ROOT_DIR/target/solace.classpath")"
pushd "$ROOT_DIR" >/dev/null
java -cp "$CLASSPATH" com.kp.trading.solace.SolaceRoundTripCli
popd >/dev/null

echo "[solace-smoke] ALL CHECKS PASSED"
