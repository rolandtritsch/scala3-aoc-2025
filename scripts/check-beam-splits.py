#!/usr/bin/env python3
"""check-beam-splits.py - validate and apply the vertical beam-splitting
technique distilled from the Day 07 review.

Deterministic, read-only helper. The puzzle: a tachyon beam enters a manifold
grid at the cell marked `S` on the top row and always moves downward. Beams
pass freely through empty space (`.`), but a beam that reaches a splitter (`^`)
is stopped and replaced by two new beams emitted from the cells immediately to
the left and to the right of the splitter, which also move downward. Multiple
beams merging into the same cell are treated as a single beam. Part 1 counts
the total number of times any beam is split.

The many-worlds variant (Day 07 part 2) instead sends a single quantum particle
that takes BOTH paths at every splitter: each splitter doubles the number of
timelines, and timelines that converge on the same cell are NOT deduplicated.
The result is the total number of distinct timelines the particle completes.

Two independent implementations must agree per mode:
  split_count_rowscan        row-by-row set of active columns (mirrors Day07.part1)
  split_count_bfs            breadth-first search over occupied (row, col) cells
  timeline_count_manyworlds  row-by-row per-column multiplicities (mirrors Day07.part2)
  timeline_count_memo        recursive memoized path counting (independent reference)

Modes:
  no args / --self-test     Seeded random cross-check of the two split-count
                            implementations on generated grids, then asserts
                            the official Day 07 sample (21) and the accepted
                            Day 07 real-input answer (1605).
  --many-worlds             Switch to the part-2 timeline-count mode: cross-check
                            the two timeline implementations, then assert the
                            official sample (40) and the accepted real-input
                            answer (29893386035180). Use it together with the
                            self-test/file modes below.
  <FILE> [EXPECTED]         Read a manifold grid (one row per non-blank line),
                            print the beam-split count, and assert it when
                            EXPECTED is given (or the timeline count with
                            --many-worlds).

Usage:
  scripts/check-beam-splits.py
  scripts/check-beam-splits.py --self-test
  scripts/check-beam-splits.py --many-worlds
  scripts/check-beam-splits.py inputs/Day07.txt 1605
  scripts/check-beam-splits.py inputs/Day07Test.txt 21
  scripts/check-beam-splits.py --many-worlds inputs/Day07.txt 29893386035180
  scripts/check-beam-splits.py --many-worlds inputs/Day07Test.txt 40

Exit codes:
  0  all checks passed
  1  a check failed or input is malformed
  2  usage error
"""

import os
import random
import sys
from collections import deque
from functools import lru_cache

SAMPLE_SPLITS = 21
REAL_SPLITS = 1605
SAMPLE_TIMELINES = 40
REAL_TIMELINES = 29893386035180


def real_input_path():
    """Path to the Day 07 real input, resolved relative to this script."""
    here = os.path.dirname(os.path.abspath(__file__))
    return os.path.join(here, "..", "src", "main", "resources", "inputs", "Day07.txt")


def parse_grid(text):
    """Parse a manifold diagram into its rectangular grid of rows.

    Every non-blank line is one row; all rows must have equal width and contain
    only '.', '^' and exactly one 'S' in the top row. Mirrors `Day07.readFile`
    plus the shape/start checks in `Day07.part1` in
    src/main/scala/aoc2025/Day07.scala.
    """
    rows = [line.rstrip("\n") for line in text.splitlines()]
    rows = [row for row in rows if row.strip() != ""]
    if not rows:
        raise ValueError("empty grid")
    width = len(rows[0])
    if any(len(row) != width for row in rows):
        raise ValueError("ragged rows (rows must all have equal width)")
    if any(c not in ".^S" for row in rows for c in row):
        raise ValueError("unexpected character (only '.', '^', 'S' allowed)")
    if "S" not in rows[0]:
        raise ValueError("no start cell 'S' in the top row")
    if sum(row.count("S") for row in rows) != 1:
        raise ValueError("expected exactly one start cell 'S'")
    return rows


def split_count_rowscan(grid):
    """Row-by-row set of active downward columns.

    Each row advances every active beam: a '.'/'S' cell carries the beam into
    the same column on the next row, while a '^' splitter stops the beam,
    records one split, and emits beams into the neighbouring columns. Columns
    outside the grid are discarded (the beam exits the manifold), and the set
    deduplicates beams that merge into a cell. Mirrors `Day07.part1` in
    src/main/scala/aoc2025/Day07.scala.
    """
    width = len(grid[0])
    active = {grid[0].index("S")}
    splits = 0
    for row in grid:
        nxt = set()
        for column in active:
            if row[column] == "^":
                splits += 1
                nxt.add(column - 1)
                nxt.add(column + 1)
            else:
                nxt.add(column)
        active = {column for column in nxt if 0 <= column < width}
    return splits


