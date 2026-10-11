---
name: worksheet-arithmetic
description: Parse a worksheet of problems laid out side by side in a wide horizontal grid, where each problem's numbers sit one per line in a vertical strip and the operator (`+` or `*`) is at the bottom of the strip, and compute the grand total of all problems. Use when a puzzle describes numbers stacked in a column with the operator below them, separated from the next problem by a full column of spaces, or when a committed worksheet-arithmetic answer must be re-derived without resubmitting (e.g. Day 06 part 1 "trash compactor", accepted answer 6378679666679).
---

# Skill: worksheet-arithmetic

Part of the AoC 2025 cycle in this repository. Day 06 is the "wide horizontal
worksheet" pattern: a math worksheet lists problems side by side; each problem's
numbers are one per line in a vertical strip with the operator (`+` or `*`) at
the bottom of that strip, and problems are separated by a full column of only
spaces (the left/right alignment of numbers within a strip is irrelevant). Part
1 (accepted answer 6378679666679) is the grand total: each problem's numbers
combined with its bottom operator, summed over all problems. The columnar-strip
parsing and per-problem fold are recurring AoC shapes, so this skill captures
them as a deterministic, read-only cross-check.

## When to use

- Any puzzle that stacks numbers one per line under a single bottom operator per
  problem and asks for a value per problem or a grand total.
- Any puzzle laid out as a wide fixed-width grid where problems are separated by
  a full column of spaces and their alignment is irrelevant.
- Verifying a committed `part1` answer for a worksheet-arithmetic day before a
  submit or review disposition (re-derive the total from the input).
- Re-confirming that a previously claimed grand total (e.g. 6378679666679 for
  Day 06) still holds on the current input without trusting a paraphrase.

## Procedure

1. Work in the synchronized checkout (`scala3-aoc-2025`).
2. Self-test the helper (no network, no submissions; validates both parsers
   against generated worksheets and asserts sample + accepted real answers):

   ```bash
   scripts/check-worksheet-arithmetic.py
   # ok: 1000 generated worksheets re-parsed to known totals ..., sample total == 4277556, real total == 6378679666679 (1000 problems)
   ```

3. Re-derive a day's grand total from its input file (asserting an expected
   value when given):

   ```bash
   scripts/check-worksheet-arithmetic.py inputs/Day06.txt 6378679666679
   # problems=1000 total=6378679666679
   # ok: total == 6378679666679
   ```

4. Read the output. Exit 0 means all checks passed; a non-zero exit names the
   failing gate (parser mismatch, generated-case mismatch, malformed worksheet
   such as a missing operator, an operator token other than `+`/`*`, or an
   expected-value mismatch).

## Evidence and validation

- The script implements parsing two independent ways and cross-checks them:
  a column-run parser that finds maximal runs of columns which are not blank
  across every line (mirrors `Day06.readFile` in
  `src/main/scala/aoc2025/Day06.scala`), and an operator-run parser that groups
  number-row tokens by the bottom-line operator token whose column span they
  overlap. Both must recover the exact `(operator, numbers)` problem list.
- Random worksheets are generated from known `(operator, numbers)` problems
  (1000 cases, seed 20251011) in the statement layout, so the known grand
  total is available for comparison; both parsers must recover it.
- The self-tests assert the official Day 06 sample total (4277556 for
  `123*45*6 + 328+64+98 + 51*387*215 + 64+23+314`) and the accepted Day 06
  real-input answer (6378679666679, 1000 problems) from
  `src/main/resources/inputs/Day06.txt` (resolved relative to the script).
- Failure cases exercised: missing file, worksheet with no operator row,
  bad operator token, missing operators/numbers, token overlapping multiple
  operator runs, wrong expected total, usage errors; each exits non-zero with a
  labeled message.
- `py_compile` clean. Read-only: never submits, never touches the network.

## Stopping conditions

- Stop after the grand total is recorded; do not submit answers, create issues,
  or push from this skill.
- For a rejection review, record the unresolved repair and leave the puzzle
  repair to the ProblemSolver on a later CTO cycle.