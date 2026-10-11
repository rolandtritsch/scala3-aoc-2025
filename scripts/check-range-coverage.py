#!/usr/bin/env python3
"""check-range-coverage.py - validate and apply the inclusive-range membership
counting technique.

Deterministic, read-only helper distilled from the Day 05 reviews. The
technique: a set of inclusive `first-last` ranges may overlap, and an item ID
is covered when it falls inside at least one range. Two recurring AoC shapes:

  1. Covered-count (Day 05 part 1, accepted answer 848): how many of a given
     list of available IDs are covered by any range.
  2. Union-count (Day 05 part 2, accepted answer 334714395325710): how many
     IDs in total the ranges consider covered, i.e. the size of the merged
     union of the inclusive ranges, ignoring the available-ID list entirely.

Modes:
  no args / --self-test     Seeded random cross-check of the naive per-ID scan
                            against an independent merged-interval binary-search
                            reference, then asserts the official Day 05 sample
                            (3 of {1, 5, 8, 11, 17, 32}) and the accepted Day 05
                            part 1 real-input answer (848).
  --union                   Seeded random cross-check of a naive union (explicit
                            set) against the merged-length union, then asserts
                            the official Day 05 part 2 sample (14) and the
                            accepted Day 05 part 2 real-input answer
                            (334714395325710).
  <FILE> [EXPECTED]         Read ranges (blank line separated from available IDs,
                            one ID per line), print the covered count of the
                            available IDs, and assert it when EXPECTED is given.
  --union <FILE> [EXPECTED] Read ranges, print the merged union count (ignoring
                            the available-ID list), and assert it when EXPECTED
                            is given.

Usage:
  scripts/check-range-coverage.py
  scripts/check-range-coverage.py --self-test
  scripts/check-range-coverage.py --union
  scripts/check-range-coverage.py inputs/Day05.txt 848
  scripts/check-range-coverage.py --union inputs/Day05.txt 334714395325710

Exit codes:
  0  all checks passed
  1  a check failed or input is malformed
  2  usage error
"""

import os
import random
import sys

SAMPLE_RANGES = [(3, 5), (10, 14), (16, 20), (12, 18)]
SAMPLE_IDS = [1, 5, 8, 11, 17, 32]
SAMPLE_COUNT = 3
REAL_COUNT = 848
SAMPLE_UNION = 14
REAL_UNION = 334714395325710


def real_input_path():
    """Path to the Day 05 real input, resolved relative to this script."""
    here = os.path.dirname(os.path.abspath(__file__))
    return os.path.join(here, "..", "src", "main", "resources", "inputs", "Day05.txt")


def parse_input(text):
    """Parse ranges then available IDs, separated by a blank line.

    Every line before the first blank line must be a `first-last` inclusive
    range with first <= last; every non-blank line after it is an ID.
    Mirrors `Day05.readFile` in src/main/scala/aoc2025/Day05.scala.
    """
    lines = [line.strip() for line in text.splitlines()]
    try:
        separator = lines.index("")
    except ValueError:
        raise ValueError("missing blank line separating ranges from available IDs")
    if separator <= 0:
        raise ValueError("no ranges before the blank line")

    ranges = []
    for line in lines[:separator]:
        dash = line.find("-")
        if dash <= 0:
            raise ValueError(f"invalid range (missing '-'): {line!r}")
        first, last = int(line[:dash]), int(line[dash + 1:])
        if first > last:
            raise ValueError(f"range first ({first}) > last ({last}): {line!r}")
        ranges.append((first, last))

    ids = [int(line) for line in lines[separator + 1:] if line]
    if not ranges:
        raise ValueError("no ranges parsed")
    return ranges, ids


def count_covered_naive(ids, ranges):
    """Covered count via a direct per-ID scan: an ID is covered when it lies
    inside any range. Mirrors `Day05.isFresh`/`Day05.part1` in
    src/main/scala/aoc2025/Day05.scala."""
    return sum(1 for ident in ids if any(lo <= ident <= hi for lo, hi in ranges))


def merge_ranges(ranges):
    """Merge overlapping/adjacent inclusive ranges into sorted disjoint spans."""
    spans = sorted(ranges)
    merged = []
    for lo, hi in spans:
        if merged and lo <= merged[-1][1] + 1:
            merged[-1] = (merged[-1][0], max(merged[-1][1], hi))
        else:
            merged.append((lo, hi))
    return merged


def count_covered_merged(ids, ranges):
    """Independent reference: binary-search each ID in merged disjoint spans."""
    spans = merge_ranges(ranges)
    starts = [lo for lo, _ in spans]

    def covered(ident):
        import bisect
        i = bisect.bisect_right(starts, ident) - 1
        return i >= 0 and ident <= spans[i][1]

    return sum(1 for ident in ids if covered(ident))


def union_count(ranges):
    """Size of the union of the inclusive ranges, ignoring any ID list.

    Merge overlapping/adjacent ranges into disjoint spans and sum their
    lengths. Mirrors `Day05.mergedRanges`/`Day05.part2` in
    src/main/scala/aoc2025/Day05.scala."""
    return sum(hi - lo + 1 for lo, hi in merge_ranges(ranges))


def naive_union_count(ranges):
    """Reference union count by materializing the covered ID set.

    Only safe for small ranges; used purely as the seeded random cross-check
    for `union_count`, never on the (huge-ID) real input."""
    covered = set()
    for lo, hi in ranges:
        covered.update(range(lo, hi + 1))
    return len(covered)


