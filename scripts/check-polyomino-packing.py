#!/usr/bin/env python3
"""check-polyomino-packing.py - validate and apply the polyomino-packing
technique (fit a multiset of small present shapes into a rectangular region).

Deterministic, read-only helper distilled from the Day 12 part 1 review. The
technique (Day 12 "Christmas Tree Farm"): the input lists a handful of small
present shapes (drawn as `#`/`.` diagrams) and, for every tree, the width and
length of the region beneath it plus how many presents of each shape must fit
inside. A present may be rotated and reflected but must sit exactly on the unit
grid, the occupied (`#`) cells of two different presents may not overlap, and
the unoccupied (`.`) cells of a diagram do not block another present. The
answer is how many regions can contain every listed present at once.

A region can only fit when the total number of occupied cells of all its
presents does not exceed the region's area. Because the Day 12 shapes are tiny
this area bound is in practice the deciding test on the real input (the
accepted answer 408 equals the area-feasibility count exactly), but a complete
geometric search is the source of truth: the official 12x5 `1 0 1 0 3 2`
region is area-feasible (49 cells in a 60-cell region) yet geometrically
impossible.

Modes:
  no args / --self-test     Seeded random cross-checks of the backtracking
                            mirror (mirroring `Day12.part1`) against an
                            independent exact-cover reference, then asserts the
                            official Day 12 sample (2, including the
                            area-feasible-but-impossible third region) and the
                            accepted Day 12 part 1 real-input answer (408,
                            plus its independent area-feasibility count).
  <FILE> [EXPECTED]         Read a Day 12 input file (shape diagrams plus
                            `WxH: q0 q1 ...` region lines), print how many
                            regions fit all their presents (and the independent
                            area-feasibility count), and assert the fit count
                            when EXPECTED is given.

Exit codes:
  0  all checks passed
  1  a check failed or input is malformed
  2  usage error
"""

import os
import random
import re
import sys

SAMPLE_ANSWER = 2
REAL_ANSWER = 408
SHAPE_INDEX_RE = re.compile(r"^\d+:$")
REGION_RE = re.compile(r"^(\d+)x(\d+):\s*(.*)$")

# The mirror's recursion is bounded by the region area (each level either places
# a present or leaves one cell empty), so a 10000 limit is safely generous.
sys.setrecursionlimit(10000)


def real_input_path():
    """Path to the Day 12 real input, resolved relative to this script."""
    here = os.path.dirname(os.path.abspath(__file__))
    return os.path.join(here, "..", "src", "main", "resources", "inputs",
                        "Day12.txt")


def sample_input_path():
    """Path to the Day 12 official sample."""
    here = os.path.dirname(os.path.abspath(__file__))
    return os.path.join(here, "..", "src", "main", "resources", "inputs",
                        "Day12Test.txt")


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


def parse_input(text):
    """Parse a Day 12 input into (shapes, regions).

    Mirrors `Day12.readFile` in src/main/scala/aoc2025/Day12.scala: each line
    matching `N:` starts a shape whose diagram is the next three lines (its
    `#` cells become a set of (x, y) offsets from the top-left corner), and
    each line matching `WxH: q0 q1 ...` is a region with per-shape quantities.
    Returns shapes as a list of frozensets of (x, y) offsets and regions as a
    list of (width, height, quantities-tuple)."""
    shapes = []
    regions = []
    lines = [line.strip() for line in text.splitlines()]
    i = 0
    while i < len(lines):
        line = lines[i]
        if SHAPE_INDEX_RE.match(line):
            diagram = lines[i + 1:i + 4]
            if len(diagram) != 3:
                raise ValueError(
                    f"shape {line!r}: expected a 3-row diagram, got {len(diagram)} rows")
            cells = frozenset(
                (x, y)
                for y, row in enumerate(diagram)
                for x, ch in enumerate(row)
                if ch == '#'
            )
            if not cells:
                raise ValueError(f"shape {line!r}: diagram has no '#' cells")
            shapes.append(cells)
            i += 4
            continue
        m = REGION_RE.match(line)
        if m:
            width = int(m.group(1))
            height = int(m.group(2))
            if width <= 0 or height <= 0:
                raise ValueError(f"region {line!r}: non-positive dimensions")
            quantities = tuple(int(t) for t in m.group(3).split())
            if not quantities:
                raise ValueError(f"region {line!r}: no quantities")
            regions.append((width, height, quantities))
        i += 1
    if not shapes:
        raise ValueError("no shape diagrams parsed")
    if not regions:
        raise ValueError("no region lines parsed")
    return shapes, regions


