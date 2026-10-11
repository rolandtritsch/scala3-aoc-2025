#!/usr/bin/env python3
"""check-red-rectangles.py - validate and apply the largest axis-aligned
rectangle over red-tile diagonal corners technique.

Deterministic, read-only helper distilled from the Day 09 part 1 review. The
technique (Day 09 "Movie Theater"): given red tile coordinates (one `X,Y` pair
per line), the Elves want the largest axis-aligned rectangle that uses two red
tiles as opposite (diagonal) corners. The rectangle's edges run along grid
lines, so its width is the inclusive span of the two X coordinates, its height
is the inclusive span of the two Y coordinates, and the area is
`(|dx| + 1) * (|dy| + 1)`. The largest area is the maximum over every unordered
pair of red tiles (O(n^2)).

The formula is inclusive of both corner tiles: the official sample confirms
2,5..11,1 -> width 10 * height 5 = 50, and 2,5..9,7 -> width 8 * height 3 = 24
("You could even make a thin rectangle" between 7,3 and 2,3 has area 6, i.e. a
width of 6 and a height of 1).

Modes:
  no args / --self-test     Seeded random cross-check of the pair-scan mirror
                            (index-based double loop, mirroring `Day09.part1`,
                            spans via abs + 1) against an independent reference
                            (functional combinations over every pair, spans via
                            min/max), then asserts the official Day 09 sample
                            (8 tiles -> 50) and the accepted Day 09 part 1
                            real-input answer (496 tiles -> 4777967538).
  <FILE> [EXPECTED]         Read red tile coordinates from FILE (one X,Y pair
                            per line), print the largest rectangle area and the
                            best pair, and assert it when EXPECTED is given.

Usage:
  scripts/check-red-rectangles.py
  scripts/check-red-rectangles.py --self-test
  scripts/check-red-rectangles.py inputs/Day09Test.txt 50
  scripts/check-red-rectangles.py inputs/Day09.txt 4777967538

Exit codes:
  0  all checks passed
  1  a check failed or input is malformed
  2  usage error
"""

import itertools
import os
import random
import sys

SAMPLE_ANSWER = 50
REAL_ANSWER = 4777967538


def real_input_path():
    """Path to the Day 09 real input, resolved relative to this script."""
    here = os.path.dirname(os.path.abspath(__file__))
    return os.path.join(here, "..", "src", "main", "resources", "inputs", "Day09.txt")


def sample_input_path():
    """Path to the Day 09 official 8-tile sample."""
    here = os.path.dirname(os.path.abspath(__file__))
    return os.path.join(here, "..", "src", "main", "resources", "inputs", "Day09Test.txt")


def repo_input_path(path):
    """Resolve a file-mode input path. Literal/existing paths are used as-is;
    an `inputs/...`-style path is resolved against the canonical
    `src/main/resources/inputs` directory so the documented invocations work
    from anywhere in the repo."""
    here = os.path.dirname(os.path.abspath(__file__))
    repo_root = os.path.dirname(here)
    if os.path.isabs(path) or os.path.exists(path):
        return path
    if path.startswith("inputs/"):
        candidate = os.path.join(repo_root, "src", "main", "resources", path)
        if os.path.exists(candidate):
            return candidate
    return path


def parse_tiles(text):
    """Parse red tile coordinates from `X,Y` pairs, one per line.

    Mirrors `Day09.readFile` in src/main/scala/aoc2025/Day09.scala: every
    non-blank line must be exactly two integer coordinates.
    """
    tiles = []
    for lineno, line in enumerate(text.splitlines(), start=1):
        stripped = line.strip()
        if not stripped:
            continue
        parts = stripped.split(",")
        if len(parts) != 2:
            raise ValueError(f"line {lineno}: expected an X,Y pair: {line!r}")
        try:
            tiles.append((int(parts[0]), int(parts[1])))
        except ValueError as exc:
            raise ValueError(f"line {lineno}: non-integer coordinate in {line!r}") from exc
    if not tiles:
        raise ValueError("no red tiles parsed")
    return tiles


def span(a, b):
    """Inclusive span between two 1-D coordinates: `abs(a - b) + 1`."""
    return abs(a - b) + 1


