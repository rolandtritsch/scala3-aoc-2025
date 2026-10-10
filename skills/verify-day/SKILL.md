---
name: verify-day
description: Verify a committed AoC day part-1 solution in scala3-aoc-2025 before submit or review disposition. Use when the solver, submitter, or reviewer needs to confirm git sync, the day-specific test suite, and the expected `sbt run` part-1 answer for a day (NN) on the synchronized checkout.
---

# Skill: verify-day

Part of the AoC 2025 cycle in this repository. The submit and review stages
depend on reproducible local evidence: a clean, synced checkout plus a green,
day-specific test suite plus an expected part-1 output. This skill captures
that as a deterministic script.

## When to use

- Before recording a submit verdict or a review disposition for day NN part 1.
- After a solver commits a new day, to re-confirm the evidence chain.
- Whenever a prior claim like "tests pass" or "part1 prints X" must be
  re-verified locally.

## Procedure

1. Work in the synchronized checkout (`scala3-aoc-2025`), which must already
   be at the reviewed commit with `origin/main` fetched.
2. Run the reusable verifier from the repo root:

   ```bash
   scripts/verify-day.sh <NN> [EXPECTED_PART1]
   ```

   - `<NN>` is the zero-padded day, e.g. `01` (also works for the `00`
     template).
   - `EXPECTED_PART1` is optional; when given, the script asserts that
     `sbt run` prints exactly `DayNN - part1: <EXPECTED_PART1>`.
3. Read the output. Each check is labeled; the script exits non-zero on the
   first failure:
   - git sync state: `HEAD == origin/main` and a clean working tree.
   - `sbt testOnly aoc2025.DayNNTest`: sample, boundary, and real-input tests.
   - `sbt run`: prints the `DayNN - part1:` line, compared to `EXPECTED` if set.
4. If a check fails, stop and record the exact failure; do not submit, accept,
   or review with broken evidence.

## Evidence and validation

- Exit code 0 with `ALL CHECKS PASSED for Day NN` means the evidence chain is
  complete for that day's part-1 submit/review.
- A non-zero exit names the failing gate (unsynced/dirty tree, failing tests,
  missing/unexpected run line).
- Independently cross-check the part-1 answer when feasible (e.g. a small
  Python pass over `inputs/DayNN.txt`) — the script asserts committed expected
  answers but is not a substitute for the solver's independent check.

## Stopping conditions

- Stop after the verification result is recorded; do not submit answers,
  create issues, or push from this skill.
- For a rejection review, record the unresolved repair and leave the puzzle
  repair to the ProblemSolver on a later CTO cycle.