def area_count(shapes, regions):
    """Independent trivial bound: count regions whose total present cells fit
    within the region area. Geometrically a necessary condition; on the Day 12
    real input it alone equals the accepted answer (408)."""
    counts = [len(shape) for shape in shapes]
    fits = 0
    for width, height, quantities in regions:
        if sum(c * q for c, q in zip(counts, quantities)) <= width * height:
            fits += 1
    return fits


def reflect_x(cells):
    return frozenset((x, -y) for (x, y) in cells)


def rotate90(cells):
    return frozenset((y, -x) for (x, y) in cells)


def normalize(cells):
    min_x = min(x for x, _ in cells)
    min_y = min(y for _, y in cells)
    return frozenset((x - min_x, y - min_y) for (x, y) in cells)


def orientations(shape):
    """Every distinct orientation of a shape under the eight dihedral
    symmetries of the unit square, deduplicated and normalized back to the
    top-left corner (mirroring `Day12.orientations`). Sorted for reproducible
    iteration order."""
    seen = set()
    cells = shape
    for _ in range(4):
        seen.add(normalize(cells))
        seen.add(normalize(reflect_x(cells)))
        cells = rotate90(cells)
    key = lambda s: (len(s), tuple(sorted(s)))
    return sorted(seen, key=key)


def fits_mirror(region, shapes):
    """Backtracking placement search mirroring `Day12.part1`.

    Presents are placed one at a time; the next present is anchored at the
    first still-empty grid cell, which either hosts the top-left corner of some
    remaining present's placement or is left empty. Every distinct orientation
    of each shape is generated up front and translated across the region. The
    search is complete, so a negative answer is only returned once every
    reachable placement has been tried. Occupied cells and placements are
    Python integers (bitmasks) so overlap checks and updates are single bigint
    operations."""
    width, height, quantities = region
    area = width * height
    total_cells = sum(len(shapes[i]) * quantities[i] for i in range(len(shapes)))
    if total_cells > area:
        return False

    anchors = {}
    for si, q in enumerate(quantities):
        if q == 0:
            continue
        for ori in orientations(shapes[si]):
            max_x = max(x for x, _ in ori)
            max_y = max(y for _, y in ori)
            for dy in range(height - max_y):
                for dx in range(width - max_x):
                    mask = 0
                    for (x, y) in ori:
                        mask |= 1 << ((y + dy) * width + (x + dx))
                    anchor = (mask & -mask).bit_length() - 1
                    anchors.setdefault(anchor, []).append((si, mask))

    remaining = list(quantities)
    empty_left = area - total_cells
    presents_left = sum(quantities)
    full = (1 << area) - 1

    def first_free(occupied, from_cell):
        bits = (~occupied) & (full ^ ((1 << from_cell) - 1))
        if bits == 0:
            return -1
        return (bits & -bits).bit_length() - 1

    def search(occupied, from_cell, empty_left, presents_left):
        if presents_left == 0:
            return True
        cell = first_free(occupied, from_cell)
        if cell < 0:
            return False
        for si, mask in anchors.get(cell, ()):
            if remaining[si] > 0 and not (occupied & mask):
                remaining[si] -= 1
                if search(occupied | mask, cell + 1, empty_left,
                          presents_left - 1):
                    remaining[si] += 1
                    return True
                remaining[si] += 1
        if empty_left > 0 and search(occupied, cell + 1, empty_left - 1,
                                     presents_left):
            return True
        return False

    return search(0, 0, empty_left, presents_left)


