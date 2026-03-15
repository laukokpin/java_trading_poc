# Implementation Plan

## 1. Objective
Execute the Java Trading PoC in a structured, test-first-visible way that supports learning goals (GC + Kafka/Solace) and produces an employer-ready GitHub repository.

## 2. Working Principles
- Implement in small, reviewable increments.
- Run relevant tests after every implementation step.
- Record pass/fail status and key notes in progress updates.
- Keep architecture choices aligned with steering.

## 3. Phased Plan

### Phase 0: Project Setup
Tasks:
1. Initialize Maven multi-module or clean single-module structure.
2. Set Java version target (17 preferred), formatter, and baseline dependencies.
3. Add initial CI workflow for build and test.

Test checkpoint after each task:
- Run build verification command.
- Record result as PASS/FAIL with short note.

Exit criteria:
- Build succeeds locally.
- CI pipeline runs build+test on push.

### Phase 1: Event Contract + Basic Service Skeleton
Tasks:
1. Define event contracts for quote/order/execution messages.
2. Create producer and consumer service skeletons.
3. Add health and readiness endpoints.

Test checkpoint after each task:
- Compile and run unit tests.
- Validate service startup and health endpoint response.

Exit criteria:
- Services start consistently.
- Contract tests or schema checks pass.

### Phase 2: Kafka Integration (Primary Path)
Tasks:
1. Configure topics and producer publishing flow.
2. Implement consumer processing flow and basic retry handling.
3. Add idempotency guard or duplicate-handling strategy.

Test checkpoint after each task:
- Run integration tests for publish-consume flow.
- Verify ordering and delivery behavior in sample run.

Exit criteria:
- End-to-end async message flow works reliably.
- Integration tests pass.

### Phase 3: Docker Compose Runtime
Tasks:
1. Add Dockerfiles for services.
2. Add docker-compose setup for app + Kafka dependencies.
3. Add one-command local start and stop workflow.

Test checkpoint after each task:
- Run compose startup check.
- Run integration smoke test in containerized environment.

Exit criteria:
- Reviewer can run full stack with minimal commands.
- Containerized smoke test passes.

### Phase 4: Performance and GC Baseline
Tasks:
1. Implement load generator with low/medium/high profiles.
2. Capture baseline throughput, latency, and GC logs.
3. Publish baseline results table.

Test checkpoint after each task:
- Verify load test executes reproducibly.
- Validate metrics and GC logs are produced.

Exit criteria:
- Baseline report is reproducible and documented.

### Phase 5: GC Tuning Iterations
Tasks:
1. Run default G1GC baseline comparison.
2. Apply tuned JVM profile (heap and pause target) and compare.
3. Optional: run ZGC comparison if environment supports it.

Test checkpoint after each task:
- Re-run benchmark profile.
- Validate no functional regressions through integration tests.

Exit criteria:
- Before/after table is published with interpretation.
- JVM flags and environment limits are documented.

### Phase 6: Solace Track (Optional Extension)
Tasks:
1. Stand up Solace local path.
2. Add adapter or parallel flow for message transport.
3. Document Kafka vs Solace tradeoffs from the PoC.

Test checkpoint after each task:
- Run equivalent flow tests for Solace path.
- Compare behavior and operational complexity notes.

Exit criteria:
- Clear decision note exists, even if only one platform is finalized.

### Phase 7: Portfolio Hardening
Tasks:
1. Improve README with architecture diagram and quick start.
2. Finalize docs for benchmark method and results.
3. Clean commit history structure for readability.

Test checkpoint after each task:
- Run full local test suite.
- Confirm CI green status.

Exit criteria:
- Repo is reviewer-friendly and runnable within 15 minutes.

## 4. Test Policy
For every implementation change:
1. Run the smallest relevant test set immediately.
2. Run broader regression tests at phase boundaries.
3. Log outcome in progress notes using PASS/FAIL format.

Example log line:
- Test: integration publish-consume flow, PASS, 2026-03-15.

## 5. Milestone Mapping (GitHub)
- Milestone 1: Setup + service skeleton complete.
- Milestone 2: Kafka end-to-end flow complete.
- Milestone 3: Docker Compose runtime complete.
- Milestone 4: GC baseline + tuning evidence complete.
- Milestone 5: Portfolio hardening complete.

## 6. Risks and Controls
- Risk: implementation drifts from steering.
  - Control: verify each phase against steering before merge.
- Risk: tests are skipped during fast iterations.
  - Control: no task marked done without recorded test outcome.
- Risk: performance results are not reproducible.
  - Control: keep input profiles and JVM flags versioned in docs.

## 7. Immediate Next Execution Step
Continue Phase 7 with final hardening: polish benchmark/result references across docs, then clean commit history structure before public publish.
