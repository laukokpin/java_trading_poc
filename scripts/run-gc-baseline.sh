#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "$0")/.." && pwd)"
RESULTS_DIR="$ROOT_DIR/artifacts/gc"
JAR_PATH="$ROOT_DIR/target/java-trading-poc-0.0.1-SNAPSHOT.jar"
APP_LOG="$RESULTS_DIR/app.out"
APP_PID_FILE="$RESULTS_DIR/app.pid"
BASE_URL="http://localhost:8080"
PROFILES=(low medium high)
JVM_PROFILE="baseline"

usage() {
  echo "Usage: $0 [--profile low|medium|high] [--jvm-profile baseline|tuned]" >&2
}

while [[ $# -gt 0 ]]; do
  case "$1" in
    --profile)
      if [[ -z "${2:-}" ]]; then
        echo "Missing profile name after --profile" >&2
        usage
        exit 1
      fi
      PROFILES=("$2")
      shift 2
      ;;
    --jvm-profile)
      if [[ -z "${2:-}" ]]; then
        echo "Missing JVM profile name after --jvm-profile" >&2
        usage
        exit 1
      fi
      JVM_PROFILE="$2"
      shift 2
      ;;
    *)
      echo "Unsupported argument: $1" >&2
      usage
      exit 1
      ;;
  esac
done

case "$JVM_PROFILE" in
  baseline)
    JVM_FLAGS=(
      -Xms256m
      -Xmx512m
      -XX:+UseG1GC
      -XX:MaxGCPauseMillis=200
    )
    GC_LOG="$RESULTS_DIR/g1gc-baseline.log"
    APP_LOG="$RESULTS_DIR/app-baseline.out"
    ;;
  tuned)
    JVM_FLAGS=(
      -Xms512m
      -Xmx512m
      -XX:+UseG1GC
      -XX:MaxGCPauseMillis=100
      -XX:InitiatingHeapOccupancyPercent=30
    )
    GC_LOG="$RESULTS_DIR/g1gc-tuned.log"
    APP_LOG="$RESULTS_DIR/app-tuned.out"
    ;;
  *)
    echo "Unsupported JVM profile: $JVM_PROFILE" >&2
    usage
    exit 1
    ;;
esac

if [[ -f "$APP_PID_FILE" ]]; then
  rm -f "$APP_PID_FILE"
fi

if [[ -f "$APP_LOG" ]]; then
  rm -f "$APP_LOG"
fi

if [[ -f "$GC_LOG" ]]; then
  rm -f "$GC_LOG"
fi

for profile in "${PROFILES[@]}"; do
  rm -f "$RESULTS_DIR/${JVM_PROFILE}-${profile}.json"
done

cleanup() {
  if [[ -f "$APP_PID_FILE" ]]; then
    APP_PID="$(cat "$APP_PID_FILE")"
    if kill -0 "$APP_PID" >/dev/null 2>&1; then
      kill "$APP_PID" >/dev/null 2>&1 || true
      wait "$APP_PID" 2>/dev/null || true
    fi
    rm -f "$APP_PID_FILE"
  fi
}
trap cleanup EXIT

mkdir -p "$RESULTS_DIR"

export SDKMAN_DIR="${SDKMAN_DIR:-$HOME/.sdkman}"
set +u
source "$HOME/.sdkman/bin/sdkman-init.sh"
sdk env >/dev/null
set -u

pushd "$ROOT_DIR" >/dev/null
mvn -q -DskipTests package

java \
  "${JVM_FLAGS[@]}" \
  -Xlog:gc*:file="$GC_LOG":time,uptime,level,tags \
  -jar "$JAR_PATH" >"$APP_LOG" 2>&1 &
APP_PID=$!
echo "$APP_PID" >"$APP_PID_FILE"

for _ in $(seq 1 60); do
  if curl -sf "$BASE_URL/api/v1/system/live" >/dev/null 2>&1; then
    break
  fi
  sleep 2
done

if ! curl -sf "$BASE_URL/api/v1/system/live" >/dev/null 2>&1; then
  echo "Application failed to become healthy; see $APP_LOG" >&2
  exit 1
fi

for profile in "${PROFILES[@]}"; do
  output_file="$RESULTS_DIR/${JVM_PROFILE}-${profile}.json"
  echo "Running JVM profile: $JVM_PROFILE, load profile: $profile"
  java -cp target/classes com.kp.trading.perf.TradingLoadGenerator \
    --profile "$profile" \
    --base-url "$BASE_URL" \
    --output "$output_file"
done

popd >/dev/null

echo "GC log: $GC_LOG"
echo "App log: $APP_LOG"
echo "Benchmark summaries: $RESULTS_DIR"

