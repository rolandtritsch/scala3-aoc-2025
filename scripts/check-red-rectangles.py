#!/usr/bin/env python3
"""check-red-rectangles.py - validate and apply the largest axis-aligned
rectangle over red-tile diagonal corners technique, both unconstrained
(part 1) and restricted to a red/green loop region (part 2).

Deterministic, read-only helper distilled from the Day 09 reviews. The
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

With `--region` the rectangle must additionally use only red or green tiles:
every red tile is connected to the tile before/after it (the list wraps) by a
straight segment of green tiles, so the red tiles are the vertices of a closed
rectilinear loop and every tile inside it is also green. A candidate rectangle
is only valid when its whole cell span lies inside-or-on the loop (a tile is
usable iff its center is inside-or-on the polygon). Part 2 computes the largest
valid rectangle with an O(n^2) pair scan where each full-rectangle containment
test is O(1) via coordinate-compressed axis blocks plus a 2D prefix sum of
unusable blocks.

Modes:
  no args / --self-test     Seeded random cross-check of the part-1 pair-scan
                            mirror (index-based double loop, mirroring
                            `Day09.part1`, spans via abs + 1) against an
                            independent reference (functional combinations over
                            every pair, spans via min/max), then asserts the
                            official Day 09 sample (8 tiles -> 50) and the
                            accepted Day 09 part 1 real-input answer
                            (496 tiles -> 4777967538).
  <FILE> [EXPECTED]         Read red tile coordinates from FILE (one X,Y pair
                            per line), print the largest rectangle area and the
                            best pair, and assert it when EXPECTED is given.
  --region [no args]        Seeded random cross-check of the part-2 block-
                            compression mirror (mirroring `Day09.part2`) against
                            a direct per-cell containment reference on generated
                            simple rectilinear loops, then asserts the official
                            Day 09 sample (-> 24), the accepted real answer
                            (-> 1439894345), and that the whole-loop usable cell
                            count equals Pick's theorem A + B/2 + 1 (shoelace
                            A=7070349835, boundary B=588088 -> 7070643880).
  --region <FILE> [EXPECTED]  Read tiles from FILE and print the largest valid
                            region rectangle (plus its corner pair); assert
                            EXPECTED when given. The direct reference runs only
                            for small bounding boxes; the whole-loop usable cell
                            count is always checked against Pick's theorem.

Usage:
  scripts/check-red-rectangles.py
  scripts/check-red-rectangles.py --self-test
  scripts/check-red-rectangles.py inputs/Day09Test.txt 50
  scripts/check-red-rectangles.py inputs/Day09.txt 4777967538
  scripts/check-red-rectangles.py --region
  scripts/check-red-rectangles.py --region inputs/Day09Test.txt 24
  scripts/check-red-rectangles.py --region inputs/Day09.txt 1439894345

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
SAMPLE_REGION_ANSWER = 24
REAL_REGION_ANSWER = 1439894345
REAL_REGION_CELLS = 7070643880
REAL_SHOELACE_TWO_A = 14140699670   # 2 * shoelace area A = 7070349835
REAL_BOUNDARY = 588088
REGION_REFERENCE_CELL_BUDGET = 2_000_000


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


def loop_segments(tiles):
    """Closed rectilinear loop through consecutive red tiles.

    Mirrors `Day09.segments`: each red tile connects to the next (wrapping) by
    a straight segment. Consecutive tiles share a row or column, so every
    segment is either vertical (`("v", x, lo, hi)`) or horizontal
    (`("h", y, lo, hi)`); both ranges are inclusive.
    """
    n = len(tiles)
    segs = []
    for i in range(n):
        ax, ay = tiles[i]
        bx, by = tiles[(i + 1) % n]
        if ax == bx:
            segs.append(("v", ax, min(ay, by), max(ay, by)))
        else:
            segs.append(("h", ay, min(ax, bx), max(ax, bx)))
    return segs


def in_region(x, y, segs):
    """Whether the tile center at (x, y) lies inside-or-on the loop.

    Mirrors `Day09.inRegion`: an on-edge tile is usable immediately; otherwise
    the even-odd rule casts a horizontal ray to the right and counts vertical
    segments that strictly straddle the row (`y in [lo, hi)` scores the bottom
    endpoint but not the top, so shared vertices are counted once).
    """
    crossings = 0
    for kind, fixed, lo, hi in segs:
        if kind == "v":
            if x == fixed and lo <= y <= hi:
                return True
            if x < fixed and lo <= y < hi:
                crossings += 1
        elif y == fixed and lo <= x <= hi:
            return True
    return crossings % 2 == 1


def axis_blocks(sorted_values):
    """One block per distinct coordinate and one per interior gap.

    Mirrors `Day09.axisBlocks`: returns (representatives, representative-to-block
    index). The usable state is constant within any block because every segment
    lies along a red-tile row or column.
    """
    reps = []
    index = {}
    block = 0
    for i, value in enumerate(sorted_values):
        reps.append(value)
        index[value] = block
        block += 1
        if i + 1 < len(sorted_values):
            nxt = sorted_values[i + 1]
            if nxt > value + 1:
                reps.append(value + 1)
                block += 1
    return reps, index


def best_region_pair_mirror(tiles):
    """Largest valid region rectangle, block compression + 2D prefix sum.

    Mirrors `Day09.part2`: split both axes into blocks, build a 2D prefix sum
    of unusable blocks, then test every unordered red pair with an O(1)
    full-rectangle containment query. Returns
    (best_area, (x1, y1), (x2, y2)); best_area is 0 when no pair is valid.
    """
    segs = loop_segments(tiles)
    xs = sorted({t[0] for t in tiles})
    ys = sorted({t[1] for t in tiles})
    x_reps, x_index = axis_blocks(xs)
    y_reps, y_index = axis_blocks(ys)
    nx, ny = len(x_reps), len(y_reps)

    prefix = [[0] * (ny + 1) for _ in range(nx + 1)]
    for bx in range(nx):
        for by in range(ny):
            unusable = 0 if in_region(x_reps[bx], y_reps[by], segs) else 1
            prefix[bx + 1][by + 1] = (
                unusable + prefix[bx][by + 1] + prefix[bx + 1][by] - prefix[bx][by]
            )

    def fully_inside(a, b):
        bx1 = x_index[min(a[0], b[0])]
        bx2 = x_index[max(a[0], b[0])]
        by1 = y_index[min(a[1], b[1])]
        by2 = y_index[max(a[1], b[1])]
        unusable = (
            prefix[bx2 + 1][by2 + 1] - prefix[bx1][by2 + 1]
            - prefix[bx2 + 1][by1] + prefix[bx1][by1]
        )
        return unusable == 0

    best = 0
    corners = None
    for i in range(len(tiles)):
        for j in range(i + 1, len(tiles)):
            a, b = tiles[i], tiles[j]
            if fully_inside(a, b):
                area = span(a[0], b[0]) * span(a[1], b[1])
                if area > best:
                    best = area
                    corners = (a, b)
    return best, corners


def best_region_pair_reference(tiles):
    """Largest valid region rectangle, direct per-cell containment scan.

    Independent reference: for every unordered pair, scan every cell of the
    rectangle and accept it only when all cells are usable (inside-or-on via
    `in_region`), so it needs no block compression or prefix sums at all. This
    is O(n^2 * area) and is only practical for small bounding boxes.
    """
    segs = loop_segments(tiles)
    best = 0
    corners = None
    for i in range(len(tiles)):
        for j in range(i + 1, len(tiles)):
            a, b = tiles[i], tiles[j]
            x1, x2 = min(a[0], b[0]), max(a[0], b[0])
            y1, y2 = min(a[1], b[1]), max(a[1], b[1])
            ok = True
            for x in range(x1, x2 + 1):
                for y in range(y1, y2 + 1):
                    if not in_region(x, y, segs):
                        ok = False
                        break
                if not ok:
                    break
            if ok:
                area = (x2 - x1 + 1) * (y2 - y1 + 1)
                if area > best:
                    best = area
                    corners = (a, b)
    return best, corners


def region_cell_count(tiles):
    """Total usable tiles over the whole loop (block-weighted sum).

    Counts every usable block's cell contribution: block width on X times block
    width on Y. This is the "region cell count" that Pick's theorem must match.
    """
    segs = loop_segments(tiles)
    xs = sorted({t[0] for t in tiles})
    ys = sorted({t[1] for t in tiles})
    x_reps, _ = axis_blocks(xs)
    y_reps, _ = axis_blocks(ys)

    def widths(reps):
        out = []
        for i in range(len(reps)):
            out.append(reps[i + 1] - reps[i] if i + 1 < len(reps) else 1)
        return out

    x_widths, y_widths = widths(x_reps), widths(y_reps)
    total = 0
    for bx in range(len(x_reps)):
        for by in range(len(y_reps)):
            if in_region(x_reps[bx], y_reps[by], segs):
                total += x_widths[bx] * y_widths[by]
    return total


def shoelace_and_boundary(tiles):
    """Pick's theorem inputs: integer 2*A (shoelace) and boundary lattice count.

    For a simple rectilinear loop the boundary lattice-point count B equals the
    total Manhattan step length of the closed path, and the usable cell count
    (tile centers inside-or-on) equals A + B/2 + 1.
    """
    two_a = 0
    boundary = 0
    n = len(tiles)
    for i in range(n):
        x1, y1 = tiles[i]
        x2, y2 = tiles[(i + 1) % n]
        two_a += x1 * y2 - x2 * y1
        boundary += abs(x2 - x1) + abs(y2 - y1)
    return two_a, boundary


def pick_region_cell_count(tiles):
    """Usable cell count from Pick's theorem: A + B/2 + 1 (integer arithmetic)."""
    two_a, boundary = shoelace_and_boundary(tiles)
    return two_a // 2 + boundary // 2 + 1


