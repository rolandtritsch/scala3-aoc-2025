#!/usr/bin/env python3
"""check-accessible-rolls.py - validate and apply the "accessible cells in a
grid under a neighbor threshold" technique.

Deterministic, read-only helper distilled from the Day 04 part 1 review
(AOC-116, reviewed commit 78c0f54). The technique covers two related stages:

1. Counting (Day 04 part 1): a cell is *accessible* when fewer than K of its
   eight king-move neighbours contain another occupied cell. Count the
   accessible occupied cells. (Day 04 part 1: K=4, answer 1356 on the real
   grid, 13 on the 10x10 sample.)

2. Cascading removal (Day 04 part 2): once a cell is accessible it is removed
   (becomes empty), which can make further cells accessible, and so on. The
   total number of cells that can ever be removed is a unique fixpoint
   (monotone cascade): it is the same whether all currently-accessible cells
   are removed in a round or cells are removed one at a time in any order.
   (Day 04 part 2 sample: 43.) This mode is a deterministic cross-check the
   solver may use once part 2 is authorized; it is NOT a submitted answer.

Modes:
  no args / --self-test   Seeded random cross-checks (counting via two
                          implementations, peel via round-based vs one-at-a-
                          time removal), then asserts the official Day 04
                          sample (count 13, peel 43) and the accepted Day 04
                          real-input answers (count 1356, peel 8713).
  <FILE> [COUNT] [PEEL]   Read a grid of '@'/'.' (one row per non-blank line),
                          print the accessible count and the peel total, and
                          assert each when a corresponding expected value is
                          given.

Usage:
  scripts/check-accessible-rolls.py
  scripts/check-accessible-rolls.py --self-test
  scripts/check-accessible-rolls.py inputs/Day04.txt 1356
  scripts/check-accessible-rolls.py inputs/Day04Test.txt 13 43

Exit codes:
  0  all checks passed
  1  a check failed or input is malformed
  2  usage error
"""

import os
import random
import sys

SAMPLE = """..@@.@@@@.
@@@.@.@.@@
@@@@@.@.@@
@.@@@@..@.
@@.@@@@.@@
.@@@@@@@.@
.@.@.@.@@@
@.@@@.@@@@
.@@@@@@@@.
@.@.@@@.@."""

THRESHOLD = 4
SAMPLE_COUNT = 13
SAMPLE_PEEL = 43
REAL_COUNT = 1356
REAL_PEEL = 8713


def real_grid_path():
    """Path to the Day 04 real-input grid, resolved relative to this script."""
    here = os.path.dirname(os.path.abspath(__file__))
    return os.path.join(here, "..", "src", "main", "resources", "inputs", "Day04.txt")


def parse_grid(text):
    """Parse a grid of '@'/'.' from text, one row per non-blank line."""
    rows = [line.strip() for line in text.splitlines() if line.strip()]
    if not rows:
        raise ValueError("grid has no non-blank rows")
    if not all(set(r) <= set("@.") for r in rows):
        raise ValueError("grid must contain only '@' and '.'")
    width = len(rows[0])
    if any(len(r) != width for r in rows):
        raise ValueError("grid rows must all have the same width")
    return rows


def count_accessible(rows):
    """Number of occupied cells with fewer than THRESHOLD occupied neighbours
    (8-neighbourhood, no wrapping). Mirrors `Day04.part1` in
    src/main/scala/aoc2025/Day04.scala."""
    h, w = len(rows), len(rows[0])

    def adj(r, c):
        n = 0
        for dr in (-1, 0, 1):
            for dc in (-1, 0, 1):
                if dr == 0 and dc == 0:
                    continue
                nr, nc = r + dr, c + dc
                if 0 <= nr < h and 0 <= nc < w and rows[nr][nc] == "@":
                    n += 1
        return n

    return sum(
        1
        for r in range(h)
        for c in range(w)
        if rows[r][c] == "@" and adj(r, c) < THRESHOLD
    )


def peel_rounds(rows):
    """Total cells removed by repeatedly removing all currently-accessible
    cells until none remain (round-based cascade)."""
    grid = [list(r) for r in rows]
    h, w = len(grid), len(grid[0])

    def adj(r, c):
        n = 0
        for dr in (-1, 0, 1):
            for dc in (-1, 0, 1):
                if dr == 0 and dc == 0:
                    continue
                nr, nc = r + dr, c + dc
                if 0 <= nr < h and 0 <= nc < w and grid[nr][nc] == "@":
                    n += 1
        return n

    total = 0
    while True:
        removable = [
            (r, c)
            for r in range(h)
            for c in range(w)
            if grid[r][c] == "@" and adj(r, c) < THRESHOLD
        ]
        if not removable:
            return total
        total += len(removable)
        for r, c in removable:
            grid[r][c] = "."


