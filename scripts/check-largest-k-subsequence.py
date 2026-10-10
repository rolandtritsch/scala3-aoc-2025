#!/usr/bin/env python3
"""check-largest-k-subsequence.py - validate and apply the largest length-k
subsequence greedy (monotonic stack) technique.

Deterministic, read-only helper distilled from the Day 03 part 2 review
(AOC-111, reviewed commit 33980c5). The technique: from a digit sequence,
keep exactly k characters in their original order to maximize the resulting
number -- the lexicographically largest length-k subsequence. It is the
general form of "turn on exactly twelve batteries" (Day 03 part 2) and of
"remove n digits so the remaining number is largest".

Modes:
  no args / --self-test   Seeded random brute-force cross-check of the greedy
                          against itertools.combinations (short/medium banks),
                          an independent window-based cross-check (long banks),
                          plus the Day 03 part 2 sample banks (k=12) with their
                          known answers.
  <FILE> [K] [EXPECTED]   Read one bank per non-blank line, compute the sum of
                          the largest K-subsequence of each bank, and (when
                          EXPECTED is given) assert that the total equals it.

Usage:
  scripts/check-largest-k-subsequence.py
  scripts/check-largest-k-subsequence.py --self-test
  scripts/check-largest-k-subsequence.py inputs/Day03.txt 12 170997883706617

Exit codes:
  0  all checks passed
  1  a check failed or input is malformed
"""

import itertools
import random
import sys

SAMPLE_BANKS = [
    "987654321111111",
    "811111111111119",
    "234234234234278",
    "818181911112111",
]
SAMPLE_K = 12
SAMPLE_EXPECTED = [
    987654321111,
    811111111119,
    434234234278,
    888911112111,
]
SAMPLE_TOTAL = 3121910778619


def largest_k_greedy(seq, k):
    """Largest length-k subsequence via a monotonic stack (one left-to-right pass).

    Pop a smaller kept digit whenever a later, larger digit can replace it and a
    removal is still available; leftover removals are dropped from the end.
    Mirrors `Day03.maxJoltage12` in src/main/scala/aoc2025/Day03.scala.
    """
    if not 0 <= k <= len(seq):
        raise ValueError("k must satisfy 0 <= k <= len(seq)")
    remove = len(seq) - k
    stack = []
    for digit in seq:
        while remove > 0 and stack and stack[-1] < digit:
            stack.pop()
            remove -= 1
        stack.append(digit)
    kept = stack[:-remove] if remove else stack
    return int("".join(kept)) if kept else 0


def largest_k_brute(seq, k):
    """Exhaustive reference: try every length-k subsequence and take the max."""
    if k == 0:
        return 0
    return max(int("".join(c)) for c in itertools.combinations(seq, k))


def largest_k_window(seq, k):
    """Independent reference: greedily pick the leftmost maximum digit inside a
    sliding window that shrinks by the number of removals still allowed."""
    if not 0 <= k <= len(seq):
        raise ValueError("k must satisfy 0 <= k <= len(seq)")
    result = []
    start = 0
    removals = len(seq) - k
    while len(result) < k:
        end = start + removals + 1
        best = max(seq[start:end])
        best_idx = seq.index(best, start, end)
        result.append(best)
        removals -= best_idx - start
        start = best_idx + 1
    return int("".join(result))


def self_test():
    """Deterministic validation: seeded random brute-force + long-bank window
    cross-checks + the Day 03 part 2 sample answers."""
    random.seed(20251010)

    # Short/medium banks: exhaustive brute force is feasible.
    short_cases = 2000
    for _ in range(short_cases):
        n = random.randint(12, 20)
        bank = "".join(random.choice("123456789") for _ in range(n))
        k = random.randint(1, n)
        got = largest_k_greedy(bank, k)
        want = largest_k_brute(bank, k)
        if got != want:
            print(f"FAIL: greedy({bank}, {k}) = {got} != brute {want}", file=sys.stderr)
            return 1

    # Long banks (up to the real-input scale, length 100 / k = 12): brute force
    # is infeasible, so cross-check the greedy against the independent window
    # algorithm.
    long_cases = 500
    for _ in range(long_cases):
        n = random.randint(12, 100)
        bank = "".join(random.choice("123456789") for _ in range(n))
        k = random.randint(1, n)
        got = largest_k_greedy(bank, k)
        want = largest_k_window(bank, k)
        if got != want:
            print(f"FAIL: greedy({bank[:30]}..., {k}) = {got} != window {want}", file=sys.stderr)
            return 1

    # Day 03 part 2 sample: known per-bank answers and total.
    for bank, want in zip(SAMPLE_BANKS, SAMPLE_EXPECTED):
        got = largest_k_greedy(bank, SAMPLE_K)
        if got != want:
            print(f"FAIL: sample bank {bank} -> {got} != {want}", file=sys.stderr)
            return 1
    total = sum(largest_k_greedy(b, SAMPLE_K) for b in SAMPLE_BANKS)
    if total != SAMPLE_TOTAL:
        print(f"FAIL: sample total {total} != {SAMPLE_TOTAL}", file=sys.stderr)
        return 1

    print(f"ok: {short_cases} brute-force + {long_cases} window cross-checks, "
          f"sample total == {SAMPLE_TOTAL}")
    return 0


def file_mode(path, k, expected):
    try:
        with open(path, "r", encoding="utf-8") as fh:
            banks = [line.strip() for line in fh if line.strip()]
    except OSError as exc:
        print(f"error: cannot read {path}: {exc}", file=sys.stderr)
        return 1
    if not banks:
        print(f"error: no non-blank banks in {path}", file=sys.stderr)
        return 1
    for bank in banks:
        if not bank.isdigit():
            print(f"error: bank is not a digit sequence: {bank!r}", file=sys.stderr)
            return 1
        if k > len(bank):
            print(f"error: k={k} > len({bank!r})={len(bank)}", file=sys.stderr)
            return 1
    total = sum(largest_k_greedy(bank, k) for bank in banks)
    print(f"banks={len(banks)} k={k} total={total}")
    if expected is not None:
        if total != expected:
            print(f"error: expected total {expected}, got {total}", file=sys.stderr)
            return 1
        print(f"ok: total == {expected}")
    return 0


def main(argv):
    args = [a for a in argv[1:] if a not in ("--self-test",)]
    if not args:
        return self_test()
    if len(args) not in (1, 2, 3):
        print("usage: scripts/check-largest-k-subsequence.py [FILE [K [EXPECTED]]]",
              file=sys.stderr)
        return 2
    path = args[0]
    k = int(args[1]) if len(args) >= 2 else 12
    expected = int(args[2]) if len(args) >= 3 else None
    return file_mode(path, k, expected)


if __name__ == "__main__":
    sys.exit(main(sys.argv))