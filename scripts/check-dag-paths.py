#!/usr/bin/env python3
"""check-dag-paths.py - validate and apply the count-distinct-paths-in-a-DAG
technique (distinct directed paths from a source device to a target output).

Deterministic, read-only helper distilled from the Day 11 part 1 review. The
technique (Day 11 "Reactor"): each line names a device followed by the devices
its outputs attach to, so `bbb: ddd eee` means data flows from `bbb` forward to
`ddd` and `eee`. Data only ever moves forward through outputs, so the wiring
forms a directed acyclic graph. The puzzle asks how many distinct directed
paths lead from the device `you` to the device `out`.

Because the graph is finite and acyclic, the number of paths from any device to
`out` is the sum of the counts of its outputs (with `out` itself contributing a
base count of one). This is evaluated with memoised depth-first traversal and
then read off at `you`.

Modes:
  no args / --self-test     Seeded random cross-checks of the memoised DFS
                            mirror (mirroring `Day11.part1`) against an
                            independent explicit path-enumeration reference,
                            then asserts the official Day 11 sample (5) and
                            the accepted Day 11 part 1 real-input answer
                            (543).
  <FILE> [EXPECTED]         Read device wiring from FILE (one line per device
                            in the statement format), print the number of
                            distinct paths from `you` to `out`, and assert it
                            when EXPECTED is given.

Part-two variant (--visits-all): count only the paths from `svr` to `out`
that visit BOTH required devices `dac` and `fft` (in any order), mirroring
`Day11.part2`. Two independent implementations are used: a memoised
subset-mask DFS (the Scala mirror) and an inclusion-exclusion reference
built from part-one path counts with the required devices pruned out.
The self-test cross-checks them on seeded random wirings and asserts the
official part-two sample (2) and the accepted Day 11 part 2 real-input
answer (479511112939968).

Usage:
  scripts/check-dag-paths.py
  scripts/check-dag-paths.py --self-test
  scripts/check-dag-paths.py inputs/Day11Test.txt 5
  scripts/check-dag-paths.py inputs/Day11.txt 543
  scripts/check-dag-paths.py --visits-all
  scripts/check-dag-paths.py --visits-all inputs/Day11.txt 479511112939968

Exit codes:
  0  all checks passed
  1  a check failed or input is malformed
  2  usage error
"""

import os
import random
import sys


SAMPLE_ANSWER = 5
REAL_ANSWER = 543
SOURCE = "you"
TARGET = "out"

# Part-two variant: every counted path must visit both of these devices.
PART2_SOURCE = "svr"
PART2_REQUIRED = ("dac", "fft")
PART2_SAMPLE_ANSWER = 2
PART2_REAL_ANSWER = 479511112939968

# The part-two official sample (13 devices), embedded so the self-test can
# assert it without a committed sample file.
PART2_SAMPLE_TEXT = """svr: aaa bbb
aaa: fft
fft: ccc
bbb: tty
tty: ccc
ccc: ddd eee
ddd: hub
hub: fff
eee: dac
dac: fff
fff: ggg hhh
ggg: out
hhh: out
"""


def real_input_path():
    """Path to the Day 11 real input, resolved relative to this script."""
    here = os.path.dirname(os.path.abspath(__file__))
    return os.path.join(here, "..", "src", "main", "resources", "inputs", "Day11.txt")


def sample_input_path():
    """Path to the Day 11 official 10-device sample."""
    here = os.path.dirname(os.path.abspath(__file__))
    return os.path.join(here, "..", "src", "main", "resources", "inputs", "Day11Test.txt")


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


def parse_wiring(text):
    """Parse device wiring from the statement's `<device>: <output>...` format,
    one line per device.

    Mirrors `Day11.readFile` in src/main/scala/aoc2025/Day11.scala: each
    non-empty line is split at the first ':' into a device name and a
    whitespace-separated output list (which may be empty).
    """
    wiring = {}
    for lineno, line in enumerate(text.splitlines(), start=1):
        stripped = line.strip()
        if not stripped:
            continue
        colon = stripped.find(":")
        if colon <= 0:
            raise ValueError(
                f"line {lineno}: missing ':' separator: {stripped!r}")
        device = stripped[:colon].strip()
        outputs = stripped[colon + 1:].split()
        if not device:
            raise ValueError(f"line {lineno}: empty device name: {stripped!r}")
        wiring[device] = outputs
    if not wiring:
        raise ValueError("no wiring parsed (empty input)")
    return wiring