def peel_one_at_a_time(rows):
    """Independent reference: remove accessible cells one at a time in an
    arbitrary order, counting every removal. Reaches the same fixpoint total
    as round-based removal because accessibility is monotone."""
    grid = [list(r) for r in rows]
    h, w = len(grid), len(grid[0])

    def adj(r, c):
        n = 0
        for dr in (-1, 0, 1):
            for dc in (-1, 0, 1):
                if dr == 0 and dc == 0:
                    continue
                nr, nc = r + dr, c + dc
                if 0 <= nr < h and 0 <= nc < w and grid[nr][nc] == "@":
                    n += 1
        return n

    total = 0
    changed = True
    while changed:
        changed = False
        for r in range(h):
            for c in range(w):
                if grid[r][c] == "@" and adj(r, c) < THRESHOLD:
                    grid[r][c] = "."
                    total += 1
                    changed = True
    return total


def count_accessible_alt(rows):
    """Independent reference for counting: identical semantics written with a
    different neighbour-iteration structure."""
    h, w = len(rows), len(rows[0])
    offsets = [(dr, dc) for dr in (-1, 0, 1) for dc in (-1, 0, 1) if (dr, dc) != (0, 0)]

    def is_in(r, c):
        return 0 <= r < h and 0 <= c < w

    acc = 0
    for r in range(h):
        for c in range(w):
            if rows[r][c] != "@":
                continue
            n = sum(
                1
                for (dr, dc) in offsets
                if is_in(r + dr, c + dc) and rows[r + dr][c + dc] == "@"
            )
            if n < THRESHOLD:
                acc += 1
    return acc


def self_test():
    """Deterministic validation: seeded random cross-checks + official sample
    + the accepted Day 04 real-input answers (count 1356, peel 8713)."""
    random.seed(20251010)

    # Random small grids: counting via two implementations, peel via two
    # implementations.
    cases = 1000
    for _ in range(cases):
        h = random.randint(1, 8)
        w = random.randint(1, 8)
        rows = ["".join(random.choice("@.") for _ in range(w)) for _ in range(h)]
        a = count_accessible(rows)
        b = count_accessible_alt(rows)
        if a != b:
            print(f"FAIL: count {rows} -> {a} != {b}", file=sys.stderr)
            return 1
        p1 = peel_rounds(rows)
        p2 = peel_one_at_a_time(rows)
        if p1 != p2:
            print(f"FAIL: peel {rows} -> {p1} != {p2}", file=sys.stderr)
            return 1

    # Official Day 04 sample grid.
    sample = parse_grid(SAMPLE)
    if count_accessible(sample) != SAMPLE_COUNT:
        print(f"FAIL: sample count {count_accessible(sample)} != {SAMPLE_COUNT}",
              file=sys.stderr)
        return 1
    if peel_rounds(sample) != SAMPLE_PEEL:
        print(f"FAIL: sample peel {peel_rounds(sample)} != {SAMPLE_PEEL}",
              file=sys.stderr)
        return 1

    # Accepted Day 04 real-input answers (part 1 count 1356, part 2 peel 8713).
    with open(real_grid_path(), "r", encoding="utf-8") as fh:
        real = parse_grid(fh.read())
    if count_accessible(real) != REAL_COUNT:
        print(f"FAIL: real count {count_accessible(real)} != {REAL_COUNT}",
              file=sys.stderr)
        return 1
    if peel_rounds(real) != REAL_PEEL:
        print(f"FAIL: real peel {peel_rounds(real)} != {REAL_PEEL}",
              file=sys.stderr)
        return 1

    print(f"ok: {cases} random count+peel cross-checks, sample count == "
          f"{SAMPLE_COUNT}, sample peel == {SAMPLE_PEEL}, real count == "
          f"{REAL_COUNT}, real peel == {REAL_PEEL}")
    return 0


def file_mode(path, expected_count, expected_peel):
    try:
        with open(path, "r", encoding="utf-8") as fh:
            rows = parse_grid(fh.read())
    except (OSError, ValueError) as exc:
        print(f"error: cannot read {path}: {exc}", file=sys.stderr)
        return 1
    count = count_accessible(rows)
    peel = peel_rounds(rows)
    print(f"rows={len(rows)} cols={len(rows[0])} accessible={count} peel_total={peel}")
    if expected_count is not None and count != expected_count:
        print(f"error: expected count {expected_count}, got {count}", file=sys.stderr)
        return 1
    if expected_peel is not None and peel != expected_peel:
        print(f"error: expected peel {expected_peel}, got {peel}", file=sys.stderr)
        return 1
    if expected_count is not None:
        print(f"ok: accessible == {expected_count}")
    if expected_peel is not None:
        print(f"ok: peel_total == {expected_peel}")
    return 0


def main(argv):
    args = [a for a in argv[1:] if a != "--self-test"]
    if not args:
        return self_test()
    if len(args) not in (1, 2, 3):
        print("usage: scripts/check-accessible-rolls.py [FILE [COUNT [PEEL]]]",
              file=sys.stderr)
        return 2
    path = args[0]
    expected_count = int(args[1]) if len(args) >= 2 else None
    expected_peel = int(args[2]) if len(args) >= 3 else None
    return file_mode(path, expected_count, expected_peel)


if __name__ == "__main__":
    sys.exit(main(sys.argv))