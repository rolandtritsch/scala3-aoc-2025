---
name: closest-pairs-clustering
description: Connect the K closest pairs of points (3D `X,Y,Z` triples) by straight-line distance using single-linkage clustering, where a pair already in the same circuit does nothing, then multiply the sizes of the three largest resulting circuits; or keep connecting until all points form a single circuit and multiply the X coordinates of the last two points connected. Use when a puzzle asks to connect the closest pairs of junction boxes/points and report the product of the largest component sizes (e.g. Day 08 part 1 "Playground") or the product of the X coordinates of the final merge edge that completes one circuit (Day 08 part 2), or when such a Kruskal-style closest-pairs clustering answer must be re-derived without resubmitting.
---

# Skill: closest-pairs-clustering

Part of the AoC 2025 cycle in this repository. Day 08 is the "closest-pairs
single-linkage clustering" pattern: given n junction boxes in 3D space (one
`X,Y,Z` triple per line), the Elves repeatedly connect the closest pair of
boxes by straight-line distance that are not yet directly connected; a pair
whose boxes are already in the same circuit does nothing. Two variants:

- Part 1: after the `K` closest such connections, the answer is the product of
  the sizes of the three largest circuits (accepted answer 90036 for K=1000 on
  1000 boxes).
- Part 2: keep connecting until all boxes form a single circuit; the answer is
  the product of the X coordinates of the last two boxes connected (the final
  successful merge edge; accepted answer 6083499488, official sample 25272).

Both are effectively Kruskal's algorithm run over the sorted all-pairs edge
list, so the technique recurs in any "connect closest things, count/measure the
resulting clusters" puzzle.

## When to use

- Any puzzle that asks how the clusters grow when the closest pairs of points
  are connected one at a time (single-linkage / minimum-spanning-forest shape).
- Any puzzle that connects the K closest pairs and asks for the sizes (or
  product of the largest sizes) of the resulting circuits.
- Any puzzle that connects closest pairs until everything is one circuit and
  asks about the final merge edge (e.g. the product of coordinates of the last
  two points connected).
- Verifying a committed `part1`/`part2` answer for a closest-pairs day before a
  submit or review disposition (re-derive from the input).
- Re-confirming that a previously claimed answer (e.g. 90036 or 6083499488 for
  Day 08) still holds on the current input without trusting a paraphrase.

## Procedure

1. Work in the synchronized checkout (`scala3-aoc-2025`).
2. Self-test the helper (no network, no submissions; validates two independent
   implementations agree per variant and asserts the accepted real answers):

   ```bash
   scripts/check-closest-pairs.py            # part-1 self-test
   # ok: 2000 random mirror/reference cross-checks, sample == 40, real == 90036
   scripts/check-closest-pairs.py --full-merge    # part-2 self-test
   # ok: 2000 random full-merge cross-checks, sample == 25272, real == 6083499488
   ```

3. Re-derive a day's answer from its input file (asserting an expected value
   when given):

   ```bash
   scripts/check-closest-pairs.py inputs/Day08Test.txt 10 40
   # points=20 connections=10 product=40
   # ok: product == 40
   scripts/check-closest-pairs.py inputs/Day08.txt 1000 90036
   # points=1000 connections=1000 product=90036
   # ok: product == 90036
   scripts/check-closest-pairs.py --full-merge inputs/Day08Test.txt 25272
   # points=20 full_merge_x_product=25272
   # ok: x_product == 25272
   scripts/check-closest-pairs.py --full-merge inputs/Day08.txt 6083499488
   # points=1000 full_merge_x_product=6083499488
   # ok: x_product == 6083499488
   ```

   The `inputs/...` path form is resolved against
   `src/main/resources/inputs` regardless of the current directory.

4. Read the output. Exit 0 means all checks passed; a non-zero exit names the
   failing gate (self-test mismatch, expected-value mismatch, or malformed
   input: a line that is not exactly three integer coordinates).

## Evidence and validation

- The script implements each variant two independent ways and cross-checks
  them: an array-based union-find with path compression (mirrors `Day08.part1`
  and `Day08.part2` in `src/main/scala/aoc2025/Day08.scala`) and a
  small-to-large set-merging reference that relabels circuit members (no parent
  pointers). Random seeded cases (2000, seed 20251011) per variant, including
  K=0, K beyond the pair count, single-box inputs, and identical/duplicate
  points, must agree.
- The part-2 mirror records the X product of every successful merge and stops
  on the first merge whose component spans all n boxes — the exact "last two
  junction boxes you need to connect" edge. The reference stops on the same
  edge because both consume the same sorted pair list.
- Squared Euclidean distance preserves straight-line ordering without
  floating-point error; pairs are sorted by squared distance then `(i, j)` to
  match the stable tie-breaking of the Scala implementation.
- The self-tests assert the official Day 08 sample (10 connections -> 40;
  full merge -> 25272 = 216*117) and the accepted Day 08 real-input answers
  (90036 and 6083499488) from `src/main/resources/inputs/Day08.txt` (resolved
  relative to the script).
- Failure cases exercised: wrong expected value, missing file, malformed line
  (wrong number of coordinates), non-integer coordinate, empty input, negative
  K, and usage errors; each exits non-zero with a labeled message.
- `py_compile` clean. Read-only: never submits, never touches the network.

## Stopping conditions

- Stop after the answer is recorded; do not submit answers, create issues, or
  push from this skill.
- For a rejection review, record the unresolved repair and leave the puzzle
  repair to the ProblemSolver on a later CTO cycle.