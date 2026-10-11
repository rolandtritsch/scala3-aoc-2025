---
name: joltage-counters
description: Find the fewest button presses to drive numeric counters to exact targets when each button increments a fixed subset of counters by 1 per press, i.e. the minimum-weight non-negative integer solution of a 0/1 linear system A x = b, summed over many machines (e.g. Day 10 part 2 "Factory" joltage counters, accepted answer 16765). Use when a puzzle asks for the fewest presses to reach exact additive targets where each control adds a fixed 1 to a listed subset (no overshoot, no cancellation), when such a minimum must be re-derived from the input without guessing, or when a committed answer must be double-checked without resubmitting.
---

# Skill: joltage-counters

Part of the AoC 2025 cycle in this repository. Day 10 part 2 is the "additive
joltage counters" pattern: each machine line lists button wiring schematics
`(a,b)` and joltage requirements `{v0,v1,...}`. Ignoring the light diagram,
each button increments its named counters by exactly 1 per press, all counters
start at 0, and the goal is the fewest total presses to reach the exact target
joltage vector. Unlike part 1's toggles, presses do not cancel, so a button may
need to be pressed many times.

In integer-programming terms, with column `j` of the 0/1 incidence matrix `A`
being button `j`'s mask and `b` the joltage target vector, this is

```
minimise   sum_j x_j
subject to A x = b,   x_j >= 0 integer
```

The per-machine minimum sums to the puzzle answer.

## When to use

- Any puzzle that asks for the fewest presses to reach exact additive counter
  targets, where each button/control adds a fixed 1 to a listed subset and
  overshooting a counter is not allowed.
- The "subset-multiset cover" family: target is a multiset of counter counts,
  each control adds a listed sub-multiset of unit increments, minimise the
  total number of controls used.
- Verifying a committed `part2` answer for a joltage-counter day before a
  submit or review disposition (re-derive from the input, per-machine minima,
  not just the total).
- Re-confirming that a previously claimed answer (e.g. 16765 for Day 10 part 2)
  still holds on the current input without trusting a paraphrase.

## Procedure

1. Work in the synchronized checkout (`scala3-aoc-2025`).
2. Self-test the helper (no network, no submissions; validates the
   branch-and-bound mirror against an independent brute-force ground truth and
   asserts the accepted real answer):

   ```bash
   scripts/check-joltage-counters.py
   # ok: 1500 small + 200 medium bnb/brute cross-checks, sample == 33, real == 16765
   ```

3. Re-derive a day's answer from its input file (asserting an expected value
   when given), which also prints the per-machine minima:

   ```bash
   scripts/check-joltage-counters.py inputs/Day10Test.txt 33
   # machines=3 per_machine=[10, 12, 11] total=33
   # ok: total == 33
   scripts/check-joltage-counters.py inputs/Day10.txt 16765
   # machines=163 per_machine=[...] total=16765
   # ok: total == 16765
   ```

   The `inputs/...` path form is resolved against
   `src/main/resources/inputs` regardless of the current directory.

4. Read the output. Exit 0 means all checks passed; a non-zero exit names the
   failing gate (self-test mismatch, expected-value mismatch, missing file, or
   malformed input: a line without a leading `[diagram]`, an unterminated
   button group or joltage block, a non-integer counter id or joltage, no
   buttons, no joltage requirements, or an empty file).

## Algorithm notes

- Branch-and-bound mirror (of `Day10.part2` / `CounterSolver` in
  `src/main/scala/aoc2025/Day10.scala`): solve the LP relaxation `min sum x,
  A x = b, x >= 0` exactly over rationals with a two-phase simplex (Bland's
  rule, `fractions.Fraction`), branch on the first fractional variable (floor
  cap vs ceiling raise), and prune once the relaxation lower bound meets the
  incumbent integer solution. Each button is capped at the smallest target
  among the counters it touches (pressing more would overshoot that counter).
- Brute-force reference (independent): enumerate every press-count vector
  within the per-button upper bounds and keep the lightest exact solution.
  Tractable only for small instances; it is the ground truth the mirror is
  cross-checked against on generated machines.
- Parsing mirrors `Day10.readFile`: the leading `[diagram]` is skipped for
  part 2, each `(a,b)` group becomes a mask with bit `id` set, and the braced
  list is the joltage target vector.

## Evidence and validation

- The script implements the computation two independent ways:
  - `min_presses_bnb`: exact branch-and-bound over the rational LP relaxation,
    conceptually mirroring `Day10.part2` in `src/main/scala/aoc2025/Day10.scala`.
  - `min_presses_brute`: exhaustive press-count enumeration (structurally
    different ground-truth algorithm).
  1500 seeded small machines (seed 20261012; 1–3 counters, 1–4 buttons, targets
  0–5) and 200 medium machines (2–4 counters, 3–5 buttons, targets 0–7) must
  agree, including unreachable `-1` cases (a counter no remaining button covers).
- The parsed sample has exactly 3 machines with per-machine minima
  `[10, 12, 11]` and total 33 (the statement's "10 + 12 + 11 = 33"), and the
  real input has exactly 163 machines totalling the accepted answer 16765.
- Failure cases exercised: wrong expected value (exit 1), missing file (exit
  1), malformed line — no `[diagram]`, unterminated button group, unterminated
  joltage block, non-integer id/joltage, no buttons, no joltages, empty input
  (exit 1), and usage errors (exit 2).
- `py_compile` clean. Read-only: never submits, never touches the network.

## Stopping conditions

- Stop after the answer is recorded; do not submit answers, create issues, or
  push from this skill.
- For a rejection review, record the unresolved repair and leave the puzzle
  repair to the ProblemSolver on a later CTO cycle.