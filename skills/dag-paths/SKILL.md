---
name: dag-paths
description: Count the distinct directed paths from one node to another in a finite directed acyclic graph whose edges only ever move forward, when the graph is given as `node: output output ...` wiring lines (e.g. Day 11 "Reactor": part 1 accepted answer 543, part 2 accepted answer 479511112939968). Use when a puzzle asks how many different paths lead from a source to a target through a device/network DAG, when a committed path-count answer must be re-derived from the input without guessing, or when a memoised DFS sum over a DAG must be cross-checked against an independent algorithm. Part two extends the pattern: count only the paths that also visit a fixed set of required devices (in any order).
---

# Skill: dag-paths

Part of the AoC 2025 cycle in this repository. Day 11 is the "count distinct
directed paths in a DAG" pattern: each line names a device followed by the
devices its outputs attach to, data flows only forward through outputs, so the
wiring is a directed acyclic graph. The puzzle asks how many distinct directed
paths lead from the device `you` to the device `out` (part 1) and, moving the
source to `svr`, how many of the `svr -> out` paths also visit both `dac` and
`fft` in any order (part 2).

Because the graph is finite and acyclic, the number of paths from any device to
the target is the sum of the counts of its outputs, with the target itself
contributing a base count of one. This yields an O(v + e) memoised depth-first
traversal read off at the source. For the "must visit every required device"
variant, each required device is assigned a bit and the state is
`(device, visitedMask)`, giving O(v * 2^k + e * 2^k) where `k` is the number of
required devices.

## When to use

- Any puzzle that asks for the number of distinct paths from a start node to an
  end node in a directed acyclic graph (flow never moves backwards).
- The part-two variant: count paths that visit a fixed set of required nodes
  (e.g. `dac` and `fft`) in any order.
- A graph given as `node: node node ...` lines (each node lists its outgoing
  edges), possibly with empty output lists.
- Verifying a committed `part1`/`part2` answer for a DAG path-counting day
  before a submit or review disposition (re-derive from the input, not just the
  total).
- Re-confirming that a previously claimed answer (e.g. 543 for Day 11 part 1,
  479511112939968 for Day 11 part 2) still holds on the current input without
  trusting a paraphrase.
- Checking that an input is actually acyclic when the algorithm relies on it:
  a cycle reachable from the source would make the naive memoised DFS recurse
  forever, so confirm acyclicity (or detect the cycle) before trusting the sum.

## Procedure

1. Work in the synchronized checkout (`scala3-aoc-2025`).
2. Self-test the helper (no network, no submissions; validates two independent
   implementations agree and asserts the accepted real answers):

   ```bash
   scripts/check-dag-paths.py
   # ok: 4000 memo/enum cross-checks, sample == 5, real == 543

   scripts/check-dag-paths.py --visits-all
   # ok: 3000 memo/IE cross-checks, part2 sample == 2, real == 479511112939968
   ```

3. Re-derive a day's answer from its input file (asserting an expected value
   when given):

   ```bash
   scripts/check-dag-paths.py inputs/Day11Test.txt 5
   # devices=10 paths=5
   # ok: paths == 5
   scripts/check-dag-paths.py inputs/Day11.txt 543
   # devices=581 paths=543
   # ok: paths == 543

   scripts/check-dag-paths.py --visits-all inputs/Day11.txt 479511112939968
   # devices=581 required_paths=479511112939968
   # ok: required_paths == 479511112939968
   ```

   The `inputs/...` path form is resolved against
   `src/main/resources/inputs` regardless of the current directory.

4. Read the output. Exit 0 means all checks passed; a non-zero exit names the
   failing gate (self-test mismatch, expected-value mismatch, missing file, or
   malformed input: a line without a `:` separator or an empty input).

## Algorithm notes

- Memoised DFS (mirror of `Day11.part1`): `count(node) = 1` when `node` is the
  target, else the sum of `count(output)` over the node's outputs. Memoising
  each node's count keeps the whole traversal O(v + e); an unreachable source
  or a node with no path to the target contributes 0.
- Explicit enumeration reference (independent): a plain DFS that walks every
  distinct path to the target and counts them one by one, never reusing a
  sub-count. It is structurally different from the memoised sum, so it catches
  summation/memoisation mistakes. Only usable on small DAGs and tiny fixtures
  (path counts explode combinatorially), which is why the self-test restricts
  it to generated graphs of up to 12 devices.
- Subset-mask DFS (mirror of `Day11.part2`): a path's state is the bitmask of
  required devices visited so far; at the target a path counts only when every
  required bit is set. Memoised on `(device, visitedMask)`.
- Inclusion-exclusion reference (independent, part 2): the paths visiting all
  required devices equal `sum_{S subset of required} (-1)^|S| * paths_avoiding(S)`,
  where "paths avoiding a set" prunes those devices from the graph and counts
  plain paths. Structurally different from the mask DFS, so agreement catches
  bit-mask/summation mistakes.
- Parsing: split each non-empty line at its first `:` into a device name and a
  whitespace-separated output list (which may be empty), exactly like
  `Day11.readFile` in `src/main/scala/aoc2025/Day11.scala`.

## Evidence and validation

- The script implements each computation two independent ways:
  - Part 1: `count_paths_memo` (mirror of `Day11.part1`) vs `count_paths_enum`
    (explicit enumeration). 4000 seeded random cases (seed 20261011; 3–12
    devices, random acyclic wiring with `you` first and `out` last) must agree,
    including cases where `you` cannot reach `out` (both count 0).
  - Part 2: `count_required_memo` (mirror of `Day11.part2`) vs
    `count_required_ie` (inclusion-exclusion). 3000 seeded random cases (seed
    20261011; 4–12 devices, `svr` first, required devices `dac`/`fft`, `out`
    last) must agree.
- The parsed part-1 sample has exactly 10 devices and 5 distinct paths (the
  statement's own `you -> bbb -> ddd -> ggg -> out`, `you -> bbb -> eee ->
  out`, `you -> ccc -> ddd -> ggg -> out`, `you -> ccc -> eee -> out`,
  `you -> ccc -> fff -> out`), and the real input has exactly 581 devices
  totalling the accepted answer 543 — asserted by both implementations
  independently.
- The embedded part-2 sample has exactly 13 devices and exactly 2 `svr -> out`
  paths visit both `dac` and `fft`; the real input totals the accepted answer
  479511112939968 — asserted by both implementations independently.
- Failure cases exercised: wrong expected value (exit 1), missing file (exit
  1), malformed line — no `:` separator (exit 1), empty input (exit 1), and
  usage errors (exit 2).
- `py_compile` clean. Read-only: never submits, never touches the network.

## Stopping conditions

- Stop after the answer is recorded; do not submit answers, create issues, or
  push from this skill.
- For a rejection review, record the unresolved repair and leave the puzzle
  repair to the ProblemSolver on a later CTO cycle.
