---
name: verify-day
description: Verify a committed AoC day solution in scala3-aoc-2025 before submit or review disposition. Use when the solver, submitter, or reviewer needs to confirm git sync, the day-specific test suite, and the expected `sbt run` part-1 and/or part-2 answer(s) for a day (NN) on the synchronized checkout.
---

# Skill: verify-day

Part of the AoC 2025 cycle in this repository. The submit and review stages
depend on reproducible local evidence: a clean, synced checkout plus a green,
day-specific test suite plus the expected `sbt run` output for the part(s)
under review. This skill captures that as a deterministic script.

## When to use

- Before recording a submit verdict or a review disposition for day NN part 1
  and/or part 2.
- After a solver commits a new day, to re-confirm the evidence chain.
- Whenever a prior claim like "tests pass" or "part1 prints X" must be
  re-verified locally.

## Procedure

1. Work in the synchronized checkout (`scala3-aoc-2025`), which must already
   be at the reviewed commit with `origin/main` fetched.
2. Run the reusable verifier from the repo root:

   ```bash
   scripts/verify-day.sh <NN> [EXPECTED_PART1] [EXPECTED_PART2]
   ```

   - `<NN>` is the zero-padded day, e.g. `01` (also works for the `00`
     template).
   - `EXPECTED_PART1` is optional; when given, the script asserts that
     `sbt run` prints exactly `DayNN - part1: <EXPECTED_PART1>`. The part-1
     line must be present even without an expected value.
   - `EXPECTED_PART2` is optional; when given, the script additionally asserts
     that `sbt run` prints exactly `DayNN - part2: <EXPECTED_PART2>`. Pre-
     part-2 days verify with only the part-1 value.
3. Read the output. Each check is labeled; the script exits non-zero on the
   first failure:
   - git sync state: `HEAD == origin/main` and a clean working tree.
   - `sbt testOnly aoc2025.DayNNTest`: sample, boundary, and real-input tests.
   - `sbt run`: prints the `DayNN - part1:` line (required) and, when
     `EXPECTED_PART2` is given, the `DayNN - part2:` line, each compared to the
     supplied expected value.
4. If a check fails, stop and record the exact failure; do not submit, accept,
   or review with broken evidence.

## Evidence and validation

- Exit code 0 with `ALL CHECKS PASSED for Day NN` means the evidence chain is
  complete for that day's submit/review with the supplied expected part(s).
- A non-zero exit names the failing gate (unsynced/dirty tree, failing tests,
  missing/unexpected run line).
- Independently cross-check the answer when feasible (e.g. a small Python pass
  over `inputs/DayNN.txt`) — the script asserts committed expected answers but
  is not a substitute for the solver's independent check.
- Validated on a scratch clone at a reviewed commit: `verify-day.sh 01 1129
  6638` and `verify-day.sh 02 44487518055` exit 0; wrong part-2, missing
  part-2 line when expected, wrong part-1, missing args, non-zero-padded day,
  and dirty-tree cases exit 1 with a labeled message.

## Stopping conditions

- Stop after the verification result is recorded; do not submit answers,
  create issues, or push from this skill.
- For a rejection review, record the unresolved repair and leave the puzzle
  repair to the ProblemSolver on a later CTO cycle.