def count_paths_memo(wiring, source=SOURCE, target=TARGET):
    """Memoised depth-first traversal mirroring `Day11.part1`: a device's path
    count is 1 for the target, else the sum of the counts of its outputs.
    Returns 0 when `source` is unreachable/absent or has no path to the target.
    """
    memo = {}

    def visit(device):
        if device in memo:
            return memo[device]
        if device == target:
            memo[device] = 1
        else:
            memo[device] = sum(visit(out) for out in wiring.get(device, ()))
        return memo[device]

    return visit(source) if source in wiring else 0


def count_paths_enum(wiring, source=SOURCE, target=TARGET):
    """Independent reference: enumerate every distinct path from `source` to
    `target` explicitly with a DFS, counting them one at a time. Structurally
    different from the memoised sum (it materialises each path and never
    reuses a sub-count), so it catches summation/memoisation mistakes. Slow on
    dense DAGs; used only on small random cases and tiny fixtures.
    """
    if source not in wiring:
        return 0

    count = 0

    def search(device):
        nonlocal count
        if device == target:
            count += 1
            return
        for out in wiring.get(device, ()):
            search(out)

    search(source)
    return count


def count_required_memo(wiring, source=PART2_SOURCE, target=TARGET,
                        required=PART2_REQUIRED):
    """Memoised subset-mask DFS mirroring `Day11.part2`: count paths from
    `source` to `target` that visit every device in `required` (in any order).
    Each required device owns a bit; a path is counted only at `target` when
    all bits are set. Memoised on (device, visitedMask), O(v * 2^k + e * 2^k)
    where k is the number of required devices."""
    required_mask = {name: 1 << i for i, name in enumerate(required)}
    required_full = (1 << len(required)) - 1
    memo = {}

    def visit(device, visited):
        key = (device, visited)
        if key in memo:
            return memo[key]
        if device == target:
            memo[key] = 1 if visited == required_full else 0
        else:
            next_visited = visited | required_mask.get(device, 0)
            memo[key] = sum(visit(out, next_visited)
                            for out in wiring.get(device, ()))
        return memo[key]

    return visit(source, 0) if source in wiring else 0


def count_required_ie(wiring, source=PART2_SOURCE, target=TARGET,
                      required=PART2_REQUIRED):
    """Independent inclusion-exclusion reference for the part-two count.

    The number of paths visiting every required device equals
    sum_{S subset of required} (-1)^|S| * (paths avoiding all of S), where
    "paths avoiding a set" is computed by pruning those devices out of the
    graph and counting plain paths (count_paths_memo). This is structurally
    different from the subset-mask DFS, so agreement catches mask/bit bugs.
    """
    total = 0
    for bits in range(1 << len(required)):
        avoid = {name for i, name in enumerate(required) if bits & (1 << i)}
        pruned = {d: [o for o in outs if o not in avoid]
                  for d, outs in wiring.items() if d not in avoid}
        count = count_paths_memo(pruned, source, target)
        if len(avoid) % 2 == 0:
            total += count
        else:
            total -= count
    return total


def random_wiring_with_required(rng):
    """Build a random acyclic wiring that contains the part-two source,
    target and required devices. Devices are laid out in a fixed total order
    (source, required devices, extras, target) and edges only go from an
    earlier device to a later one, guaranteeing acyclicity. The target is
    last so the required devices genuinely lie on paths to it; some random
    cases have no valid path and some do."""
    n = rng.randint(4, 12)
    extras = [f"e{i}" for i in range(n - 2 - len(PART2_REQUIRED))]
    devices = [PART2_SOURCE] + list(PART2_REQUIRED) + extras + [TARGET]
    wiring = {d: [] for d in devices}
    for i in range(len(devices)):
        for j in range(i + 1, len(devices)):
            if rng.random() < 0.30:
                wiring[devices[i]].append(devices[j])
    return wiring


def self_test_visits_all():
    """Deterministic validation of the part-two variant: seeded random
    cross-checks of the subset-mask DFS mirror against the inclusion-exclusion
    reference, plus the official part-two sample (2) and the accepted part-two
    real-input answer (479511112939968)."""
    rng = random.Random(20261011)

    cases = 3000
    for _ in range(cases):
        wiring = random_wiring_with_required(rng)
        a = count_required_memo(wiring)
        b = count_required_ie(wiring)
        if a != b:
            print(f"FAIL: wiring={wiring} -> memo {a} != IE {b}",
                  file=sys.stderr)
            return 1

    sample = parse_wiring(PART2_SAMPLE_TEXT)
    assert len(sample) == 13, f"part2 sample should have 13 devices, got {len(sample)}"
    sample_memo = count_required_memo(sample)
    sample_ie = count_required_ie(sample)
    if sample_memo != PART2_SAMPLE_ANSWER or sample_ie != PART2_SAMPLE_ANSWER:
        print(f"FAIL: part2 sample memo {sample_memo} / IE {sample_ie} "
              f"!= {PART2_SAMPLE_ANSWER}", file=sys.stderr)
        return 1

    with open(real_input_path(), "r", encoding="utf-8") as fh:
        real = parse_wiring(fh.read())
    real_memo = count_required_memo(real)
    real_ie = count_required_ie(real)
    if real_memo != PART2_REAL_ANSWER or real_ie != PART2_REAL_ANSWER:
        print(f"FAIL: real memo {real_memo} / IE {real_ie} "
              f"!= {PART2_REAL_ANSWER}", file=sys.stderr)
        return 1

    print(f"ok: {cases} memo/IE cross-checks, part2 sample == {PART2_SAMPLE_ANSWER}, "
          f"real == {PART2_REAL_ANSWER}")
    return 0