def split_count_bfs(grid):
    """Independent reference: breadth-first search over occupied cells.

    A beam occupying cell (r, c) moves down to (r+1, c); a splitter at (r, c)
    instead counts one split and occupies (r+1, c-1) and (r+1, c+1). Cells
    reached by any beam are visited once, which is exactly the "merged beams
    are treated as a single beam" rule. Counts each splitter the moment it is
    reached.
    """
    height, width = len(grid), len(grid[0])
    start = (0, grid[0].index("S"))
    seen = {start}
    queue = deque([start])
    splits = 0
    while queue:
        r, c = queue.popleft()
        if grid[r][c] == "^":
            splits += 1
            for nc in (c - 1, c + 1):
                nr = r + 1
                if 0 <= nc < width and nr < height and (nr, nc) not in seen:
                    seen.add((nr, nc))
                    queue.append((nr, nc))
        else:
            nr = r + 1
            if nr < height and (nr, c) not in seen:
                seen.add((nr, c))
                queue.append((nr, c))
    return splits


def random_grid(seed):
    """Deterministic random grid with S in the top row and scattered '^'."""
    rng = random.Random(seed)
    height = rng.randint(1, 9)
    width = rng.randint(1, 9)
    rows = []
    for r in range(height):
        row = "".join("^" if rng.random() < 0.35 else "." for _ in range(width))
        rows.append(row)
    s_col = rng.randrange(width)
    rows[0] = rows[0][:s_col] + "S" + rows[0][s_col + 1:]
    return rows


def timeline_count_manyworlds(grid):
    """Many-worlds timeline count: row-by-row per-column multiplicities.

    Under the many-worlds interpretation a single particle takes both the left
    and the right path of every splitter it reaches, so each timeline forks
    into two. Unlike part 1, timelines that converge on the same cell are never
    deduplicated: two different decision sequences ending at the same cell are
    two distinct timelines. Each column carries a multiplicity of distinct
    timelines currently at that cell; off-grid branches exit the manifold and
    end their timeline; every timeline surviving to the bottom row also exits
    there. Mirrors `Day07.part2` in src/main/scala/aoc2025/Day07.scala.
    """
    width = len(grid[0])
    counts = {grid[0].index("S"): 1}
    exited = 0
    for row in grid:
        nxt = {}
        for col, mult in counts.items():
            targets = (col - 1, col + 1) if row[col] == "^" else (col,)
            for target in targets:
                if target < 0 or target >= width:
                    exited += mult
                else:
                    nxt[target] = mult + nxt.get(target, 0)
        counts = nxt
    return exited + sum(counts.values())


def timeline_count_memo(grid):
    """Independent reference: recursive memoized path counting.

    timeline(r, c) = number of distinct journeys (decision sequences) a single
    particle completes starting from cell (r, c). A particle past the bottom
    row, or emitted off the left/right edge, ends exactly one timeline; a
    splitter forks into the two neighbouring columns; otherwise the particle
    continues one row down. Memoized over (r, c) this is O(h*w) time/space and
    counts paths, so converging timelines are naturally NOT deduplicated —
    independently of the multiplicity propagation in timeline_count_manyworlds.
    """
    height, width = len(grid), len(grid[0])

    @lru_cache(maxsize=None)
    def timeline(r, c):
        if r >= height:
            return 1
        if c < 0 or c >= width:
            return 1
        if grid[r][c] == "^":
            return timeline(r + 1, c - 1) + timeline(r + 1, c + 1)
        return timeline(r + 1, c)

    return timeline(0, grid[0].index("S"))


