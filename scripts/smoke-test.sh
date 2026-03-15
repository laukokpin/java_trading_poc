#!/usr/bin/env bash
set -euo pipefail

COMPOSE_FILE="$(cd "$(dirname "$0")/.." && pwd)/docker-compose.yml"
APP_URL="http://localhost:8080"
MAX_WAIT=90

cleanup() {
  echo "[smoke] Tearing down containers..."
  docker compose -f "$COMPOSE_FILE" down --volumes --remove-orphans
}
trap cleanup EXIT

echo "[smoke] Building and starting containers..."
docker compose -f "$COMPOSE_FILE" up -d --build

echo "[smoke] Waiting for app to become healthy (max ${MAX_WAIT}s)..."
elapsed=0
until curl -sf "${APP_URL}/api/v1/system/live" > /dev/null 2>&1; do
  if [[ $elapsed -ge $MAX_WAIT ]]; then
    echo "[smoke] FAIL: app did not become healthy within ${MAX_WAIT}s"
    docker compose -f "$COMPOSE_FILE" logs app
    exit 1
  fi
  sleep 3
  elapsed=$((elapsed + 3))
done
echo "[smoke] App is healthy after ${elapsed}s"

echo "[smoke] Checking /api/v1/system/ready..."
curl -sf "${APP_URL}/api/v1/system/ready" > /dev/null
echo "[smoke] Ready endpoint OK"

echo "[smoke] Posting a test order..."
RESPONSE=$(curl -sf -X POST "${APP_URL}/api/v1/trading/orders" \
  -H "Content-Type: application/json" \
  -d '{
    "orderId": "smoke-001",
    "symbol": "GBPUSD",
    "side": "BUY",
    "quantity": 1000000,
    "timestamp": "2026-03-15T10:00:00Z"
  }')

echo "[smoke] Response: $RESPONSE"

ORDER_ID=$(echo "$RESPONSE" | grep -o '"orderId":"[^"]*"' | cut -d'"' -f4 || true)
if [[ -z "$ORDER_ID" ]]; then
  echo "[smoke] FAIL: orderId not found in response"
  exit 1
fi
echo "[smoke] orderId in response: $ORDER_ID"

echo "[smoke] Posting a test quote..."
curl -sf -X POST "${APP_URL}/api/v1/trading/quotes" \
  -H "Content-Type: application/json" \
  -d '{
    "symbol": "EURUSD",
    "bid": 1.0850,
    "ask": 1.0852,
    "timestamp": "2026-03-15T10:00:00Z"
  }' > /dev/null
echo "[smoke] Quote accepted"

echo "[smoke] ALL CHECKS PASSED"