def random_region_loop(rng):
    """Generate a simple rectilinear loop with distinct vertices.

    Two monotone staircases (a strictly rising bottom chain and a strictly
    falling top chain, kept strictly above it) joined on the left and right
    sides. Consecutive vertices always share a row or column, all vertices are
    distinct, and the loop has positive area, matching the problem's loop shape.
    """
    k = rng.randint(1, 4)
    xs = [0]
    for _ in range(k):
        xs.append(xs[-1] + rng.randint(1, 5))
    sb = rng.randint(1, 3)
    st = rng.randint(1, 3)
    base = rng.randint(0, 4)
    top = base + k * (sb + st) + rng.randint(2, 8)
    bottom = [base + i * sb for i in range(k + 1)]
    upper = [top - i * st for i in range(k + 1)]
    verts = [(xs[0], bottom[0])]
    for i in range(k):
        verts.append((xs[i], bottom[i + 1]))
        verts.append((xs[i + 1], bottom[i + 1]))
    verts.append((xs[k], upper[k]))
    for i in range(k, 0, -1):
        verts.append((xs[i], upper[i - 1]))
        verts.append((xs[i - 1], upper[i - 1]))
    return verts


def region_self_test():
    """Deterministic part-2 validation: random mirror/reference cross-checks +
    official sample (24) + accepted real answer (1439894345) + Pick's theorem
    whole-loop cell count (7070643880)."""
    rng = random.Random(20261011)
    cases = 3000
    for _ in range(cases):
        loop = random_region_loop(rng)
        (a, _), (b, _) = best_region_pair_mirror(loop), best_region_pair_reference(loop)
        if a != b:
            print(f"FAIL: loop {loop} -> mirror {a} != reference {b}", file=sys.stderr)
            return 1
        if region_cell_count(loop) != pick_region_cell_count(loop):
            print(f"FAIL: loop {loop} region cells != Pick", file=sys.stderr)
            return 1

    with open(sample_input_path(), "r", encoding="utf-8") as fh:
        sample = parse_tiles(fh.read())
    (a, corners_a), (b, corners_b) = (
        best_region_pair_mirror(sample), best_region_pair_reference(sample))
    if a != SAMPLE_REGION_ANSWER or b != SAMPLE_REGION_ANSWER:
        print(f"FAIL: sample region mirror {a} / reference {b} != "
              f"{SAMPLE_REGION_ANSWER}", file=sys.stderr)
        return 1
    if corners_a != corners_b:
        print(f"FAIL: sample region corners {corners_a} != reference corners "
              f"{corners_b}", file=sys.stderr)
        return 1
    if region_cell_count(sample) != pick_region_cell_count(sample):
        print("FAIL: sample region cells != Pick", file=sys.stderr)
        return 1

    with open(real_input_path(), "r", encoding="utf-8") as fh:
        real = parse_tiles(fh.read())
    (a, _) = best_region_pair_mirror(real)
    if a != REAL_REGION_ANSWER:
        print(f"FAIL: real region mirror {a} != {REAL_REGION_ANSWER}", file=sys.stderr)
        return 1
    two_a, boundary = shoelace_and_boundary(real)
    cells = region_cell_count(real)
    if two_a != REAL_SHOELACE_TWO_A or boundary != REAL_BOUNDARY:
        print(f"FAIL: real geometry 2A={two_a} B={boundary} != expected "
              f"{REAL_SHOELACE_TWO_A}/{REAL_BOUNDARY}", file=sys.stderr)
        return 1
    if cells != REAL_REGION_CELLS or cells != pick_region_cell_count(real):
        print(f"FAIL: real region cells {cells} != {REAL_REGION_CELLS} or != Pick",
              file=sys.stderr)
        return 1

    print(f"ok: {cases} random mirror/reference + Pick cross-checks, sample == "
          f"{SAMPLE_REGION_ANSWER}, real == {REAL_REGION_ANSWER}, region cells == "
          f"{REAL_REGION_CELLS} (Pick's theorem)")
    return 0


