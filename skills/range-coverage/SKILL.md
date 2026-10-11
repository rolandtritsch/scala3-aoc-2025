---
name: range-coverage
description: Count how many of a set of item IDs fall inside at least one inclusive `first-last` range (ranges may overlap) using a deterministic, read-only cross-check. Use when a puzzle asks how many values are covered by any of a set of inclusive ranges (e.g. Day 05 part 1 "cafeteria fresh ingredient IDs"), or when a covered-count answer must be re-derived without resubmitting.
---

# Skill: range-coverage

Part of the AoC 2025 cycle in this repository. Day 05 part 1 (accepted answer
848) is the "inclusive range membership" pattern: a database lists fresh-ID
ranges (each an inclusive `first-last`, possibly overlapping), a blank line,
then available ingredient IDs; an ID is fresh when it falls inside at least one
range. Counting those fresh IDs is a recurring AoC problem shape (union
coverage of inclusive ranges), so this skill captures it as a deterministic,
read-only cross-check.

## When to use

- Any puzzle that asks "how many of these values are covered by any of these
  inclusive ranges" (or equivalently, how many items lie in at least one range).
- Verifying a committed `part1` answer for a range-coverage day before a submit
  or review disposition (re-derive the count from the input file).
- Re-confirming that a previously claimed covered-count (e.g. 848 for Day 05)
  still holds on the current input without trusting a paraphrase.

## Procedure

1. Work in the synchronized checkout (`scala3-aoc-2025`).
2. Self-test the helper (no network, no submissions; also validates the two
   independent implementations agree and asserts the accepted real answer):

   ```bash
   scripts/check-range-coverage.py
   # ok: 2000 random naive/merged cross-checks, sample count == 3, real count == 848
   ```

3. Re-derive a day's covered count from its input file (asserting an expected
   value when given):

   ```bash
   scripts/check-range-coverage.py inputs/Day05.txt 848
   # ranges=182 ids=1000 covered=848
   # ok: covered == 848
   ```

4. Read the output. Exit 0 means all checks passed; a non-zero exit names the
   failing gate (self-test mismatch, expected-value mismatch, or malformed
   input: missing blank line, bad `first-last` range, `first > last`).

## Evidence and validation

- The script implements the count two independent ways and cross-checks them:
  a direct per-ID scan (mirrors `Day05.isFresh`/`Day05.part1` in
  `src/main/scala/aoc2025/Day05.scala`) and a merged-interval binary search.
  Random seeded cases (2000, seed 20251011) must agree.
- The self-test asserts the official Day 05 sample (3 of
  {1, 5, 8, 11, 17, 32}) and the accepted Day 05 real-input answer (848) from
  `src/main/resources/inputs/Day05.txt` (resolved relative to the script).
- Failure cases exercised: wrong expected count, missing file, missing blank
  line, malformed range, `first > last` range; each exits non-zero with a
  labeled message.
- `py_compile` clean. Read-only: never submits, never touches the network.

## Stopping conditions

- Stop after the covered count is recorded; do not submit answers, create
  issues, or push from this skill.
- For a rejection review, record the unresolved repair and leave the puzzle
  repair to the ProblemSolver on a later CTO cycle.