def fits_dlx(region, shapes):
    """Independent exact-cover (Algorithm X) reference.

    Each copy of each required present must be placed exactly once, and each
    grid cell must be covered at most once. Exact cover normally requires every
    column exactly once, so the "leave a cell empty" freedom is modelled by
    giving every cell its own singleton skip row: a cell is either covered by
    exactly one present or by its skip row, never both. Columns are one per
    grid cell plus one per present copy; a placement row covers the cells it
    occupies and its copy's column. The recursion always branches on the
    uncovered column with the fewest still-usable rows (Knuth's S heuristic).
    Structurally different from the anchored backtracking mirror, so agreement
    catches anchoring/orientation/quantity bugs. Only used on small regions and
    tiny fixtures (the 12x5 sample regions and the real input are far too
    large)."""
    width, height, quantities = region
    area = width * height
    total_cells = sum(len(shapes[i]) * quantities[i] for i in range(len(shapes)))
    if total_cells > area:
        return False

    copies = []
    for si, q in enumerate(quantities):
        copies.extend([si] * q)
    n_cols = area + len(copies)

    rows = []
    for ci, si in enumerate(copies):
        copy_bit = 1 << (area + ci)
        for ori in orientations(shapes[si]):
            max_x = max(x for x, _ in ori)
            max_y = max(y for _, y in ori)
            for dy in range(height - max_y):
                for dx in range(width - max_x):
                    cells = 0
                    for (x, y) in ori:
                        cells |= 1 << ((y + dy) * width + (x + dx))
                    rows.append(cells | copy_bit)
    for cell in range(area):
        rows.append(1 << cell)

    col_rows = [[] for _ in range(n_cols)]
    for r, mask in enumerate(rows):
        m = mask
        while m:
            lsb = m & -m
            col_rows[lsb.bit_length() - 1].append(r)
            m -= lsb

    full = (1 << n_cols) - 1

    def search(covered):
        if covered == full:
            return True
        best_col = -1
        best_ops = None
        for c in range(n_cols):
            if covered >> c & 1:
                continue
            ops = [r for r in col_rows[c] if not rows[r] & covered]
            if best_col == -1 or len(ops) < len(best_ops):
                best_col = c
                best_ops = ops
                if not best_ops:
                    return False
        for r in best_ops:
            if search(covered | rows[r]):
                return True
        return False

    return search(0)


def count_regions(shapes, regions):
    return sum(1 for region in regions if fits_mirror(region, shapes))


def random_shape(rng):
    """A random small shape: 1..5 distinct cells of a 3x3 box, normalized."""
    cells = rng.sample([(x, y) for x in range(3) for y in range(3)],
                       rng.randint(1, 5))
    return normalize(frozenset(cells))


def random_case(rng):
    """A random small region + shape set. Sizes are kept tiny so the exact-cover
    reference stays fast; total cells often lands near the area so both the
    area-reject fast path and the geometric search are exercised."""
    width = rng.randint(1, 4)
    height = rng.randint(1, 4)
    n_shapes = rng.randint(1, 4)
    shapes = [random_shape(rng) for _ in range(n_shapes)]
    quantities = tuple(rng.randint(0, 2) for _ in range(n_shapes))
    return (width, height, quantities), shapes


