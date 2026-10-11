#!/usr/bin/env python3
"""check-closest-pairs.py - validate and apply the closest-pairs single-linkage
clustering technique.

Deterministic, read-only helper distilled from the Day 08 part 1 review. The
technique (Day 08 "Playground"): given n junction boxes in 3D space (one
`X,Y,Z` triple per line), repeatedly connect the closest pair of boxes by
straight-line distance that are not yet directly connected; a pair whose boxes
are already in the same circuit does nothing. After `k` such connections, the
answer is the product of the sizes of the three largest circuits (minimally
growing single-linkage clustering; effectively Kruskal's algorithm over the
sorted all-pairs edge list, stopping after `k` edges).

Modes:
  no args / --self-test     Seeded random cross-check of the mirror (array-based
                            union-find with path compression, mirroring
                            `Day08.part1`) against an independent reference
                            (small-to-large set merging with relabeling), then
                            asserts the official Day 08 sample (10 connections ->
                            40) and the accepted Day 08 part 1 real-input answer
                            (1000 connections -> 90036).
  <FILE> <K> [EXPECTED]     Read junction boxes from FILE (one X,Y,Z triple per
                            line), process the K closest pairs, print the product
                            of the three largest circuit sizes, and assert it
                            when EXPECTED is given.

Usage:
  scripts/check-closest-pairs.py
  scripts/check-closest-pairs.py --self-test
  scripts/check-closest-pairs.py inputs/Day08Test.txt 10 40
  scripts/check-closest-pairs.py inputs/Day08.txt 1000 90036

Exit codes:
  0  all checks passed
  1  a check failed or input is malformed
  2  usage error
"""

import os
import random
import sys

SAMPLE_CONNECTIONS = 10
SAMPLE_ANSWER = 40
REAL_CONNECTIONS = 1000
REAL_ANSWER = 90036


def real_input_path():
    """Path to the Day 08 real input, resolved relative to this script."""
    here = os.path.dirname(os.path.abspath(__file__))
    return os.path.join(here, "..", "src", "main", "resources", "inputs", "Day08.txt")


def sample_input_path():
    """Path to the Day 08 official 20-box sample."""
    here = os.path.dirname(os.path.abspath(__file__))
    return os.path.join(here, "..", "src", "main", "resources", "inputs", "Day08Test.txt")


def parse_points(text):
    """Parse junction box positions from `X,Y,Z` triples, one per line.

    Mirrors `Day08.readFile` in src/main/scala/aoc2025/Day08.scala: every
    non-blank line must be exactly three integer coordinates.
    """
    points = []
    for lineno, line in enumerate(text.splitlines(), start=1):
        stripped = line.strip()
        if not stripped:
            continue
        parts = stripped.split(",")
        if len(parts) != 3:
            raise ValueError(f"line {lineno}: expected an X,Y,Z triple: {line!r}")
        try:
            points.append(tuple(int(part) for part in parts))
        except ValueError as exc:
            raise ValueError(f"line {lineno}: non-integer coordinate in {line!r}") from exc
    if not points:
        raise ValueError("no junction boxes parsed")
    return points


def squared_distance(a, b):
    """Squared Euclidean distance between two 3D points.

    Squared distance preserves straight-line ordering while avoiding
    floating-point error, so it is sufficient for selecting closest pairs.
    Mirrors `Day08.squaredDistance`.
    """
    return (a[0] - b[0]) ** 2 + (a[1] - b[1]) ** 2 + (a[2] - b[2]) ** 2


def sorted_pairs(points):
    """All unordered pairs ordered by squared distance, then (i, j).

    This mirrors `Day08.part1`: the pair list is generated in (i, j) index order
    and `sortBy(_._1)` is a stable sort, so equal-distance pairs keep index
    order. The reference implementation below consumes the same list so the two
    methods agree by construction on tie-breaking.
    """
    n = len(points)
    pairs = []
    for i in range(n):
        for j in range(i + 1, n):
            pairs.append((squared_distance(points[i], points[j]), i, j))
    pairs.sort()
    return pairs


def part1_mirror(points, k):
    """Array-based union-find with path compression, mirroring `Day08.part1`."""
    n = len(points)
    parent = list(range(n))
    size = [1] * n

    def find(x):
        root = x
        while parent[root] != root:
            root = parent[root]
        while parent[x] != root:
            nxt = parent[x]
            parent[x] = root
            x = nxt
        return root

    def union(a, b):
        ra, rb = find(a), find(b)
        if ra == rb:
            return
        if size[ra] < size[rb]:
            parent[ra] = rb
            size[rb] += size[ra]
        else:
            parent[rb] = ra
            size[ra] += size[rb]

    pairs = sorted_pairs(points)
    for dist, i, j in pairs[: max(0, k)]:
        union(i, j)

    comp = [find(i) for i in range(n)]
    sizes = sorted((comp.count(v) for v in set(comp)), reverse=True)
    product = 1
    for s in sizes[:3]:
        product *= s
    return product


