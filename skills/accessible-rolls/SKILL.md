---
name: accessible-rolls
description: Solve "count/peel cells in a grid whose occupancy is below a neighbour threshold" problems (8-directional king-move neighbourhood, no wrapping). Use when a puzzle asks how many cells are accessible because fewer than K of their eight adjacent positions are occupied, and/or how many cells can be removed by repeatedly removing every currently-accessible cell until none remain (e.g. Day 04 part 1 "rolls of paper a forklift can access", and the Day 04 part 2 cascade it feeds). Provides a validated counting rule, the cascade fixpoint, a deterministic cross-check script, and the Day 04 evidence.
---

# Skill: accessible-rolls

Distilled from the Day 04 part 1 review (AOC-116, reviewed commit 78c0f54,
accepted answer 1356). The puzzle: rolls of paper (`@`) sit on a grid (`.` is
empty). A forklift can access a roll only when fewer than four rolls occupy its
eight adjacent (king-move) positions. Part 1 counts accessible rolls; part 2
removes accessible rolls, which can make more rolls accessible, and repeats.

Generalized, this is the "occupancy-threshold neighbourhood" pattern: a cell is
*accessible* when the number of occupied cells in its 8-neighbourhood is below
a threshold `K`. It appears in AoC in several guises (accessible cells, edge /
bubble peeling, cascading removals).

## When to use

- A puzzle says a cell/roll/item is usable when fewer than `K` of its eight
  adjacent positions contain something, and asks how many are usable.
- A puzzle peels a grid by repeatedly removing all currently-eligible cells
  until a fixpoint (e.g. "how many can be removed in total if we keep
  repeating"). The per-round eligibility rule is often the same threshold.
- Cross-checking such a solution before a submit or review disposition.

## Counting rule (Day 04 part 1)

A roll at `(r, c)` is accessible exactly when fewer than four of its eight
king-move neighbours (all `(dr, dc)` in `[-1,1]^2` except `(0,0)`) are inside
the grid and hold `@`. Neighbourhood does not wrap around grid edges. Each cell
is checked once; O(R*C) time, O(1) extra space.

Reference implementation: `src/main/scala/aoc2025/Day04.scala`
(`adjacentRolls` + `part1`), plus a Python mirror in
`scripts/check-accessible-rolls.py`.

## Cascade rule (Day 04 part 2)

Once a roll is accessible it is removed (becomes `.`), which can lower the
neighbour counts around it and make further rolls accessible, and so on. The
total number of rolls that can ever be removed is a **unique fixpoint**:
removing all currently-accessible rolls in rounds, or removing them one at a
time in any order, yields the same total. This monotonicity lets one simulate
either way and cross-check the other.

Reference implementation for the cascade is not yet in the Scala sources (part 2
is not yet assigned); `scripts/check-accessible-rolls.py` provides a validated
round-based simulator plus an independent one-at-a-time reference.

## Cross-check / validation

Deterministic, read-only helper: `scripts/check-accessible-rolls.py`.

- `python3 scripts/check-accessible-rolls.py` (or `--self-test`) — seeded
  (20251010) random cross-check: 1000 grids up to 8x8, counting via two
  implementations and peel via round-based vs one-at-a-time removal; then
  asserts the official Day 04 sample (count 13, peel 43). Exit 0 = all pass.
- `python3 scripts/check-accessible-rolls.py <FILE> [COUNT] [PEEL]` — reads a
  grid of `@`/`.` (one row per non-blank line), prints the accessible count and
  the peel total, and asserts each when given. Examples that re-derive known
  answers:
  - `python3 scripts/check-accessible-rolls.py src/main/resources/inputs/Day04Test.txt 13 43` (official sample)
  - `python3 scripts/check-accessible-rolls.py src/main/resources/inputs/Day04.txt 1356` (accepted part-1 answer; the peel total prints as informational only, part 2 is not yet assigned)
- Failure cases (all exit 1 or 2): missing file, empty grid, characters other
  than `@`/`.`, ragged rows, wrong `COUNT`/`PEEL`, unknown usage.

Never submit or push from this helper; it is read-only and local. The
real-input peel total is informational for the later part-2 cycle, not an
answer to submit.

## Edge cases to cover in tests

- Single roll in a 1x1 grid: accessible (0 neighbours).
- Edge/corner cells: neighbourhood does not wrap.
- All-occupied 3x3 block: only the four corners are accessible (3 neighbours
  each); peel removes 9 in total (4 corners, then 4 edge middles, then centre).
- Empty grid: count 0, peel 0.
- Threshold boundary: exactly `K` neighbours is NOT accessible (`< K`, not
  `<= K`).

## Stopping conditions

- Stop after the cross-check/validation result is recorded. Do not create
  issues, retry submissions, or push from this skill.
- If the cross-check disagrees with the committed solution, record the exact
  mismatch as an unresolved repair for the solver on a later CTO cycle.