def self_test():
    """Deterministic validation: seeded random cross-checks of the backtracking
    mirror against the exact-cover reference, the official Day 12 sample (2,
    including the area-feasible-but-impossible third region), the boundary
    fixtures, and the accepted Day 12 real-input answer (408 plus its
    independent area-feasibility count)."""
    rng = random.Random(20261011)

    cases = 4000
    for _ in range(cases):
        region, shapes = random_case(rng)
        a = fits_mirror(region, shapes)
        b = fits_dlx(region, shapes)
        if a != b:
            print(f"FAIL: region={region} shapes={shapes} "
                  f"-> mirror {a} != dlx {b}", file=sys.stderr)
            return 1

    with open(sample_input_path(), "r", encoding="utf-8") as fh:
        sample_shapes, sample_regions = parse_input(fh.read())
    assert len(sample_shapes) == 6, f"sample should have 6 shapes, got {len(sample_shapes)}"
    assert all(len(s) == 7 for s in sample_shapes), \
        "sample shapes should each have 7 cells"
    assert len(sample_regions) == 3, \
        f"sample should have 3 regions, got {len(sample_regions)}"
    sample_mirror = count_regions(sample_shapes, sample_regions)
    if sample_mirror != SAMPLE_ANSWER:
        print(f"FAIL: sample mirror {sample_mirror} != {SAMPLE_ANSWER}",
              file=sys.stderr)
        return 1
    if not fits_mirror(sample_regions[0], sample_shapes) or \
            not fits_dlx(sample_regions[0], sample_shapes):
        print("FAIL: sample region 0 should fit (mirror and dlx agree)",
              file=sys.stderr)
        return 1
    if not fits_mirror(sample_regions[1], sample_shapes):
        print("FAIL: sample region 1 should fit", file=sys.stderr)
        return 1

    width, height, quantities = sample_regions[2]
    total_cells = sum(len(sample_shapes[i]) * quantities[i]
                      for i in range(len(sample_shapes)))
    if total_cells > width * height:
        print("FAIL: expected sample region 3 to be area-feasible "
              "but geometrically impossible", file=sys.stderr)
        return 1
    if fits_mirror(sample_regions[2], sample_shapes):
        print("FAIL: sample region 3 should be geometrically impossible "
              "despite enough area", file=sys.stderr)
        return 1

    domino = frozenset([(0, 0), (1, 0)])
    triomino = frozenset([(0, 0), (1, 0), (2, 0)])
    fixtures = [
        (([frozenset([(0, 0)])], [(5, 5, (0,))]), 1, "zero-present region fits"),
        (([frozenset([(0, 0)])], [(2, 3, (7,))]), 0,
         "7 cells cannot fit a 6-cell region"),
        (([triomino], [(2, 2, (1,))]), 0,
         "1x3 triomino cannot fit 2x2 despite enough area"),
        (([domino], [(2, 2, (2,))]), 1, "two dominoes exactly tile 2x2"),
        (([domino], [(2, 2, (2,)), (1, 2, (2,))]), 1,
         "mixed fitting/non-fitting regions counted separately"),
    ]
    for (shapes, regions), expected, label in fixtures:
        if count_regions(shapes, regions) != expected:
            print(f"FAIL: {label} (mirror)", file=sys.stderr)
            return 1
        if sum(1 for r in regions if fits_dlx(r, shapes)) != expected:
            print(f"FAIL: {label} (dlx)", file=sys.stderr)
            return 1

    with open(real_input_path(), "r", encoding="utf-8") as fh:
        real_shapes, real_regions = parse_input(fh.read())
    assert len(real_shapes) == 6, \
        f"real input should have 6 shapes, got {len(real_shapes)}"
    assert len(real_regions) == 1000, \
        f"real input should have 1000 regions, got {len(real_regions)}"
    real_mirror = count_regions(real_shapes, real_regions)
    if real_mirror != REAL_ANSWER:
        print(f"FAIL: real mirror {real_mirror} != {REAL_ANSWER}",
              file=sys.stderr)
        return 1
    real_area = area_count(real_shapes, real_regions)
    if real_area != REAL_ANSWER:
        print(f"FAIL: real area-feasibility count {real_area} != {REAL_ANSWER}",
              file=sys.stderr)
        return 1

    print(f"ok: {cases} mirror/dlx cross-checks, sample == {SAMPLE_ANSWER}, "
          f"real == {REAL_ANSWER} (area count {real_area})")
    return 0


def file_mode(path, expected):
    path = repo_input_path(path)
    try:
        with open(path, "r", encoding="utf-8") as fh:
            shapes, regions = parse_input(fh.read())
    except (OSError, ValueError) as exc:
        print(f"error: cannot read {path}: {exc}", file=sys.stderr)
        return 1
    total = count_regions(shapes, regions)
    area = area_count(shapes, regions)
    print(f"shapes={len(shapes)} regions={len(regions)} fits={total} "
          f"area={area}")
    if expected is not None:
        if total != expected:
            print(f"error: expected {expected}, got {total}", file=sys.stderr)
            return 1
        print(f"ok: fits == {expected}")
    return 0


def main(argv):
    args = argv[1:]
    want_self_test = "--self-test" in args
    args = [a for a in args if a != "--self-test"]
    if want_self_test or not args:
        return self_test()
    if len(args) not in (1, 2):
        print("usage: scripts/check-polyomino-packing.py [FILE [EXPECTED]]",
              file=sys.stderr)
        return 2
    expected = int(args[1]) if len(args) >= 2 else None
    return file_mode(args[0], expected)


if __name__ == "__main__":
    sys.exit(main(sys.argv))