def part1_reference(points, k):
    """Independent reference: small-to-large set merging with relabeling.

    Instead of a parent-pointer union-find, each circuit is a Python set of
    indices and every successful merge relabels the members of the smaller
    circuit into the larger one. This is a genuinely different implementation
    of the same closest-pairs clustering, so agreement on random cases and the
    real input is meaningful evidence for `Day08.part1`.
    """
    n = len(points)
    sets = [frozenset([i]) for i in range(n)]
    lookup = list(range(n))
    for dist, i, j in sorted_pairs(points)[: max(0, k)]:
        si, sj = lookup[i], lookup[j]
        if si == sj:
            continue
        a, b = (si, sj) if len(sets[si]) >= len(sets[sj]) else (sj, si)
        merged = sets[a] | sets[b]
        for member in sets[b]:
            lookup[member] = a
        sets[a] = merged
        sets[b] = frozenset()

    sizes = sorted((len(s) for s in sets if s), reverse=True)
    product = 1
    for s in sizes[:3]:
        product *= s
    return product


def self_test():
    """Deterministic validation: seeded random cross-checks + official sample
    + the accepted Day 08 real-input answer (90036)."""
    random.seed(20251011)

    cases = 2000
    for _ in range(cases):
        n = random.randint(1, 12)
        points = [(random.randint(0, 50), random.randint(0, 50), random.randint(0, 50))
                  for _ in range(n)]
        max_pairs = n * (n - 1) // 2
        k = random.randint(0, max_pairs + 3)
        a = part1_mirror(points, k)
        b = part1_reference(points, k)
        if a != b:
            print(f"FAIL: n={n} k={k} {points} -> mirror {a} != reference {b}",
                  file=sys.stderr)
            return 1

    with open(sample_input_path(), "r", encoding="utf-8") as fh:
        sample = parse_points(fh.read())
    if part1_mirror(sample, SAMPLE_CONNECTIONS) != SAMPLE_ANSWER:
        print(f"FAIL: sample mirror != {SAMPLE_ANSWER}", file=sys.stderr)
        return 1
    if part1_reference(sample, SAMPLE_CONNECTIONS) != SAMPLE_ANSWER:
        print(f"FAIL: sample reference != {SAMPLE_ANSWER}", file=sys.stderr)
        return 1

    with open(real_input_path(), "r", encoding="utf-8") as fh:
        real = parse_points(fh.read())
    if part1_mirror(real, REAL_CONNECTIONS) != REAL_ANSWER:
        print(f"FAIL: real mirror != {REAL_ANSWER}", file=sys.stderr)
        return 1
    if part1_reference(real, REAL_CONNECTIONS) != REAL_ANSWER:
        print(f"FAIL: real reference != {REAL_ANSWER}", file=sys.stderr)
        return 1

    print(f"ok: {cases} random mirror/reference cross-checks, sample == "
          f"{SAMPLE_ANSWER}, real == {REAL_ANSWER}")
    return 0


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


def file_mode(path, k, expected):
    path = repo_input_path(path)
    try:
        with open(path, "r", encoding="utf-8") as fh:
            points = parse_points(fh.read())
    except (OSError, ValueError) as exc:
        print(f"error: cannot read {path}: {exc}", file=sys.stderr)
        return 1
    mirror = part1_mirror(points, k)
    reference = part1_reference(points, k)
    print(f"points={len(points)} connections={k} product={mirror}")
    if mirror != reference:
        print(f"error: mirror {mirror} != reference {reference}", file=sys.stderr)
        return 1
    if expected is not None:
        if mirror != expected:
            print(f"error: expected {expected}, got {mirror}", file=sys.stderr)
            return 1
        print(f"ok: product == {expected}")
    return 0


def main(argv):
    args = argv[1:]
    if not args or args == ["--self-test"]:
        return self_test()
    if len(args) not in (2, 3):
        print("usage: scripts/check-closest-pairs.py [FILE K [EXPECTED]]",
              file=sys.stderr)
        return 2
    path = args[0]
    try:
        k = int(args[1])
    except ValueError:
        print(f"error: connections K must be an integer, got {args[1]!r}",
              file=sys.stderr)
        return 2
    if k < 0:
        print(f"error: connections K must be >= 0, got {k}", file=sys.stderr)
        return 2
    expected = int(args[2]) if len(args) >= 3 else None
    return file_mode(path, k, expected)


if __name__ == "__main__":
    sys.exit(main(sys.argv))