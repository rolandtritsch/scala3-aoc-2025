---
name: beam-splits
description: Count beam splits in a vertical-propagating grid where a beam enters at `S` on the top row, moves only downward, passes through `.`, is stopped by a `^` splitter and replaced by two beams emitted to the cells immediately left and right of the splitter, and beams merging into the same cell are deduplicated (e.g. Day 07 part 1 "tachyon manifold", accepted answer 1605). Use when a puzzle has a single downward-moving beam/ray that splits at marker cells with deduplication of merged beams, or when a committed vertical beam-splitting answer must be re-derived without resubmitting.
---

# Skill: beam-splits

Distilled from the Day 07 part 1 review (AOC-161, reviewed commit
03e6d78a9e1c5db51dc8dd92a9db5b133d8e1245, accepted answer 1605). The puzzle: a
tachyon beam enters a manifold diagram at the cell marked `S` on the top row and
always moves downward. Beams pass freely through empty space (`.`); a beam that
reaches a splitter (`^`) is stopped and replaced by two new beams emitted from
the cells immediately to the left and to the right of the splitter, which also
move downward. Beams that merge into the same cell are treated as a single
beam. Part 1 counts the total number of times any beam is split.

Generalized, this is the "downward beam splitter" pattern: one directional
propagator that splits 1-to-2 at a marker and requires deduplication when
branches converge on the same cell.

## When to use

- A puzzle has a single beam/ray that enters a grid at `S` (top row), moves only
  downward, and a `^`-like marker stops it and emits left/right successors.
- A puzzle requires treating beams that merge into the same cell as a single
  beam (deduplicate converged states).
- Verifying a committed `part1` answer for a beam-splitting day before a submit
  or review disposition (re-derive the split count from the input).
- Re-confirming that a previously claimed split count (1605 for Day 07 part 1,
  21 for the official sample) still holds on the current input without trusting
  a paraphrase.

## Counting rule

Keep the set of active columns that a downward beam currently occupies, walking
rows top to bottom. At each row:

- A `.` (or `S`) cell carries the beam into the same column on the next row.
- A `^` cell stops the beam, records one split, and emits beams into columns
  `c-1` and `c+1` on the next row.
- Columns outside the grid are discarded (the beam exits the manifold).
- The set deduplicates beams that merge into the same cell.

With an `h`-row by `w`-column grid this is O(h*w) time and O(w) space. Reference
implementation: `src/main/scala/aoc2025/Day07.scala` (`part1`), plus a Python
mirror in `scripts/check-beam-splits.py` (row-scan) that is cross-checked
against an independent BFS-over-occupied-cells reference.

## Cross-check / validation

Deterministic, read-only helper: `scripts/check-beam-splits.py`.

- `python3 scripts/check-beam-splits.py` (or `--self-test`) — seeded random
  cross-check of two independent implementations (row-by-row active-column set
  mirroring `Day07.part1`, and a breadth-first search over occupied cells
  visiting each merged cell once) on 2000 generated grids; then asserts the
  official Day 07 sample (21) and the accepted Day 07 real-input answer (1605).
  Exit 0 = all pass.
- `python3 scripts/check-beam-splits.py <FILE> [EXPECTED]` — reads a manifold
  grid (one row per non-blank line; only `.`, `^`, exactly one `S` in the top
  row), prints the beam-split count, and asserts it when given. Examples:
  - `python3 scripts/check-beam-splits.py src/main/resources/inputs/Day07Test.txt 21` (official sample)
  - `python3 scripts/check-beam-splits.py src/main/resources/inputs/Day07.txt 1605` (accepted answer)
- Failure cases (all exit 1 or 2): missing file, empty grid, characters other
  than `.`/`^`/`S`, ragged rows, missing or multiple `S`, `S` not on the top
  row, wrong `EXPECTED`, unknown usage.

Never submit or push from this helper; it is read-only and local.

## Edge cases to cover in tests

- No splitter: 0 splits.
- A splitter emitting one beam off the left/right edge (the off-grid beam is
  discarded) still counts 1 split.
- Two splitters both dumping beams into the same centre cell: the merged beams
  are deduplicated before the next row, so each splitter still counts once.
- Beams must not wrap around the grid edges.
- The official sample and the real input must match the accepted answers.

## Stopping conditions

- Stop after the cross-check/validation result is recorded. Do not create
  issues, retry submissions, or push from this skill.
- If the cross-check disagrees with the committed solution, record the exact
  mismatch as an unresolved repair for the solver on a later CTO cycle.
