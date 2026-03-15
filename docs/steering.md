# Java Trading PoC Steering Document

## 1. Purpose
Build a public, employer-ready Java trading PoC that demonstrates practical capability aligned to the target FX electronic trading role:
- low-latency Java service development
- asynchronous messaging with Solace or Kafka
- measurable garbage collection (GC) and throughput tuning
- clean delivery workflow using GitHub, Maven, and CI

This document defines direction, boundaries, and decision criteria so implementation work stays focused.

## 2. Role Alignment (From JD)
The PoC should provide evidence of:
- Java 11-17 server-side engineering (Spring-based service)
- asynchronous, real-time style communication pattern
- messaging middleware competence (Solace or Kafka)
- Linux/bash-friendly run and test workflow
- DevOps basics (Maven build, GitHub repo hygiene, optional CI)
- ownership mindset: incremental delivery and clear sprint outcomes

## 3. Learning Goals
Primary learning outcomes for this PoC:
1. Garbage Collection
- Understand collector behavior under sustained message flow
- Compare baseline vs tuned JVM settings
- Explain impact on latency, throughput, and pause times

2. Solace or Kafka
- Implement event-driven publish/consume workflow
- Understand delivery semantics, ordering, and back-pressure behavior
- Document tradeoffs and why one platform was chosen for the PoC

3. Employer Visibility via GitHub
- Publish a clean, reproducible repository
- Show measurable performance work and transparent engineering decisions
- Provide documentation that allows reviewers to run and evaluate quickly

## 4. PoC Scope
In scope:
- One core trading simulation flow (quote/order event pipeline)
- Spring Boot Java service(s)
- Middleware integration with either:
  - Option A: Kafka (default if tooling/setup speed is priority)
  - Option B: Solace PubSub+ (if demonstrating enterprise broker exposure is priority)
- Docker Compose for one-command local startup (app(s) + broker dependencies)
- Basic persistence or in-memory state as needed for demonstration
- Performance/GC test scenario with repeatable scripts
- Observability outputs: logs + key latency/throughput metrics

Out of scope (for initial PoC):
- Production-grade risk engine
- Full FIX gateway implementation
- Complex UI (minimal API or simple CLI is enough)
- Multi-region deployment and advanced infra hardening
- Kubernetes and production orchestration concerns

## 5. Candidate Architecture
Suggested minimal architecture:
- producer-service: emits quote/order events at controlled rates
- matching-service: consumes, processes, and emits execution events
- reporter module: aggregates metrics (p50/p95/p99 latency, throughput)
- broker: Kafka or Solace as transport backbone

Key design principles:
- async boundaries explicit at broker topics/queues
- deterministic payload schema and versioned event contracts
- idempotent consumer behavior where practical
- benchmark mode separated from functional mode

## 6. GC and Performance Workstream
Baseline first, tune second.

Baseline tasks:
- Run controlled load profile (low, medium, high)
- Capture JVM and GC logs
- Record end-to-end message latency and throughput

Tuning tasks:
- Start with G1GC defaults
- Compare at least one tuned profile (heap sizing and pause target)
- Optional comparison with ZGC for modern JDK if environment supports it

Minimum evidence to publish:
- before/after performance table
- JVM flags used
- short interpretation of tradeoffs

## 7. Solace vs Kafka Decision Track
Decision criteria:
- setup speed and local reproducibility
- ecosystem maturity and learning value for target role
- operational complexity for a public PoC
- ability to demonstrate queue/topic patterns clearly

Recommended approach:
- Phase 1: implement Kafka path quickly to secure end-to-end flow
- Phase 2 (optional): add Solace adapter or parallel path and compare behavior

If only one platform is delivered, include a concise "why not the other" rationale.

## 8. Docker Positioning
Use Docker, but keep it pragmatic:
- Containerize services and dependencies to guarantee reproducible setup for reviewers
- Use Docker Compose for local developer experience and demo readiness
- Keep JVM tuning transparent by documenting container memory limits and matching JVM flags

Do not let Docker become the project focus. The focus remains Java async design, messaging, and GC/performance evidence.

## 9. Delivery Plan (4 Sprints, 2 Weeks Each)
Sprint 1: Foundation
- initialize project structure and build tooling
- implement event contracts and local runtime setup
- add docker-compose baseline for local run
- publish initial architecture and run instructions

Sprint 2: Functional Flow
- complete producer -> broker -> consumer pipeline
- add retries/error handling basics
- ship integration tests for core flow

Sprint 3: Performance + GC
- implement load script and metrics capture
- baseline + tuned GC runs
- publish results and interpretation

Sprint 4: Portfolio Hardening
- clean docs, diagrams, and decision records
- add GitHub Actions CI (build/test)
- polish README for recruiter/hiring-manager review

## 10. Definition of Done
The PoC is done when:
- a reviewer can clone and run in under 15 minutes
- core async trading simulation works reliably
- GC/performance evidence is documented with reproducible steps
- messaging choice (Kafka/Solace) is justified with clear criteria
- repository demonstrates engineering quality (tests, structure, docs)

## 11. GitHub Portfolio Requirements
Repository should include:
- clear README with:
  - problem statement and architecture diagram
  - quick-start commands
  - sample output
  - performance findings summary
- docs folder with:
  - steering document (this file)
  - architecture notes
  - benchmark methodology and results
- CI status badge and test command
- concise commit history showing incremental delivery

Suggested top-level sections in README:
1. Why this project
2. Architecture
3. Run locally
4. Test and benchmark
5. GC tuning results
6. Messaging decision (Kafka vs Solace)
7. Future improvements

## 12. Risks and Mitigations
Risk: scope becomes too broad for a PoC.
Mitigation: keep one critical trading flow only; defer extras.

Risk: tuning effort becomes anecdotal without measurement.
Mitigation: require repeatable load profiles and structured results table.

Risk: repository looks incomplete to employers.
Mitigation: prioritize documentation, reproducibility, and CI before adding new features.

## 13. Immediate Next Actions
1. Choose initial broker path: Kafka first or Solace first.
2. Create initial project skeleton and event contract package.
3. Add load generator script and define first benchmark scenario.
4. Start a results log template for GC and latency runs.
5. Add a simple Docker Compose command path for reviewers (for example, up, test, down).
