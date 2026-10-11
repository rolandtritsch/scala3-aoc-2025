---
name: polyomino-packing
description: Decide whether a multiset of small, odd present shapes can all be placed inside a rectangular region on the unit grid, with each present rotatable and reflectable, the `#` cells of different presents never overlapping and the `.` cells freely shared (e.g. Day 12 "Christmas Tree Farm": part 1 accepted answer 408). Use when a puzzle asks how many regions can fit all listed presents, when a committed packing answer must be re-derived from the input without guessing, or when a backtracking polyomino-packing solver must be cross-checked against an independent exact-cover algorithm.
---

# Skill: polyomino-packing

Part of the AoC 2025 cycle in this repository. Day 12 is the "fit every
present into every region" pattern: the input lists a handful of small present
shapes (drawn as `#`/`.` diagrams) and, for every tree, the width and length of
the region beneath it plus how many presents of each shape must fit inside. A
present may be rotated and reflected but must sit exactly on the unit grid,
the occupied (`#`) cells of two different presents may not occupy the same grid
cell, and the unoccupied (`.`) cells of one present's diagram do not block
another present. The answer is how many regions can contain every listed
present at once.

A region can only fit when the total number of occupied cells of all its
presents does not exceed the region's area. Because the Day 12 shapes are tiny
this area bound is in practice the deciding test on the real input (the
accepted answer 408 equals the area-feasibility count exactly), but a complete
geometric search is the source of truth: the official `12x5: 1 0 1 0 3 2`
region is area-feasible (49 cells in a 60-cell region) yet geometrically
impossible.

## When to use

- Any puzzle that asks how many rectangular regions can fit a multiset of small
  shapes, where shapes may be rotated and reflected, occupied cells must not
  overlap, and unoccupied cells are shared freely.
- Verifying a committed `part1` answer for a polyomino-packing day before a
  submit or review disposition (re-derive from the input, not just the total).
- Re-confirming that a previously claimed answer (e.g. 408 for Day 12 part 1)
  still holds on the current input without trusting a paraphrase.
- Cross-checking a backtracking solver against an independent exact-cover
  implementation when the geometric feasibility (not just the area bound) is
  in question.

## Procedure

1. Work in the synchronized checkout (`scala3-aoc-2025`).
2. Self-test the helper (no network, no submissions; validates the mirror
   against an independent exact-cover reference and asserts the accepted real
   answers). This takes about 45 seconds because the real input runs the full
   geometric search over all 1000 regions:

   ```bash
   scripts/check-polyomino-packing.py
   # ok: 4000 mirror/dlx cross-checks, sample == 2, real == 408 (area count 408)
   ```

3. Re-derive a day's answer from its input file (asserting an expected value
   when given):

   ```bash
   scripts/check-polyomino-packing.py inputs/Day12Test.txt 2
   # shapes=6 regions=3 fits=2 area=3
   # ok: fits == 2
   scripts/check-polyomino-packing.py inputs/Day12.txt 408
   # shapes=6 regions=1000 fits=408 area=408
   # ok: fits == 408
   ```

   The `inputs/...` path form is resolved against
   `src/main/resources/inputs` regardless of the current directory. The printed
   `area=` value is the independent area-feasibility count; for the sample it
   differs from `fits` (area 3 vs fits 2) because the third region is
   area-feasible but geometrically impossible.

4. Read the output. Exit 0 means all checks passed; a non-zero exit names the
   failing gate (self-test mismatch, expected-value mismatch, missing file, or
   malformed input: no shape diagrams, no region lines, or a diagram without a
   `#` cell).

## Algorithm notes

- Backtracking mirror (of `Day12.part1`): presents are placed one at a time and
  the next present is anchored at the first still-empty grid cell, which either
  hosts the top-left corner of some remaining present's placement or is left
  empty. Every distinct orientation of each shape is generated up front via the
  eight dihedral symmetries (deduplicated and normalized) and translated across
  the region. The search is complete, so a negative answer is returned only
  after every reachable placement has been tried. Occupied cells and placements
  are Python integers (bitmasks) so overlap checks and updates are single
  bigint operations.
- Independent exact-cover reference (`fits_dlx`): each copy of each required
  present must be placed exactly once and each grid cell must be covered at
  most once. Exact cover normally requires every column exactly once, so the
  "leave a cell empty" freedom is modelled by giving every cell its own
  singleton skip row (a cell is either covered by exactly one present or by its
  skip row, never both). The recursion branches on the uncovered column with
  the fewest still-usable rows (Knuth's S heuristic). Structurally different
  from the anchored backtracking mirror, so agreement catches
  anchoring/orientation/quantity bugs. Only used on small regions and tiny
  fixtures: the 12x5 sample regions and the real input are far too large for a
  pure-Python exact cover, so the mirror covers those while the exact cover
  validates the mirror on seeded small cases, the 4x4 sample region, and the
  boundary fixtures.
- Area bound (`area_count`): the number of regions whose total present cells
  fit within the region area is an independent, trivial necessary condition. On
  the Day 12 real input it alone equals the accepted answer (408), which
  confirms no area-feasible real region is geometrically impossible — but the
  sample's third region shows the bound is not sufficient in general.
- Parsing: each line matching `N:` starts a shape whose diagram is the next
  three lines (its `#` cells become a set of `(x, y)` offsets), and each line
  matching `WxH: q0 q1 ...` is a region with per-shape quantities, exactly like
  `Day12.readFile` in `src/main/scala/aoc2025/Day12.scala`.

## Evidence and validation

- The script implements the geometric feasibility check two independent ways:
  - `fits_mirror` (mirror of `Day12.part1`): anchored backtracking search over
    every orientation/placement.
  - `fits_dlx` (exact cover with per-cell skip rows): independent Algorithm X
    search with the minimum-remaining-rows column heuristic.
  - 4000 seeded random cases (seed 20261011; 1..4 shapes, region up to 4x4,
    0..2 copies each, shapes up to 5 cells) must agree, including cases that
    are area-feasible but geometrically impossible and cases that exceed the
    area (both reject).
- The official sample has exactly 6 seven-cell shapes and 3 regions: region 0
  (4x4, two shape-4 presents) and region 1 (12x5) fit; region 2 (12x5 with one
  extra shape-4 present) is area-feasible (49 cells in 60) but geometrically
  impossible, so the sample total is 2. Asserted by the mirror, with the exact
  cover independently confirming region 0 and the area-feasible-but-impossible
  property asserted explicitly.
- Boundary fixtures (mirror and exact cover agree): a zero-present region fits;
  more present cells than region area never fits; a 1x3 triomino cannot fit a
  2x2 region despite enough area; two 1x2 dominoes exactly tile a 2x2 region;
  mixed fitting/non-fitting regions are counted separately.
- The real input has exactly 6 shapes and 1000 regions; the mirror computes the
  accepted answer 408 and the independent area-feasibility count is also 408 —
  both asserted.
- Failure cases exercised: wrong expected value (exit 1), missing file (exit
  1), empty input (exit 1), malformed input without shape diagrams (exit 1),
  and usage errors (exit 2).
- `py_compile` clean. Read-only: never submits, never touches the network. The
  full self-test takes about 45 seconds because the real input runs the
  complete geometric search over 1000 regions.

## Stopping conditions

- Stop after the answer is recorded; do not submit answers, create issues, or
  push from this skill.
- For a rejection review, record the unresolved repair and leave the puzzle
  repair to the ProblemSolver on a later CTO cycle.