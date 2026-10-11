---
name: worksheet-arithmetic
description: Parse a worksheet of problems laid out side by side in a wide horizontal grid, where each problem's numbers sit one per line in a vertical strip and the operator (`+` or `*`) is at the bottom of the strip, and compute the grand total of all problems. Two readings are supported: part 1 reads each number as the token on its own number line (left-to-right), part 2 reads each number as a single column of stacked digits with its most significant digit at the top and least significant digit at the bottom, columns read right-to-left within a problem and problems emitted right-to-left. Use when a puzzle describes numbers stacked in a column with the operator below them, separated from the next problem by a full column of spaces, or when a committed worksheet-arithmetic answer must be re-derived without resubmitting (e.g. Day 06 "trash compactor", accepted answers part 1 6378679666679 and part 2 11494432585168).
---

# Skill: worksheet-arithmetic

Part of the AoC 2025 cycle in this repository. Day 06 is the "wide horizontal
worksheet" pattern: a math worksheet lists problems side by side; each problem's
numbers are one per line in a vertical strip with the operator (`+` or `*`) at
the bottom of that strip, and problems are separated by a full column of only
spaces. The same worksheet is read two different ways across the two parts:

- Part 1 (accepted answer 6378679666679): each number is the token on its own
  number line inside the strip, read left-to-right. The grand total combines
  each problem's numbers with its bottom operator.
- Part 2 (accepted answer 11494432585168): each number is a single column of
  digits, most significant digit at the top, least significant digit at the
  bottom (a leading blank appears only above a shorter number). A problem's
  columns are read right-to-left, and problems themselves are emitted
  right-to-left, then the same per-problem fold gives the grand total.

The columnar-strip parsing and per-problem fold are recurring AoC shapes, so
this skill captures both readings as deterministic, read-only cross-checks.

## When to use

- Any puzzle that stacks numbers one per line under a single bottom operator per
  problem and asks for a value per problem or a grand total.
- Any puzzle laid out as a wide fixed-width grid where problems are separated by
  a full column of spaces and their alignment is irrelevant.
- A puzzle that reads the same worksheet a second way, where each number is the
  stacked digit run of a single column read right-to-left (e.g. Day 06 part 2).
- Verifying a committed `part1` or `part2` answer for a worksheet-arithmetic day
  before a submit or review disposition (re-derive the total from the input).
- Re-confirming that a previously claimed grand total (e.g. 6378679666679 for
  Day 06 part 1 or 11494432585168 for Day 06 part 2) still holds on the current
  input without trusting a paraphrase.

## Procedure

1. Work in the synchronized checkout (`scala3-aoc-2025`).
2. Self-test both readings (no network, no submissions; validates both parsers
   of each reading against generated worksheets and asserts sample + accepted
   real answers):

   ```bash
   scripts/check-worksheet-arithmetic.py
   # ok: 1000 generated worksheets re-parsed to known totals ..., sample total == 4277556, real total == 6378679666679 (1000 problems)
   scripts/check-worksheet-arithmetic.py --part2
   # ok: part2: 1000 generated worksheets re-parsed to known totals ..., sample total == 3263827, real total == 11494432585168 (1000 problems)
   ```

3. Re-derive a day's grand total from its input file (asserting an expected
   value when given):

   ```bash
   scripts/check-worksheet-arithmetic.py inputs/Day06.txt 6378679666679
   # problems=1000 total=6378679666679
   # ok: total == 6378679666679
   scripts/check-worksheet-arithmetic.py --part2 inputs/Day06.txt 11494432585168
   # problems=1000 total=11494432585168
   # ok: total == 11494432585168
   ```

4. Read the output. Exit 0 means all checks passed; a non-zero exit names the
   failing gate (parser mismatch, generated-case mismatch, malformed worksheet
   such as a missing operator or an empty number column, an operator token other
   than `+`/`*`, or an expected-value mismatch).

## Evidence and validation

- The script implements parsing two independent ways per reading and
  cross-checks them. For part 1: a column-run parser that finds maximal runs of
  columns which are not blank across every line (mirrors `Day06.readFile` in
  `src/main/scala/aoc2025/Day06.scala`), and an interval-union parser that
  groups number-row tokens by the bottom-line operator token whose column span
  they overlap. For part 2: a right-to-left column-run parser (mirrors
  `Day06.readFilePart2`) and an interval-union reference that forms the same
  strips by merging the occupied column intervals of every token. Both readings'
  two parsers must recover the exact `(operator, numbers)` problem list.
- Random worksheets are generated from known `(operator, numbers)` problems
  (1000 cases per reading, seed 20251011) in each statement layout, so the known
  grand total is available for comparison; both parsers must recover it.
- The self-tests assert the official Day 06 sample totals (4277556 for part 1 —
  `123*45*6 + 328+64+98 + 51*387*215 + 64+23+314` — and 3263827 for part 2 —
  `4+431+623 + 175*581*32 + 8+248+369 + 356*24*1`) and the accepted Day 06
  real-input answers (6378679666679 and 11494432585168, 1000 problems each)
  from `src/main/resources/inputs/Day06.txt` (resolved relative to the script).
- Failure cases exercised for both readings: missing file, worksheet with no
  operator row, bad operator token, missing operators, empty number column,
  wrong expected total, usage errors; each exits non-zero with a labeled
  message.
- `py_compile` clean. Read-only: never submits, never touches the network.

## Stopping conditions

- Stop after the grand total is recorded; do not submit answers, create issues,
  or push from this skill.
- For a rejection review, record the unresolved repair and leave the puzzle
  repair to the ProblemSolver on a later CTO cycle.