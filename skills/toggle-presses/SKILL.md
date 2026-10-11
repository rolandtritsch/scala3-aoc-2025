---
name: toggle-presses
description: Find the fewest button presses to configure indicator lights (or toggles) when each button toggles a fixed subset and pressing twice cancels out, i.e. the minimum-weight subset of GF(2) toggle masks whose XOR equals a target mask, summed over many machines/objects (e.g. Day 10 part 1 "Factory", accepted answer 417). Use when a puzzle asks for the fewest presses/toggles to reach a target set of on/off states where each control toggles a listed subset and double-pressing restores the prior state, when a toggle-XOR (lights-out style) minimal-subset answer must be re-derived from the input without guessing, or when a committed answer must be double-checked without resubmitting.
---

# Skill: toggle-presses

Part of the AoC 2025 cycle in this repository. Day 10 is the "button-toggle
minimum presses" pattern: each machine line lists a target indicator-light
diagram `[....]` (light `i` on = `#`) and button wiring schematics `(a,b,c)`
(each button toggles the listed lights). Because pressing a button twice
restores the prior state at twice the cost, an optimal configuration presses
every button at most once: minimize the number of pressed buttons whose XOR of
toggle masks equals the target light pattern. The puzzle answer is that
per-machine minimum summed over every machine.

In bit terms, every button is a mask (bit `i` set when it toggles light `i`)
and the target is a mask: find `min |S|` such that
`XOR(button[i] for i in S) == target` — the minimum-weight solution of a linear
system over GF(2).

## When to use

- Any puzzle that asks for the fewest presses/toggles to reach a target set of
  on/off indicator states, where each button/control toggles a listed subset
  and pressing the same button twice restores the previous state.
- The lights-out family: binary state per cell, controls flip a fixed subset,
  press parity is all that matters (`press(button) twice == press(0)`).
- Any puzzle where the answer reduces to a minimal subset whose XOR equals a
  target mask and the input may be parsed back from the statement's
  `[diagram] (button) ... {ignored}` line format.
- Verifying a committed `part1` answer for a toggle-press day before a submit
  or review disposition (re-derive from the input, per-machine minima, not just
  the total).
- Re-confirming that a previously claimed answer (e.g. 417 for Day 10 part 1)
  still holds on the current input without trusting a paraphrase.

## Procedure

1. Work in the synchronized checkout (`scala3-aoc-2025`).
2. Self-test the helper (no network, no submissions; validates two independent
   implementations agree and asserts the accepted real answer):

   ```bash
   scripts/check-toggle-presses.py
   # ok: 2000 random enum/bfs cross-checks, sample == 7, real == 417
   ```

3. Re-derive a day's answer from its input file (asserting an expected value
   when given), which also prints the per-machine minima:

   ```bash
   scripts/check-toggle-presses.py inputs/Day10Test.txt 7
   # machines=3 per_machine=[2, 3, 2] total=7
   # ok: total == 7
   scripts/check-toggle-presses.py inputs/Day10.txt 417
   # machines=163 per_machine=[1, 5, 1, ...] total=417
   # ok: total == 417
   ```

   The `inputs/...` path form is resolved against
   `src/main/resources/inputs` regardless of the current directory.

4. Read the output. Exit 0 means all checks passed; a non-zero exit names the
   failing gate (self-test mismatch, expected-value mismatch, missing file, or
   malformed input: a line without a leading `[diagram]` or with an unterminated
   button group or a non-integer light id).

## Algorithm notes

- Subset enumeration (mirror of `Day10.part1`): for each machine, iterate every
  `2^b` subset of its `b` buttons (`b <= 13` here), track the running XOR and
  weight, and keep the smallest weight whose XOR equals the target. O(b * 2^b)
  per machine — trivial for the puzzle's sizes.
- BFS reference (independent): the state is the current on-light mask; pressing
  a button XORs its mask into the state at cost 1. The minimum presses from the
  all-off state `0` to the target is the BFS distance. This needs no subset
  enumeration at all and never presses a button twice (a repeat is a self-loop
  at extra cost), so it catches weight/XOR mistakes in the eventual subset
  enumeration.
- Parsing: the bracketed diagram sets bit `i` when light `i` is `#`; each
  parenthesised group becomes a mask via `1 << id` XOR-combined (a light listed
  twice XORs back out, exactly like `Day10.readFile`); braced joltages are
  ignored for part 1.

## Evidence and validation

- The script implements the computation two independent ways:
  - `min_presses_enum`: full 2^b subset enumeration mirroring `Day10.part1` in
    `src/main/scala/aoc2025/Day10.scala`.
  - `min_presses_bfs`: BFS over the XOR state graph (independent, structurally
    different algorithm).
  2000 seeded random cases (seed 20261011; 1–9 lights, 0–11 buttons, random
  targets including unreachable ones) must agree, including the unreachable
  `-1` case.
- The parsed sample has exactly 3 machines with per-machine minima `[2, 3, 2]`
  and total 7 (the statement's "2 + 3 + 2 = 7"), and the real input has
  exactly 163 machines totalling the accepted answer 417 — asserted by both
  implementations independently.
- Failure cases exercised: wrong expected value (exit 1), missing file (exit
  1), malformed line — no `[diagram]`, unterminated button group
  (exit 1), empty input (exit 1), and usage errors (exit 2).
- `py_compile` clean. Read-only: never submits, never touches the network.

## Stopping conditions

- Stop after the answer is recorded; do not submit answers, create issues, or
  push from this skill.
- For a rejection review, record the unresolved repair and leave the puzzle
  repair to the ProblemSolver on a later CTO cycle.