# Progress Log

This file tracks dated progress updates for the Java trading PoC.

## Update Format
- Date
- What changed
- Why it changed
- Impact on next steps

## Entries
- 2026-03-15: Created initial steering document from JD.
  - Why: establish project direction, scope, and milestone cadence.
  - Impact: implementation can proceed with clear priorities.

- 2026-03-15: Added Docker positioning to steering.
  - Why: improve reproducibility for GitHub reviewers without shifting focus away from Java, messaging, and GC.
  - Impact: Docker Compose included in scope; Kubernetes remains out of scope.

- 2026-03-15: Added docs index and guide.
  - Why: enforce consistent read-first and update behavior.
  - Impact: future work should follow documented workflow before implementation.

- 2026-03-15: Moved JD into docs and added test-after-implementation rule.
  - Why: centralize project docs and enforce quality checks on every implementation step.
  - Impact: JD is now under docs; guide now requires running and recording relevant tests after changes.

- 2026-03-15: Added implementation plan and integrated it into read-first workflow.
  - Why: translate steering into phase-by-phase execution with explicit test checkpoints.
  - Impact: implementation now has task sequencing, exit criteria, and milestone mapping.
  - Test: docs-only change, PASS (manual verification of links and structure).

- 2026-03-15: Switched project runtime to SDKMAN Java 17 and verified build/tests.
  - Why: project targets Java 17 and initial verification failed under Java 8.
  - Impact: Java runtime is aligned with pom settings; repeatable local setup improved.
  - Test: mvn test on Java 17 (SDKMAN), PASS (1 test, 0 failures).

- 2026-03-15: Implemented Phase 1 trading skeleton.
  - Why: complete initial functional foundation before Kafka integration.
  - Impact: event contracts, in-memory producer/consumer flow, and system/trading endpoints are now available.
  - Test: mvn -B test (Java 17 via SDKMAN), PASS (5 tests, 0 failures).

- 2026-03-15: Implemented Phase 2 Kafka integration.
  - Why: replace in-memory transport with Kafka for async publish/consume flow.
  - Impact: KafkaQuoteEventProducer and KafkaOrderProcessor active via @Profile("kafka"); application-kafka.yml drives Kafka config; docker-compose.yml added for local Kafka (bitnami KRaft, no ZooKeeper).
  - Test: mvn -B test, PASS (7 tests, 0 failures) including EmbeddedKafka integration test.

- 2026-03-15: Implemented Phase 3 Docker runtime.
  - Why: containerize application for reproducibility; demonstrate Docker competency for GitHub reviewers.
  - Impact: multi-stage Dockerfile (eclipse-temurin:17-jdk-alpine build / :17-jre-alpine runtime, non-root user, G1GC flags); docker-compose.yml extended with app service depending on healthy Kafka; .dockerignore added; scripts/smoke-test.sh verifies full end-to-end flow (health, order, quote) with cleanup trap.
  - Test: mvn -B test (Java 17 via SDKMAN), PASS (7 tests, 0 failures).

- 2026-03-15: Implemented Phase 4 performance and GC baseline.
  - Why: establish a reproducible throughput, latency, and GC baseline before JVM tuning work.
  - Impact: added standalone Java load generator with low/medium/high profiles; added scripts/run-gc-baseline.sh to package the app, launch it with GC logging, and capture benchmark summaries; published measured baseline results and GC observations in gc-baseline.md.
  - Test: mvn -B test (Java 17 via SDKMAN), PASS (7 tests, 0 failures).
  - Test: ./scripts/run-gc-baseline.sh, PASS (low/medium/high profiles completed with 0 failures; GC log and benchmark summaries produced under artifacts/gc/).

- 2026-03-15: Implemented Phase 5 G1GC tuning comparison.
  - Why: validate whether a more aggressive fixed-heap G1GC profile improves tail behavior enough to justify changing the default runtime profile.
  - Impact: extended the benchmark runner to support baseline and tuned JVM profiles; added scripts/run-g1gc-comparison.sh; published before/after throughput, latency, and GC observations; decision is to keep the baseline profile because the tuned profile regressed throughput and p99 under medium/high load.
  - Test: mvn -B test (Java 17 via SDKMAN), PASS (7 tests, 0 failures).
  - Test: ./scripts/run-g1gc-comparison.sh, PASS (baseline and tuned runs completed; no full GC events observed in either profile).

- 2026-03-15: Implemented Phase 6 Solace track.
  - Why: add enterprise-broker breadth aligned to the JD and document the practical tradeoffs against the existing Kafka path.
  - Impact: added a `solace` Spring profile backed by the JCSMP client; created direct-topic quote publishing and order-to-execution processing; added `solace` and `app-solace` services to docker-compose; added `SolaceRoundTripCli` and `scripts/smoke-test-solace.sh`; published `solace-comparison.md`; fixed the Docker build stage to use a Maven image so container builds work reliably.
  - Test: mvn -B test (Java 17 via SDKMAN), PASS (7 tests, 0 failures).
  - Test: ./scripts/smoke-test-solace.sh, PASS (Solace broker, app-solace health endpoint, quote publish, and direct order-to-execution round trip all succeeded).

- 2026-03-15: Started Phase 7 portfolio hardening.
  - Why: improve reviewer experience with a clear project narrative, architecture, and copy-paste run paths.
  - Impact: added top-level README.md with architecture diagram, quick-start instructions, API examples, benchmark workflow, messaging decision snapshot, and documentation map.
  - Test: docs update verification, PASS (manual link and command consistency check).
