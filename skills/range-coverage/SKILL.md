---
name: range-coverage
description: Count how many of a set of item IDs fall inside at least one inclusive `first-last` range (ranges may overlap), or count the total IDs the ranges themselves consider covered (merged inclusive-union count). Use when a puzzle asks how many values are covered by any of a set of inclusive ranges (e.g. Day 05 part 1 "cafeteria fresh ingredient IDs"), or how many IDs a set of inclusive ranges covers in total (e.g. Day 05 part 2), or when such a covered/union count must be re-derived without resubmitting.
---

# Skill: range-coverage

Part of the AoC 2025 cycle in this repository. Day 05 is the "inclusive range
membership" pattern: a database lists fresh-ID ranges (each an inclusive
`first-last`, possibly overlapping), a blank line, then available ingredient
IDs; an ID is fresh when it falls inside at least one range. Counting those
fresh IDs (part 1, accepted answer 848) and counting the total IDs the ranges
themselves consider fresh (part 2, accepted answer 334714395325710) are both
recurring AoC problem shapes (union coverage of inclusive ranges), so this
skill captures them as deterministic, read-only cross-checks.

## When to use

- Any puzzle that asks "how many of these values are covered by any of these
  inclusive ranges" (or equivalently, how many items lie in at least one range).
- Any puzzle that asks "how many IDs do these inclusive ranges cover in total",
  i.e. the merged inclusive-union count, where the available-ID list (if any) is
  irrelevant.
- Verifying a committed `part1` or `part2` answer for a range-coverage day
  before a submit or review disposition (re-derive the count from the input).
- Re-confirming that a previously claimed covered/union count (e.g. 848 or
  334714395325710 for Day 05) still holds on the current input without trusting
  a paraphrase.

## Procedure

1. Work in the synchronized checkout (`scala3-aoc-2025`).
2. Self-test the helper (no network, no submissions; validates the independent
   implementations agree and asserts the accepted real answers):

   ```bash
   scripts/check-range-coverage.py            # covered-count self-test
   # ok: 2000 random naive/merged cross-checks, sample count == 3, real count == 848
   scripts/check-range-coverage.py --union    # union-count self-test
   # ok: 2000 random naive/merged union cross-checks, sample union == 14, real union == 334714395325710
   ```

3. Re-derive a day's covered count from its input file (asserting an expected
   value when given):

   ```bash
   scripts/check-range-coverage.py inputs/Day05.txt 848
   # ranges=182 ids=1000 covered=848
   # ok: covered == 848
   ```

4. Re-derive a day's merged inclusive-union count (ignoring the available-ID
   list) from its input file:

   ```bash
   scripts/check-range-coverage.py --union inputs/Day05.txt 334714395325710
   # ranges=182 merged=83 union=334714395325710
   # ok: union == 334714395325710
   ```

5. Read the output. Exit 0 means all checks passed; a non-zero exit names the
   failing gate (self-test mismatch, expected-value mismatch, or malformed
   input: missing blank line, bad `first-last` range, `first > last`).

## Evidence and validation

- The script implements the covered count two independent ways and
  cross-checks them: a direct per-ID scan (mirrors `Day05.isFresh`/`Day05.part1`
  in `src/main/scala/aoc2025/Day05.scala`) and a merged-interval binary search.
  Random seeded cases (2000, seed 20251011) must agree.
- The union count merges overlapping/adjacent inclusive ranges and sums the
  span lengths (mirrors `Day05.mergedRanges`/`Day05.part2`), cross-checked
  against a naive explicit-set union on the same seeded random cases.
- The self-tests assert the official Day 05 sample (3 of {1, 5, 8, 11, 17, 32},
  and union 14 of ranges 3-5,10-14,16-20,12-18) and the accepted Day 05
  real-input answers (848 covered, 334714395325710 union, 83 merged spans) from
  `src/main/resources/inputs/Day05.txt` (resolved relative to the script).
- Failure cases exercised: wrong expected count, missing file, missing blank
  line, malformed range, `first > last` range, usage errors; each exits non-zero
  with a labeled message.
- `py_compile` clean. Read-only: never submits, never touches the network.

## Stopping conditions

- Stop after the covered/union count is recorded; do not submit answers, create
  issues, or push from this skill.
- For a rejection review, record the unresolved repair and leave the puzzle
  repair to the ProblemSolver on a later CTO cycle.