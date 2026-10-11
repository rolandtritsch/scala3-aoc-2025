---
name: closest-pairs-clustering
description: Connect the K closest pairs of points (3D `X,Y,Z` triples) by straight-line distance using single-linkage clustering, where a pair already in the same circuit does nothing, then multiply the sizes of the three largest resulting circuits. Use when a puzzle asks to connect the closest pairs of junction boxes/points and report the product of the largest component sizes (e.g. Day 08 part 1 "Playground"), or when such a Kruskal-style closest-pairs clustering answer must be re-derived without resubmitting.
---

# Skill: closest-pairs-clustering

Part of the AoC 2025 cycle in this repository. Day 08 is the "closest-pairs
single-linkage clustering" pattern: given n junction boxes in 3D space (one
`X,Y,Z` triple per line), the Elves repeatedly connect the closest pair of
boxes by straight-line distance that are not yet directly connected; a pair
whose boxes are already in the same circuit does nothing. After the `K`
closest such connections, the answer is the product of the sizes of the three
largest circuits (part 1, accepted answer 90036 for K=1000 on 1000 boxes).
This is effectively Kruskal's algorithm run over the sorted all-pairs edge
list, stopping after `K` edges, so the technique recurs in any "connect closest
things, count/measure the resulting clusters" puzzle.

## When to use

- Any puzzle that asks how the clusters grow when the closest pairs of points
  are connected one at a time (single-linkage / minimum-spanning-forest shape).
- Any puzzle that connects the K closest pairs and asks for the sizes (or
  product of the largest sizes) of the resulting circuits.
- Verifying a committed `part1` answer for a closest-pairs day before a submit
  or review disposition (re-derive the product from the input).
- Re-confirming that a previously claimed product (e.g. 90036 for Day 08) still
  holds on the current input without trusting a paraphrase.

## Procedure

1. Work in the synchronized checkout (`scala3-aoc-2025`).
2. Self-test the helper (no network, no submissions; validates two independent
   implementations agree and asserts the accepted real answer):

   ```bash
   scripts/check-closest-pairs.py            # self-test
   # ok: 2000 random mirror/reference cross-checks, sample == 40, real == 90036
   ```

3. Re-derive a day's product from its input file and connection count (asserting
   an expected value when given):

   ```bash
   scripts/check-closest-pairs.py inputs/Day08Test.txt 10 40
   # points=20 connections=10 product=40
   # ok: product == 40
   scripts/check-closest-pairs.py inputs/Day08.txt 1000 90036
   # points=1000 connections=1000 product=90036
   # ok: product == 90036
   ```

   The `inputs/...` path form is resolved against
   `src/main/resources/inputs` regardless of the current directory.

4. Read the output. Exit 0 means all checks passed; a non-zero exit names the
   failing gate (self-test mismatch, expected-value mismatch, or malformed
   input: a line that is not exactly three integer coordinates).

## Evidence and validation

- The script implements the clustering two independent ways and cross-checks
  them: an array-based union-find with path compression (mirrors `Day08.part1`
  in `src/main/scala/aoc2025/Day08.scala`) and a small-to-large set-merging
  reference that relabels circuit members (no parent pointers). Random seeded
  cases (2000, seed 20251011), including K=0, K beyond the pair count, and
  identical/duplicate points, must agree.
- Squared Euclidean distance preserves straight-line ordering without
  floating-point error; pairs are sorted by squared distance then `(i, j)` to
  match the stable tie-breaking of the Scala implementation.
- The self-test asserts the official Day 08 sample (10 connections -> circuits
  of sizes 5,4,2,2 and seven singletons -> 5*4*2 = 40) and the accepted Day 08
  real-input answer (90036, 1000 connections over 1000 boxes) from
  `src/main/resources/inputs/Day08.txt` (resolved relative to the script).
- Failure cases exercised: wrong expected value, missing file, malformed line
  (wrong number of coordinates), non-integer coordinate, empty input, negative
  K, and usage errors; each exits non-zero with a labeled message.
- `py_compile` clean. Read-only: never submits, never touches the network.

## Stopping conditions

- Stop after the product is recorded; do not submit answers, create issues, or
  push from this skill.
- For a rejection review, record the unresolved repair and leave the puzzle
  repair to the ProblemSolver on a later CTO cycle.