def best_pair_mirror(tiles):
    """Largest area over every unordered pair, index-based double loop.

    Mirrors `Day09.part1`: for each i < j compute the inclusive-span area and
    keep the maximum. Returns (best_area, (x1, y1), (x2, y2)); best_area is 0
    with no pair for fewer than two tiles.
    """
    best = 0
    corners = None
    for i in range(len(tiles)):
        for j in range(i + 1, len(tiles)):
            a = tiles[i]
            b = tiles[j]
            area = span(a[0], b[0]) * span(a[1], b[1])
            if area > best:
                best = area
                corners = (a, b)
    return best, corners


def best_pair_reference(tiles):
    """Largest area over every unordered pair, functional formulation.

    Independent reference: the span is computed as `max - min + 1` instead of
    `abs(...) + 1` (structurally different, catches a sign/order mistake), and
    pairs are enumerated with `itertools.combinations` rather than index loops.
    Returns the same (best_area, (x1, y1), (x2, y2)) shape.
    """
    best = 0
    corners = None
    for a, b in itertools.combinations(tiles, 2):
        width = max(a[0], b[0]) - min(a[0], b[0]) + 1
        height = max(a[1], b[1]) - min(a[1], b[1]) + 1
        area = width * height
        if area > best:
            best = area
            corners = (a, b)
    return best, corners


def self_test():
    """Deterministic validation: seeded random cross-checks + official sample
    + the accepted Day 09 real-input answer (4777967538)."""
    random.seed(20261011)

    cases = 5000
    for _ in range(cases):
        n = random.randint(0, 14)
        tiles = [(random.randint(0, 100), random.randint(0, 100)) for _ in range(n)]
        (a, _), (b, _) = best_pair_mirror(tiles), best_pair_reference(tiles)
        if a != b:
            print(f"FAIL: n={n} {tiles} -> mirror {a} != reference {b}",
                  file=sys.stderr)
            return 1

    with open(sample_input_path(), "r", encoding="utf-8") as fh:
        sample = parse_tiles(fh.read())
    (a, corners_a), (b, corners_b) = (
        best_pair_mirror(sample), best_pair_reference(sample))
    if a != SAMPLE_ANSWER or b != SAMPLE_ANSWER:
        print(f"FAIL: sample mirror {a} / reference {b} != {SAMPLE_ANSWER}",
              file=sys.stderr)
        return 1
    if corners_a != corners_b:
        print(f"FAIL: sample mirror corners {corners_a} != reference corners "
              f"{corners_b}", file=sys.stderr)
        return 1

    with open(real_input_path(), "r", encoding="utf-8") as fh:
        real = parse_tiles(fh.read())
    (a, corners_a), (b, corners_b) = (
        best_pair_mirror(real), best_pair_reference(real))
    if a != REAL_ANSWER or b != REAL_ANSWER:
        print(f"FAIL: real mirror {a} / reference {b} != {REAL_ANSWER}",
              file=sys.stderr)
        return 1
    if corners_a != corners_b:
        print(f"FAIL: real mirror corners {corners_a} != reference corners "
              f"{corners_b}", file=sys.stderr)
        return 1

    print(f"ok: {cases} random mirror/reference cross-checks, sample == "
          f"{SAMPLE_ANSWER}, real == {REAL_ANSWER}")
    return 0


def file_mode(path, expected):
    path = repo_input_path(path)
    try:
        with open(path, "r", encoding="utf-8") as fh:
            tiles = parse_tiles(fh.read())
    except (OSError, ValueError) as exc:
        print(f"error: cannot read {path}: {exc}", file=sys.stderr)
        return 1
    area, corners = best_pair_mirror(tiles)
    print(f"tiles={len(tiles)} largest_area={area}")
    if corners is not None:
        print(f"corners=({corners[0][0]},{corners[0][1]})-({corners[1][0]},{corners[1][1]})")
    reference, _ = best_pair_reference(tiles)
    if area != reference:
        print(f"error: mirror {area} != reference {reference}", file=sys.stderr)
        return 1
    if expected is not None:
        if area != expected:
            print(f"error: expected {expected}, got {area}", file=sys.stderr)
            return 1
        print(f"ok: largest_area == {expected}")
    return 0


def main(argv):
    args = argv[1:]
    if not args or args == ["--self-test"]:
        return self_test()
    if len(args) not in (1, 2):
        print("usage: scripts/check-red-rectangles.py [FILE [EXPECTED]]",
              file=sys.stderr)
        return 2
    expected = int(args[1]) if len(args) >= 2 else None
    return file_mode(args[0], expected)


if __name__ == "__main__":
    sys.exit(main(sys.argv))