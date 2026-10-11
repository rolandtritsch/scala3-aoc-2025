#!/usr/bin/env python3
"""check-toggle-presses.py - validate and apply the button-toggle minimum-press
technique (minimum-weight subset of toggle XOR masks that reaches a target).

Deterministic, read-only helper distilled from the Day 10 part 1 review. The
technique (Day 10 "Factory"): each machine line lists a target indicator-light
diagram `[....]` and button wiring schematics `(a,b)`; each button toggles the
listed lights. Pressing a button twice restores the prior state at twice the
cost, so an optimal configuration presses every button 0 or 1 times: the
minimum is the smallest subset of buttons whose toggle-XOR equals the target
light pattern. The answer sums that per-machine minimum over all machines.

In bit terms, each button is a mask (bit `i` set when it toggles light `i`),
the target is a mask, and we want `min |S|` over `XOR(buttons[i] for i in S)
== target` -- the classic minimum-weight solution of a linear system over
GF(2).

Modes:
  no args / --self-test     Seeded random cross-check of the subset-enumeration
                            mirror (mirroring `Day10.part1`, iterates every
                            subset) against an independent BFS reference over
                            the light-pattern XOR state graph, then asserts the
                            official Day 10 sample (3 machines -> 7) and the
                            accepted Day 10 part 1 real-input answer
                            (163 machines -> 417).
  <FILE> [EXPECTED]         Read machines from FILE (one per line in the
                            statement format), print the total minimum presses
                            and per-machine minima, and assert it when EXPECTED
                            is given.

Usage:
  scripts/check-toggle-presses.py
  scripts/check-toggle-presses.py --self-test
  scripts/check-toggle-presses.py inputs/Day10Test.txt 7
  scripts/check-toggle-presses.py inputs/Day10.txt 417

Exit codes:
  0  all checks passed
  1  a check failed or input is malformed
  2  usage error
"""

import os
import random
import sys


SAMPLE_ANSWER = 7
REAL_ANSWER = 417


def real_input_path():
    """Path to the Day 10 real input, resolved relative to this script."""
    here = os.path.dirname(os.path.abspath(__file__))
    return os.path.join(here, "..", "src", "main", "resources", "inputs", "Day10.txt")


def sample_input_path():
    """Path to the Day 10 official 3-machine sample."""
    here = os.path.dirname(os.path.abspath(__file__))
    return os.path.join(here, "..", "src", "main", "resources", "inputs", "Day10Test.txt")


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


def parse_machines(text):
    """Parse machines from the statement's `[diagram] (btn) ... {joltages}`
    format, one per line.

    Mirrors `Day10.readFile` in src/main/scala/aoc2025/Day10.scala: the leading
    bracketed diagram becomes the target mask (bit `i` set when light `i` is
    on), each parenthesised button group is a mask (bit `id` set for each listed
    light; a light listed twice XORs back out), and the trailing braced joltages
    are ignored for part 1.
    """
    machines = []
    for lineno, line in enumerate(text.splitlines(), start=1):
        stripped = line.strip()
        if not stripped:
            continue
        close = stripped.find("]")
        if not stripped.startswith("[") or close <= 0:
            raise ValueError(f"line {lineno}: missing light diagram: {stripped!r}")
        diagram = stripped[1:close]
        target = 0
        for i, ch in enumerate(diagram):
            if ch == "#":
                target |= 1 << i

        buttons = []
        i = close + 1
        text_len = len(stripped)
        while i < text_len:
            if stripped[i] == "(":
                end = stripped.find(")", i)
                if end < 0:
                    raise ValueError(f"line {lineno}: unterminated button: {stripped!r}")
                ids = [tok for tok in stripped[i + 1:end].split(",") if tok]
                mask = 0
                for tok in ids:
                    try:
                        mask ^= 1 << int(tok)
                    except ValueError as exc:
                        raise ValueError(
                            f"line {lineno}: non-integer light id {tok!r} in {stripped!r}") from exc
                buttons.append(mask)
                i = end + 1
            elif stripped[i] == "{":
                i = text_len
            else:
                i += 1
        machines.append((target, tuple(buttons)))
    if not machines:
        raise ValueError("no machines parsed")
    return machines


def min_presses_enum(machine):
    """Minimum presses over every button subset (full enumeration).

    Mirrors `Day10.part1`: enumerate all `2^b` subsets and keep the smallest
    subset whose toggle-XOR equals the target. Returns the minimum count, or -1
    when the target is unreachable.
    """
    target, buttons = machine
    b = len(buttons)
    best = -1
    for subset in range(1 << b):
        x = 0
        weight = 0
        for j in range(b):
            if (subset >> j) & 1:
                x ^= buttons[j]
                weight += 1
        if x == target and (best < 0 or weight < best):
            best = weight
    return best


