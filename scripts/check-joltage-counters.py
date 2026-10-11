#!/usr/bin/env python3
"""check-joltage-counters.py - validate and apply the button-press minimum for
additive joltage counters (exact integer programming over 0/1 button columns).

Deterministic, read-only helper distilled from the Day 10 part 2 review. The
technique (Day 10 "Factory", part 2): each machine line lists button wiring
schematics `(a,b)` and joltage requirements `{v0,v1,...}`. Ignoring the light
diagram, each button now increments its named counters by exactly 1 per press
and the counters all start at 0; the task is the fewest total presses to drive
every counter to its target joltage.

In linear-programming terms this is the integer program

    minimise   sum_j x_j
    subject to for every counter i: sum_{j: bit i set in button j} x_j == b_i,
               x_j >= 0 integer

where column j of the 0/1 incidence matrix is button j's mask and b is the
joltage target vector. The mirror implementation solves it exactly with
branch-and-bound over the LP relaxation using a rational simplex
(conceptually mirroring `Day10.part2` / `CounterSolver` in
src/main/scala/aoc2025/Day10.scala), and an independent brute-force ground
truth is used on small random instances.

Modes:
  no args / --self-test     Seeded random cross-checks of the branch-and-bound
                            mirror against the independent brute-force
                            enumeration (small and medium instances), then
                            asserts the official Day 10 sample (3 machines ->
                            per-machine [10, 12, 11], total 33) and the
                            accepted Day 10 part 2 real-input answer
                            (163 machines -> 16765).
  <FILE> [EXPECTED]         Read machines from FILE (one per line in the
                            statement format), print the total minimum presses
                            and per-machine minima, and assert it when EXPECTED
                            is given.

Usage:
  scripts/check-joltage-counters.py
  scripts/check-joltage-counters.py --self-test
  scripts/check-joltage-counters.py inputs/Day10Test.txt 33
  scripts/check-joltage-counters.py inputs/Day10.txt 16765

Exit codes:
  0  all checks passed
  1  a check failed or input is malformed
  2  usage error
"""

import os
import random
import sys
from fractions import Fraction


