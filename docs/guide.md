# Documentation Guide

## Mandatory Read Order
Before planning or coding, always read these files first:
1. [index.md](index.md)
2. [guide.md](guide.md)
3. [steering.md](steering.md)
4. [implementation-plan.md](implementation-plan.md)
5. [progress.md](progress.md)

## Core Working Rules
- Always align implementation work to [steering.md](steering.md).
- Always keep docs current with the latest decisions and progress.
- If priorities, scope, architecture, or timelines change, update docs immediately.
- After each implementation step, run relevant tests and capture the result.

## Required Documentation Updates
Update docs whenever any of the following changes:
- Project scope or out-of-scope boundaries
- Architecture decisions or middleware direction (Kafka/Solace)
- GC strategy, benchmark method, or tuning results
- Sprint status, milestones, blockers, or next actions
- Execution tasks, sequencing, or phase exit criteria

## Minimum Update Checklist
For meaningful progress, do all of the following:
1. Update [index.md](index.md) progress snapshot.
2. Add a dated entry to [progress.md](progress.md).
3. Update [implementation-plan.md](implementation-plan.md) if execution sequencing or checkpoints changed.
4. Update [steering.md](steering.md) if direction, scope, or plan changed.
5. Add or refresh any supporting notes in docs as needed.
6. Run relevant tests after implementation and record pass/fail status.
7. Keep changes concise, factual, and date-aware.

## Change Logging Convention
Use a short entry format when recording updates:
- Date
- What changed
- Why it changed
- Impact on next steps

Example:
- 2026-03-15: Added Docker positioning to steering to improve reproducibility; keeps Kubernetes out of scope; next step is Docker Compose baseline.

## Definition of Documentation Done
A documentation update is complete when:
- Read-first files are still accurate
- Current status is visible in [index.md](index.md)
- A dated update exists in [progress.md](progress.md)
- Execution status is aligned with [implementation-plan.md](implementation-plan.md)
- Test status for implemented changes is recorded
- Steering and progress are consistent with implementation reality