def min_presses_bfs(machine):
    """Minimum presses via BFS over the light-pattern XOR state graph.

    Independent reference: the state is the current on-light mask; pressing a
    button XORs its mask into the state at cost 1. The minimum number of
    presses from the all-off state (0) to the target is the BFS distance, which
    never needs more than one press per button (pressing twice is a self-loop
    at extra cost). Works for masks up to ~20 bits; trivially sized for this
    puzzle (at most 10 lights). Returns the distance, or -1 when unreachable.
    """
    target, buttons = machine
    if target == 0:
        return 0
    if not buttons:
        return -1
    start = 0
    visited = {start}
    frontier = [start]
    dist = 0
    while frontier:
        dist += 1
        nxt = []
        for state in frontier:
            for mask in buttons:
                nxt_state = state ^ mask
                if nxt_state == target:
                    return dist
                if nxt_state not in visited:
                    visited.add(nxt_state)
                    nxt.append(nxt_state)
        frontier = nxt
    return -1


def total_presses(machines, fn):
    """Sum of `fn` over every machine, mirroring `Day10.totalPresses`."""
    return sum(fn(m) for m in machines)


def self_test():
    """Deterministic validation: seeded random cross-checks + official sample
    + the accepted Day 10 real-input answer (417)."""
    rng = random.Random(20261011)

    cases = 2000
    for _ in range(cases):
        lights = rng.randint(1, 9)
        b = rng.randint(0, 11)
        buttons = tuple(
            rng.randint(0, (1 << lights) - 1) for _ in range(b))
        target = rng.randint(0, (1 << lights) - 1)
        machine = (target, buttons)
        a = min_presses_enum(machine)
        c = min_presses_bfs(machine)
        if a != c:
            print(f"FAIL: lights={lights} buttons={buttons} target={target} "
                  f"-> enum {a} != bfs {c}", file=sys.stderr)
            return 1

    with open(sample_input_path(), "r", encoding="utf-8") as fh:
        sample = parse_machines(fh.read())
    assert len(sample) == 3, f"sample should have 3 machines, got {len(sample)}"
    per_machine = [min_presses_enum(m) for m in sample]
    if per_machine != [2, 3, 2]:
        print(f"FAIL: sample per-machine {per_machine} != [2, 3, 2]",
              file=sys.stderr)
        return 1
    if total_presses(sample, min_presses_enum) != SAMPLE_ANSWER:
        print(f"FAIL: sample total != {SAMPLE_ANSWER}", file=sys.stderr)
        return 1
    if total_presses(sample, min_presses_bfs) != SAMPLE_ANSWER:
        print(f"FAIL: sample total (BFS) != {SAMPLE_ANSWER}", file=sys.stderr)
        return 1

    with open(real_input_path(), "r", encoding="utf-8") as fh:
        real = parse_machines(fh.read())
    assert len(real) == 163, f"real input should have 163 machines, got {len(real)}"
    enum_total = total_presses(real, min_presses_enum)
    bfs_total = total_presses(real, min_presses_bfs)
    if enum_total != REAL_ANSWER or bfs_total != REAL_ANSWER:
        print(f"FAIL: real enum {enum_total} / bfs {bfs_total} != {REAL_ANSWER}",
              file=sys.stderr)
        return 1

    print(f"ok: {cases} random enum/bfs cross-checks, sample == {SAMPLE_ANSWER}, "
          f"real == {REAL_ANSWER}")
    return 0


def file_mode(path, expected):
    path = repo_input_path(path)
    try:
        with open(path, "r", encoding="utf-8") as fh:
            machines = parse_machines(fh.read())
    except (OSError, ValueError) as exc:
        print(f"error: cannot read {path}: {exc}", file=sys.stderr)
        return 1
    per_machine = [min_presses_enum(m) for m in machines]
    enum_total = sum(per_machine)
    bfs_total = total_presses(machines, min_presses_bfs)
    print(f"machines={len(machines)} per_machine={per_machine} total={enum_total}")
    if enum_total != bfs_total:
        print(f"error: enum {enum_total} != bfs {bfs_total}", file=sys.stderr)
        return 1
    if expected is not None:
        if enum_total != expected:
            print(f"error: expected {expected}, got {enum_total}", file=sys.stderr)
            return 1
        print(f"ok: total == {expected}")
    return 0


def main(argv):
    args = argv[1:]
    want_self_test = "--self-test" in args
    args = [a for a in args if a != "--self-test"]
    if want_self_test or not args:
        return self_test()
    if len(args) not in (1, 2):
        print("usage: scripts/check-toggle-presses.py [FILE [EXPECTED]]",
              file=sys.stderr)
        return 2
    expected = int(args[1]) if len(args) >= 2 else None
    return file_mode(args[0], expected)


if __name__ == "__main__":
    sys.exit(main(sys.argv))