SAMPLE_ANSWER = 33
REAL_ANSWER = 16765


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
    bracketed diagram is the part-1 target (ignored here), each parenthesised
    button group is a mask (bit `id` set for each listed counter), and the
    braced list is the joltage target vector.
    """
    machines = []
    for lineno, line in enumerate(text.splitlines(), start=1):
        stripped = line.strip()
        if not stripped:
            continue
        close = stripped.find("]")
        if not stripped.startswith("[") or close <= 0:
            raise ValueError(f"line {lineno}: missing light diagram: {stripped!r}")

        buttons = []
        joltages = None
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
                        mask |= 1 << int(tok)
                    except ValueError as exc:
                        raise ValueError(
                            f"line {lineno}: non-integer counter id {tok!r} "
                            f"in {stripped!r}") from exc
                buttons.append(mask)
                i = end + 1
            elif stripped[i] == "{":
                end = stripped.find("}", i)
                if end < 0:
                    raise ValueError(f"line {lineno}: unterminated joltages: {stripped!r}")
                toks = [tok for tok in stripped[i + 1:end].split(",") if tok]
                try:
                    joltages = tuple(int(tok) for tok in toks)
                except ValueError as exc:
                    raise ValueError(
                        f"line {lineno}: non-integer joltage {exc} in {stripped!r}")
                i = end + 1
            else:
                i += 1
        if not buttons:
            raise ValueError(f"line {lineno}: no buttons parsed: {stripped!r}")
        if joltages is None:
            raise ValueError(f"line {lineno}: no joltage requirements: {stripped!r}")
        machines.append((tuple(buttons), joltages))
    if not machines:
        raise ValueError("no machines parsed")
    return machines


def simplex(a, b, c):
    """Solve the equality-form LP `min c.x s.t. A x = b, x >= 0` (b >= 0)
    exactly over rationals with a two-phase simplex using Bland's rule.

    Returns `(value, x)` with `value` and every `x` a `Fraction`, or `None`
    when the LP is infeasible. Unboundedness cannot occur for the covering LPs
    this helper builds (all costs are 1 and a non-negative feasible solution
    always bounds the objective from below).
    """
    m = len(b)
    n = len(c)
    total = n + m
    rows = []
    for r in range(m):
        row = [Fraction(0)] * (total + 1)
        for j in range(n):
            row[j] = a[r][j]
        row[n + r] = Fraction(1)
        row[total] = b[r]
        rows.append(row)
    basis = [n + r for r in range(m)]

    red = [Fraction(0)] * (total + 1)
    for r in range(m):
        red[n + r] = Fraction(1)
    for r in range(m):
        for k in range(total + 1):
            red[k] -= rows[r][k]

    def pivot(rows, red, basis, e, l):
        pv = rows[l][e]
        for k in range(total + 1):
            rows[l][k] /= pv
        for rr in range(m):
            if rr != l and rows[rr] is not None:
                f = rows[rr][e]
                if f != 0:
                    for k in range(total + 1):
                        rows[rr][k] -= f * rows[l][k]
        fr = red[e]
        for k in range(total + 1):
            red[k] -= fr * rows[l][k]
        basis[l] = e

    def iterate(rows, red, basis):
        while True:
            e = -1
            for k in range(total):
                if red[k] < 0:
                    e = k
                    break
            if e < 0:
                return True
            best = None
            l = -1
            for rr in range(m):
                if rows[rr] is None:
                    continue
                ac = rows[rr][e]
                if ac > 0:
                    ratio = rows[rr][total] / ac
                    if best is None or ratio < best or (ratio == best and basis[rr] < basis[l]):
                        best = ratio
                        l = rr
            if l < 0:
                return False
            pivot(rows, red, basis, e, l)

    if not iterate(rows, red, basis):
        return None
    if red[total] != 0:
        return None

    for rr in range(m):
        if basis[rr] >= n:
            j = 0
            while j < n and rows[rr][j] == 0:
                j += 1
            if j < n:
                pivot(rows, red, basis, j, rr)
            else:
                rows[rr] = None

    live = [r for r in range(m) if rows[r] is not None]
    m2 = len(live)
    tab = [[rows[r][k] if k < n else rows[r][total] for k in range(n + 1)] for r in live]
    basis2 = [basis[r] for r in live]

    red2 = [Fraction(0)] * (n + 1)
    for j in range(n):
        red2[j] = c[j]
    for rr in range(m2):
        bi = basis2[rr]
        cb = c[bi] if 0 <= bi < n else Fraction(0)
        if cb != 0:
            for k in range(n + 1):
                red2[k] -= cb * tab[rr][k]

    def pivot2(e, l):
        pv = tab[l][e]
        for k in range(n + 1):
            tab[l][k] /= pv
        for rr in range(m2):
            if rr != l:
                f = tab[rr][e]
                if f != 0:
                    for k in range(n + 1):
                        tab[rr][k] -= f * tab[l][k]
        fr = red2[e]
        for k in range(n + 1):
            red2[k] -= fr * tab[l][k]
        basis2[l] = e

    while True:
        e = -1
        for k in range(n):
            if red2[k] < 0:
                e = k
                break
        if e < 0:
            break
        best = None
        l = -1
        for rr in range(m2):
            ac = tab[rr][e]
            if ac > 0:
                ratio = tab[rr][n] / ac
                if best is None or ratio < best or (ratio == best and basis2[rr] < basis2[l]):
                    best = ratio
                    l = rr
        if l < 0:
            return None
        pivot2(e, l)

    value = -red2[n]
    x = [Fraction(0)] * n
    for rr in range(m2):
        if 0 <= basis2[rr] < n:
            x[basis2[rr]] = tab[rr][n]
    return (value, x)


def solve_bounded(a, b, lb, ub):
    """Solve the LP relaxation of `min sum x : A x = b, x >= 0` with integer
    lower/upper bounds on every variable.

    Lower bounds shift the right-hand side; finite upper bounds become slack
    equations `x'_j + s_j = ub_j - lb_j`. Returns `(value, x)` where `x` is the
    solution in the original variables (Fractions), or `None` when infeasible.
    """
    m = len(b)
    n = len(lb)
    beq = list(b)
    for j in range(n):
        lo = lb[j]
        if lo != 0:
            for i in range(m):
                if a[i][j] != 0:
                    beq[i] -= lo * a[i][j]

    total = n + n
    neq = m + n
    big = [[Fraction(0)] * total for _ in range(neq)]
    rhs = [Fraction(0)] * neq
    for i in range(m):
        for j in range(n):
            big[i][j] = a[i][j]
        rhs[i] = beq[i]
    for u in range(n):
        big[m + u][u] = Fraction(1)
        big[m + u][n + u] = Fraction(1)
        rhs[m + u] = Fraction(ub[u]) - lb[u]

    c = [Fraction(1)] * n + [Fraction(0)] * n
    for v in rhs:
        if v < 0:
            return None

    res = simplex(big, rhs, c)
    if res is None:
        return None
    value, xs = res
    x = [xs[k] + lb[k] for k in range(n)]
    return (value + sum(lb), x)


def min_presses_bnb(buttons, joltages):
    """Fewest total presses for one machine's joltage counters via exact
    branch-and-bound over the rational LP relaxation (mirror of `Day10.part2`
    / `CounterSolver` in src/main/scala/aoc2025/Day10.scala).

    Branch on the first fractional variable of the relaxation: one child caps
    it at its floor, the other raises it to its ceiling. A branch is pruned
    when its lower bound meets the incumbent integer solution. Each button's
    upper bound is the smallest target among the counters it touches (pressing
    more would overshoot that counter). Returns the minimum total presses, or
    -1 when the targets are unreachable.
    """
    m = len(joltages)
    n = len(buttons)
    a = [[Fraction(1) if (buttons[j] >> i) & 1 else Fraction(0) for j in range(n)]
         for i in range(m)]
    b = [Fraction(v) for v in joltages]

    ub = []
    for j in range(n):
        lo = None
        for i in range(m):
            if (buttons[j] >> i) & 1:
                if lo is None or joltages[i] < lo:
                    lo = joltages[i]
        ub.append(lo if lo is not None else 0)

    best = float("inf")

    def at_least(value, bound):
        return value >= bound

    def node(lb_, hi_):
        nonlocal best
        res = solve_bounded(a, b, lb_, hi_)
        if res is None:
            return
        value, x = res
        if at_least(value, best):
            return
        frac = [k for k in range(n) if x[k].denominator != 1]
        if not frac:
            if int(value) < best:
                best = int(value)
        else:
            k = frac[0]
            z = x[k]
            flo = int(z.numerator // z.denominator)
            hi2 = hi_.copy()
            hi2[k] = flo
            node(lb_, hi2)
            lb2 = lb_.copy()
            lb2[k] = Fraction(flo + 1)
            node(lb2, hi_)

    node([Fraction(0)] * n, ub)
    return best if best != float("inf") else -1


def min_presses_brute(buttons, joltages):
    """Fewest total presses for one machine by exhaustive enumeration.

    Independent ground-truth reference: enumerate every press-count vector
    within the per-button upper bounds and keep the lightest exact solution.
    Only tractable for small instances (few buttons, small targets); used to
    validate the branch-and-bound mirror on generated machines.
    """
    m = len(joltages)
    n = len(buttons)
    ub = []
    for j in range(n):
        lo = None
        for i in range(m):
            if (buttons[j] >> i) & 1:
                if lo is None or joltages[i] < lo:
                    lo = joltages[i]
        ub.append(lo if lo is not None else 0)

    best = None

    def rec(k, cur, used):
        nonlocal best
        if best is not None and used >= best:
            return
        if k == n:
            if cur == joltages and (best is None or used < best):
                best = used
            return
        for x in range(0, ub[k] + 1):
            nxt = cur
            if x:
                nxt = list(cur)
                for i in range(m):
                    if (buttons[k] >> i) & 1:
                        nxt[i] += x
                nxt = tuple(nxt)
            rec(k + 1, nxt, used + x)

    rec(0, tuple([0] * m), 0)
    return best if best is not None else -1


def total_presses(machines, fn):
    """Sum of `fn` over every machine, mirroring `Day10.totalJoltagePresses`."""
    return sum(fn(b, j) for b, j in machines)


def self_test():
    """Deterministic validation: seeded random cross-checks (small and medium
    instances) + official sample (33) + the accepted Day 10 real-input answer
    (16765)."""
    rng = random.Random(20261012)

    cases = 1500
    for _ in range(cases):
        m = rng.randint(1, 3)
        n = rng.randint(1, 4)
        buttons = []
        for _ in range(n):
            mask = 0
            for i in range(m):
                if rng.random() < 0.5:
                    mask |= 1 << i
            buttons.append(mask)
        joltages = tuple(rng.randint(0, 5) for _ in range(m))
        a = min_presses_bnb(buttons, joltages)
        c = min_presses_brute(buttons, joltages)
        if a != c:
            print(f"FAIL: small buttons={buttons} joltages={joltages} "
                  f"-> bnb {a} != brute {c}", file=sys.stderr)
            return 1

    medium = 200
    for _ in range(medium):
        m = rng.randint(2, 4)
        n = rng.randint(3, 5)
        buttons = []
        for _ in range(n):
            mask = 0
            for i in range(m):
                if rng.random() < 0.5:
                    mask |= 1 << i
            buttons.append(mask)
        joltages = tuple(rng.randint(0, 7) for _ in range(m))
        a = min_presses_bnb(buttons, joltages)
        c = min_presses_brute(buttons, joltages)
        if a != c:
            print(f"FAIL: medium buttons={buttons} joltages={joltages} "
                  f"-> bnb {a} != brute {c}", file=sys.stderr)
            return 1

    with open(sample_input_path(), "r", encoding="utf-8") as fh:
        sample = parse_machines(fh.read())
    assert len(sample) == 3, f"sample should have 3 machines, got {len(sample)}"
    per_machine = [min_presses_bnb(b, j) for b, j in sample]
    if per_machine != [10, 12, 11]:
        print(f"FAIL: sample per-machine {per_machine} != [10, 12, 11]",
              file=sys.stderr)
        return 1
    if total_presses(sample, min_presses_bnb) != SAMPLE_ANSWER:
        print(f"FAIL: sample total != {SAMPLE_ANSWER}", file=sys.stderr)
        return 1

    with open(real_input_path(), "r", encoding="utf-8") as fh:
        real = parse_machines(fh.read())
    assert len(real) == 163, f"real input should have 163 machines, got {len(real)}"
    real_total = total_presses(real, min_presses_bnb)
    if real_total != REAL_ANSWER:
        print(f"FAIL: real {real_total} != {REAL_ANSWER}", file=sys.stderr)
        return 1

    print(f"ok: {cases} small + {medium} medium bnb/brute cross-checks, "
          f"sample == {SAMPLE_ANSWER}, real == {REAL_ANSWER}")
    return 0


def file_mode(path, expected):
    path = repo_input_path(path)
    try:
        with open(path, "r", encoding="utf-8") as fh:
            machines = parse_machines(fh.read())
    except (OSError, ValueError) as exc:
        print(f"error: cannot read {path}: {exc}", file=sys.stderr)
        return 1
    per_machine = [min_presses_bnb(b, j) for b, j in machines]
    total = sum(per_machine)
    print(f"machines={len(machines)} per_machine={per_machine} total={total}")
    if expected is not None:
        if total != expected:
            print(f"error: expected {expected}, got {total}", file=sys.stderr)
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
        print("usage: scripts/check-joltage-counters.py [FILE [EXPECTED]]",
              file=sys.stderr)
        return 2
    expected = int(args[1]) if len(args) >= 2 else None
    return file_mode(args[0], expected)


if __name__ == "__main__":
    sys.exit(main(sys.argv))