# Documentation Index

## Read First
Read these in order before making changes:
1. [index.md](index.md)
2. [guide.md](guide.md)
3. [steering.md](steering.md)
4. [implementation-plan.md](implementation-plan.md)
5. [progress.md](progress.md)
6. [gc-baseline.md](gc-baseline.md)
7. [solace-comparison.md](solace-comparison.md)

## Purpose
This index is the entry point for project documentation and current status.

## Document Map
- [guide.md](guide.md): Working rules for contributors and update protocol
- [steering.md](steering.md): Project direction, scope, milestones, and definition of done
- [implementation-plan.md](implementation-plan.md): Execution tasks, test checkpoints, and phase exit criteria
- [progress.md](progress.md): Dated change and progress history
- [gc-baseline.md](gc-baseline.md): Benchmark method, baseline results, and GC observations
- [solace-comparison.md](solace-comparison.md): Optional Solace transport path and Kafka vs Solace comparison
- [jd.md](jd.md): Source job description used for project alignment

## Current Focus
- Build employer-ready Java trading PoC
- Demonstrate GC tuning with measurable results
- Implement messaging flow with Kafka first, then optional Solace comparison
- Keep Docker Compose as reproducibility support, not the main project focus
- Run tests after each implementation step and record outcomes

## Progress Snapshot
- Steering document completed
- Docker positioning added to steering
- Index and guide added with mandatory read-first workflow
- Progress log created for dated updates
- JD moved into docs folder for centralized documentation
- Implementation plan created with phase-by-phase test checkpoints
- SDKMAN Java 17 configured for project; Maven tests passing on Java 17
- Phase 1 skeleton implemented: event contracts, in-memory producer/consumer, and REST endpoints
- Phase 1 tests passing (5 tests total)
- Phase 2 complete: Kafka producer, order processor, EmbeddedKafka integration tests
- Docker Compose (Kafka KRaft mode, bitnami) added for local developer run
- Phase 3 complete: multi-stage Dockerfile, .dockerignore, docker-compose app service, smoke-test.sh
- Phase 4 complete: standalone load generator, GC baseline runner, and published benchmark results
- Phase 5 complete: tuned G1GC comparison recorded and evaluated against the baseline profile
- Phase 6 complete: Solace profile, local broker runtime, direct round-trip CLI, and smoke test
- Phase 7 started: reviewer-facing README added with architecture diagram and quick-start paths
- All 7 tests passing
- Baseline benchmark recorded for low/medium/high profiles with zero failures
- Tuned G1GC reduced max GC pause but regressed throughput and tail latency at medium/high load
- Solace optional path verified with ./scripts/smoke-test-solace.sh
- Next implementation step: Phase 7 - portfolio hardening final pass (docs polish + commit history cleanup)

## Update Rule
When project scope, decisions, or progress changes, update this index, [progress.md](progress.md), and any affected docs in the same change.