def self_test():
    """Deterministic validation: seeded random cross-checks + official sample
    + the accepted Day 07 real-input answer (1605 splits; with --many-worlds,
    40 / 29893386035180 timelines)."""
    cases = 2000
    for seed in range(cases):
        grid = random_grid(seed)
        a = split_count_rowscan(grid)
        b = split_count_bfs(grid)
        if a != b:
            print(f"FAIL: grid (seed {seed}) -> {a} != {b}", file=sys.stderr)
            return 1
        m = timeline_count_manyworlds(grid)
        n = timeline_count_memo(grid)
        if m != n:
            print(f"FAIL: grid (seed {seed}) manyworlds {m} != memo {n}",
                  file=sys.stderr)
            return 1

    # Official Day 07 sample: 21 splits for the 16-row example manifold.
    with open(os.path.join(os.path.dirname(os.path.abspath(__file__)), "..",
                           "src", "main", "resources", "inputs",
                           "Day07Test.txt"), "r", encoding="utf-8") as fh:
        sample = parse_grid(fh.read())
    if split_count_rowscan(sample) != SAMPLE_SPLITS:
        print(f"FAIL: sample rowscan != {SAMPLE_SPLITS}", file=sys.stderr)
        return 1
    if split_count_bfs(sample) != SAMPLE_SPLITS:
        print(f"FAIL: sample bfs != {SAMPLE_SPLITS}", file=sys.stderr)
        return 1
    if timeline_count_manyworlds(sample) != SAMPLE_TIMELINES:
        print(f"FAIL: sample manyworlds != {SAMPLE_TIMELINES}", file=sys.stderr)
        return 1
    if timeline_count_memo(sample) != SAMPLE_TIMELINES:
        print(f"FAIL: sample memo != {SAMPLE_TIMELINES}", file=sys.stderr)
        return 1

    # Accepted Day 07 real-input answers: 1605 beam splits / 29893386035180 timelines.
    with open(real_input_path(), "r", encoding="utf-8") as fh:
        real = parse_grid(fh.read())
    if split_count_rowscan(real) != REAL_SPLITS:
        print(f"FAIL: real rowscan != {REAL_SPLITS}", file=sys.stderr)
        return 1
    if split_count_bfs(real) != REAL_SPLITS:
        print(f"FAIL: real bfs != {REAL_SPLITS}", file=sys.stderr)
        return 1
    if timeline_count_manyworlds(real) != REAL_TIMELINES:
        print(f"FAIL: real manyworlds != {REAL_TIMELINES}", file=sys.stderr)
        return 1
    if timeline_count_memo(real) != REAL_TIMELINES:
        print(f"FAIL: real memo != {REAL_TIMELINES}", file=sys.stderr)
        return 1

    print(f"ok: {cases} random rowscan/bfs + manyworlds/memo cross-checks, "
          f"sample == {SAMPLE_SPLITS}/{SAMPLE_TIMELINES}, "
          f"real == {REAL_SPLITS}/{REAL_TIMELINES}")
    return 0


def file_mode(path, expected, many_worlds=False):
    try:
        with open(path, "r", encoding="utf-8") as fh:
            grid = parse_grid(fh.read())
    except (OSError, ValueError) as exc:
        print(f"error: cannot read {path}: {exc}", file=sys.stderr)
        return 1
    if many_worlds:
        count = timeline_count_manyworlds(grid)
        memo = timeline_count_memo(grid)
        print(f"rows={len(grid)} cols={len(grid[0])} timelines={count}")
        if count != memo:
            print(f"error: manyworlds {count} != memo {memo}", file=sys.stderr)
            return 1
        if expected is not None:
            if count != expected:
                print(f"error: expected {expected}, got {count}", file=sys.stderr)
                return 1
            print(f"ok: timelines == {expected}")
        return 0
    count = split_count_rowscan(grid)
    bfs = split_count_bfs(grid)
    print(f"rows={len(grid)} cols={len(grid[0])} splits={count}")
    if count != bfs:
        print(f"error: rowscan {count} != bfs {bfs}", file=sys.stderr)
        return 1
    if expected is not None:
        if count != expected:
            print(f"error: expected {expected}, got {count}", file=sys.stderr)
            return 1
        print(f"ok: splits == {expected}")
    return 0


def main(argv):
    args = argv[1:]
    many_worlds = "--many-worlds" in args
    args = [a for a in args if a != "--many-worlds"]
    if "--self-test" in args:
        args = [a for a in args if a != "--self-test"]
    if not args:
        return self_test()
    if len(args) not in (1, 2):
        print("usage: scripts/check-beam-splits.py [--many-worlds] "
              "[FILE [EXPECTED]]", file=sys.stderr)
        return 2
    path = args[0]
    expected = int(args[1]) if len(args) >= 2 else None
    return file_mode(path, expected, many_worlds=many_worlds)


if __name__ == "__main__":
    sys.exit(main(sys.argv))