def random_wiring(rng):
    """Build a random acyclic wiring. Acyclicity is guaranteed by laying out
    `n` devices in a fixed total order and only allowing edges from an earlier
    device to a later one. `you` is placed first and `out` last so the reach
    of `you` is a strict sub-DAG of the whole graph (exercising both the
    reachable and unreachable cases)."""
    n = rng.randint(3, 12)
    devices = [SOURCE, TARGET] + [f"d{i}" for i in range(n - 2)]
    wiring = {d: [] for d in devices}
    for i in range(n):
        for j in range(i + 1, n):
            if rng.random() < 0.30:
                wiring[devices[i]].append(devices[j])
    return wiring


def self_test():
    """Deterministic validation: seeded random cross-checks of the memoised
    mirror against the explicit enumeration reference + official sample (5) +
    the accepted Day 11 real-input answer (543)."""
    rng = random.Random(20261011)

    cases = 4000
    for _ in range(cases):
        wiring = random_wiring(rng)
        a = count_paths_memo(wiring)
        b = count_paths_enum(wiring)
        if a != b:
            print(f"FAIL: wiring={wiring} -> memo {a} != enum {b}",
                  file=sys.stderr)
            return 1

    with open(sample_input_path(), "r", encoding="utf-8") as fh:
        sample = parse_wiring(fh.read())
    assert len(sample) == 10, f"sample should have 10 devices, got {len(sample)}"
    sample_memo = count_paths_memo(sample)
    sample_enum = count_paths_enum(sample)
    if sample_memo != SAMPLE_ANSWER or sample_enum != SAMPLE_ANSWER:
        print(f"FAIL: sample memo {sample_memo} / enum {sample_enum} "
              f"!= {SAMPLE_ANSWER}", file=sys.stderr)
        return 1

    with open(real_input_path(), "r", encoding="utf-8") as fh:
        real = parse_wiring(fh.read())
    assert len(real) == 581, f"real input should have 581 devices, got {len(real)}"
    real_memo = count_paths_memo(real)
    if real_memo != REAL_ANSWER:
        print(f"FAIL: real memo {real_memo} != {REAL_ANSWER}", file=sys.stderr)
        return 1

    print(f"ok: {cases} memo/enum cross-checks, sample == {SAMPLE_ANSWER}, "
          f"real == {REAL_ANSWER}")
    return 0


def file_mode(path, expected, visits_all=False):
    path = repo_input_path(path)
    try:
        with open(path, "r", encoding="utf-8") as fh:
            wiring = parse_wiring(fh.read())
    except (OSError, ValueError) as exc:
        print(f"error: cannot read {path}: {exc}", file=sys.stderr)
        return 1
    if visits_all:
        total = count_required_memo(wiring)
        print(f"devices={len(wiring)} required_paths={total}")
        label = "required_paths"
    else:
        total = count_paths_memo(wiring)
        print(f"devices={len(wiring)} paths={total}")
        label = "paths"
    if expected is not None:
        if total != expected:
            print(f"error: expected {expected}, got {total}", file=sys.stderr)
            return 1
        print(f"ok: {label} == {expected}")
    return 0


def main(argv):
    args = argv[1:]
    want_self_test = "--self-test" in args
    visits_all = "--visits-all" in args
    args = [a for a in args if a not in ("--self-test", "--visits-all")]
    if (want_self_test or not args) and not visits_all:
        return self_test()
    if (want_self_test or not args) and visits_all:
        return self_test_visits_all()
    if len(args) not in (1, 2):
        print("usage: scripts/check-dag-paths.py [--visits-all] [FILE [EXPECTED]]",
              file=sys.stderr)
        return 2
    expected = int(args[1]) if len(args) >= 2 else None
    return file_mode(args[0], expected, visits_all=visits_all)


if __name__ == "__main__":
    sys.exit(main(sys.argv))
