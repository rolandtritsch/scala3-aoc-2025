---
name: red-rectangles
description: Find the largest axis-aligned rectangle whose opposite (diagonal) corners are two of a set of given grid points/tiles, where the rectangle edges run along grid lines so the width is the inclusive X span (`|dx|+1`), the height is the inclusive Y span (`|dy|+1`), and the area is their product, maximized over every unordered pair (e.g. Day 09 part 1 "Movie Theater", accepted answer 4777967538). Use when a puzzle asks for the largest rectangle between two marked tiles/points used as opposite corners, when an inclusive-vs-exclusive span interpretation must be double-checked, or when such a committed answer must be re-derived without resubmitting.
---

# Skill: red-rectangles

Part of the AoC 2025 cycle in this repository. Day 09 is the "largest
axis-aligned rectangle between two red tiles" pattern: given n red tile
coordinates (one `X,Y` pair per line), the Elves want the largest axis-aligned
rectangle that uses two red tiles as a pair of opposite (diagonal) corners.
The rectangle's edges run along the grid lines, so its width is the inclusive
span of the two X coordinates and its height is the inclusive span of the two Y
coordinates; the area of any such rectangle is `(|dx| + 1) * (|dy| + 1)`, and
the answer is the maximum over every unordered pair of red tiles (O(n^2)).

The key trap is the inclusive `+ 1` on each span: the corner tiles are part of
the rectangle, so 2,5..9,7 is width 8 * height 3 = 24 (not 7*2 = 14), and a
"thin" rectangle between two tiles on the same row is a valid height-1
rectangle. The official sample confirms the rule with 50 (2,5..11,1 -> 10*5)
and 24 (2,5..9,7 -> 8*3).

## When to use

- Any puzzle that asks for the largest axis-aligned rectangle formed by two
  marked tiles/points as opposite corners (area over an inclusive span).
- Any puzzle where the interpretation of "rectangle on a grid" is ambiguous
  between inclusive (`+1`) and exclusive spans — the thin-rectangle example is
  the tell.
- Verifying a committed `part1` answer for a red-rectangle day before a submit
  or review disposition (re-derive from the input).
- Re-confirming that a previously claimed answer (e.g. 4777967538 for Day 09)
  still holds on the current input without trusting a paraphrase.

## Procedure

1. Work in the synchronized checkout (`scala3-aoc-2025`).
2. Self-test the helper (no network, no submissions; validates two independent
   implementations agree and asserts the accepted real answer):

   ```bash
   scripts/check-red-rectangles.py
   # ok: 5000 random mirror/reference cross-checks, sample == 50, real == 4777967538
   ```

3. Re-derive a day's answer from its input file (asserting an expected value
   when given):

   ```bash
   scripts/check-red-rectangles.py inputs/Day09Test.txt 50
   # tiles=8 largest_area=50
   # corners=(11,1)-(2,5)
   # ok: largest_area == 50
   scripts/check-red-rectangles.py inputs/Day09.txt 4777967538
   # tiles=496 largest_area=4777967538
   # corners=(83050,86053)-(14405,16451)
   # ok: largest_area == 4777967538
   ```

   The `inputs/...` path form is resolved against
   `src/main/resources/inputs` regardless of the current directory.

4. Read the output. Exit 0 means all checks passed; a non-zero exit names the
   failing gate (self-test mismatch, expected-value mismatch, or malformed
   input: a line that is not exactly two integer coordinates).

## Evidence and validation

- The script implements the computation two independent ways and cross-checks
  them: an index-based double loop that mirrors `Day09.part1` in
  `src/main/scala/aoc2025/Day09.scala` (span via `abs(...) + 1`) and a
  functional `itertools.combinations` reference that computes each span via
  `max - min + 1` (structurally different, catches a sign/order mistake).
  5000 seeded random cases (seed 20261011), including 0/1-tile inputs and
  tiles sharing a row or column, must agree.
- The mirror also records the best corner pair, which the self-test and file
  mode report; on the real input the pair is
  (83050,86053)-(14405,16451), matching the independent brute-force
  cross-check recorded on the Day 09 solve issue.
- The self-test asserts the official Day 09 sample (8 tiles -> 50, best pair
  (11,1)-(2,5), the exact pair the statement uses for the area-50 rectangle)
  and the accepted Day 09 real-input answer 4777967538 from
  `src/main/resources/inputs/Day09.txt` (resolved relative to the script).
- Failure cases exercised: wrong expected value, missing file, malformed line
  (wrong number of coordinates), non-integer coordinate, empty input, and
  usage errors; each exits non-zero with a labeled message.
- `py_compile` clean. Read-only: never submits, never touches the network.

## Stopping conditions

- Stop after the answer is recorded; do not submit answers, create issues, or
  push from this skill.
- For a rejection review, record the unresolved repair and leave the puzzle
  repair to the ProblemSolver on a later CTO cycle.