def region_file_mode(path, expected):
    path = repo_input_path(path)
    try:
        with open(path, "r", encoding="utf-8") as fh:
            tiles = parse_tiles(fh.read())
    except (OSError, ValueError) as exc:
        print(f"error: cannot read {path}: {exc}", file=sys.stderr)
        return 1
    if len(tiles) < 4:
        print(f"error: region mode needs at least 4 red tiles, got {len(tiles)}",
              file=sys.stderr)
        return 1
    area, corners = best_region_pair_mirror(tiles)
    print(f"tiles={len(tiles)} largest_region_area={area}")
    if corners is not None:
        print(f"corners=({corners[0][0]},{corners[0][1]})-({corners[1][0]},{corners[1][1]})")
    cells = region_cell_count(tiles)
    pick = pick_region_cell_count(tiles)
    if cells != pick:
        print(f"error: region cells {cells} != Pick {pick}", file=sys.stderr)
        return 1
    print(f"region_cells={cells} (Pick's theorem A + B/2 + 1)")

    xs = [t[0] for t in tiles]
    ys = [t[1] for t in tiles]
    bbox_cells = (max(xs) - min(xs) + 1) * (max(ys) - min(ys) + 1)
    if bbox_cells <= REGION_REFERENCE_CELL_BUDGET:
        reference, _ = best_region_pair_reference(tiles)
        if area != reference:
            print(f"error: mirror {area} != reference {reference}", file=sys.stderr)
            return 1
    else:
        print(f"note: skipping O(n^2*area) reference for {bbox_cells}-cell bounding box")
    if expected is not None:
        if area != expected:
            print(f"error: expected {expected}, got {area}", file=sys.stderr)
            return 1
        print(f"ok: largest_region_area == {expected}")
    return 0


def main(argv):
    args = argv[1:]
    region = "--region" in args
    args = [a for a in args if a != "--region"]
    want_self_test = "--self-test" in args
    args = [a for a in args if a != "--self-test"]
    if want_self_test:
        return region_self_test() if region else self_test()
    if not args:
        return region_self_test() if region else self_test()
    if len(args) not in (1, 2):
        print("usage: scripts/check-red-rectangles.py [--region] [FILE [EXPECTED]]",
              file=sys.stderr)
        return 2
    expected = int(args[1]) if len(args) >= 2 else None
    if region:
        return region_file_mode(args[0], expected)
    return file_mode(args[0], expected)


if __name__ == "__main__":
    sys.exit(main(sys.argv))