def self_test():
    """Deterministic validation: seeded random cross-checks + official sample
    + the accepted Day 05 real-input answer (848)."""
    random.seed(20251011)

    # Random small cases: naive scan vs merged-interval binary search, plus
    # inclusive-range boundary and overlap coverage.
    cases = 2000
    for _ in range(cases):
        n_ranges = random.randint(1, 5)
        ranges = []
        for _ in range(n_ranges):
            lo = random.randint(0, 60)
            hi = random.randint(lo, lo + random.randint(0, 40))
            ranges.append((lo, hi))
        ids = [random.randint(0, 100) for _ in range(random.randint(0, 12))]
        a = count_covered_naive(ids, ranges)
        b = count_covered_merged(ids, ranges)
        if a != b:
            print(f"FAIL: count {ranges} {ids} -> {a} != {b}", file=sys.stderr)
            return 1

    # Official Day 05 sample: 3 of {1, 5, 8, 11, 17, 32} are covered.
    if count_covered_naive(SAMPLE_IDS, SAMPLE_RANGES) != SAMPLE_COUNT:
        print(f"FAIL: sample count != {SAMPLE_COUNT}", file=sys.stderr)
        return 1
    if count_covered_merged(SAMPLE_IDS, SAMPLE_RANGES) != SAMPLE_COUNT:
        print(f"FAIL: sample count (merged) != {SAMPLE_COUNT}", file=sys.stderr)
        return 1

    # Accepted Day 05 real-input answer: 848 fresh available IDs.
    with open(real_input_path(), "r", encoding="utf-8") as fh:
        real_ranges, real_ids = parse_input(fh.read())
    if count_covered_naive(real_ids, real_ranges) != REAL_COUNT:
        print(f"FAIL: real count != {REAL_COUNT}", file=sys.stderr)
        return 1
    if count_covered_merged(real_ids, real_ranges) != REAL_COUNT:
        print(f"FAIL: real count (merged) != {REAL_COUNT}", file=sys.stderr)
        return 1

    print(f"ok: {cases} random naive/merged cross-checks, sample count == "
          f"{SAMPLE_COUNT}, real count == {REAL_COUNT}")
    return 0


def union_test():
    """Deterministic validation of the union-count mode: seeded random
    naive-vs-merged union cross-checks, the official Day 05 part 2 sample
    (14), and the accepted Day 05 part 2 real-input answer
    (334714395325710)."""
    random.seed(20251011)

    # Random small cases: naive set union vs merged-length union.
    cases = 2000
    for _ in range(cases):
        n_ranges = random.randint(1, 5)
        ranges = []
        for _ in range(n_ranges):
            lo = random.randint(0, 60)
            hi = random.randint(lo, lo + random.randint(0, 40))
            ranges.append((lo, hi))
        a = naive_union_count(ranges)
        b = union_count(ranges)
        if a != b:
            print(f"FAIL: union {ranges} -> {a} != {b}", file=sys.stderr)
            return 1

    # Official Day 05 part 2 sample: ranges 3-5,10-14,16-20,12-18 -> 14 IDs.
    if union_count(SAMPLE_RANGES) != SAMPLE_UNION:
        print(f"FAIL: sample union != {SAMPLE_UNION}", file=sys.stderr)
        return 1

    # Accepted Day 05 part 2 real-input answer: merged union of the 182 ranges.
    with open(real_input_path(), "r", encoding="utf-8") as fh:
        real_ranges, _ = parse_input(fh.read())
    if union_count(real_ranges) != REAL_UNION:
        print(f"FAIL: real union != {REAL_UNION}", file=sys.stderr)
        return 1

    print(f"ok: {cases} random naive/merged union cross-checks, sample union == "
          f"{SAMPLE_UNION}, real union == {REAL_UNION}")
    return 0


def file_mode(path, expected):
    try:
        with open(path, "r", encoding="utf-8") as fh:
            ranges, ids = parse_input(fh.read())
    except (OSError, ValueError) as exc:
        print(f"error: cannot read {path}: {exc}", file=sys.stderr)
        return 1
    count = count_covered_naive(ids, ranges)
    merged = count_covered_merged(ids, ranges)
    print(f"ranges={len(ranges)} ids={len(ids)} covered={count}")
    if count != merged:
        print(f"error: naive {count} != merged {merged}", file=sys.stderr)
        return 1
    if expected is not None:
        if count != expected:
            print(f"error: expected {expected}, got {count}", file=sys.stderr)
            return 1
        print(f"ok: covered == {expected}")
    return 0


def union_file_mode(path, expected):
    """Print the merged union count of the ranges in FILE, ignoring the
    available-ID list, and assert it when EXPECTED is given."""
    try:
        with open(path, "r", encoding="utf-8") as fh:
            ranges, _ = parse_input(fh.read())
    except (OSError, ValueError) as exc:
        print(f"error: cannot read {path}: {exc}", file=sys.stderr)
        return 1
    spans = merge_ranges(ranges)
    count = union_count(ranges)
    print(f"ranges={len(ranges)} merged={len(spans)} union={count}")
    # Idempotence check: re-merging disjoint spans must be a fixed point.
    if merge_ranges(spans) != spans:
        print("error: merge is not idempotent", file=sys.stderr)
        return 1
    if expected is not None:
        if count != expected:
            print(f"error: expected {expected}, got {count}", file=sys.stderr)
            return 1
        print(f"ok: union == {expected}")
    return 0


def main(argv):
    args = argv[1:]
    union = "--union" in args
    args = [a for a in args if a not in ("--self-test", "--union")]
    if not args:
        return union_test() if union else self_test()
    if len(args) not in (1, 2):
        print("usage: scripts/check-range-coverage.py [--union] [FILE [EXPECTED]]",
              file=sys.stderr)
        return 2
    path = args[0]
    expected = int(args[1]) if len(args) >= 2 else None
    if union:
        return union_file_mode(path, expected)
    return file_mode(path, expected)


if __name__ == "__main__":
    sys.exit(main(sys.argv))