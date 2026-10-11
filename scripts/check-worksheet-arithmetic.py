#!/usr/bin/env python3
"""check-worksheet-arithmetic.py - validate and apply the columnar worksheet
arithmetic technique.

Deterministic, read-only helper distilled from the Day 06 review. The
technique: a worksheet lays problems out side by side in a wide horizontal
grid. Each problem's numbers sit one per line in a vertical strip, with the
operator (`+` or `*`) in the same strip on the bottom line. A full column of
spaces across every line separates neighbouring problems; the left/right
alignment of numbers within a strip is irrelevant. Each problem's value is its
numbers combined with its bottom operator, and the puzzle asks for the grand
total over all problems.

Two readings of the same worksheet are supported:

Part 1 (per-line tokens, left-to-right):
  Each number is the token on its own number line inside a strip. Two
  independent parsers are provided and cross-checked:

  1. Column-run parser (`parse_column_runs`): finds maximal runs of columns
     that are not blank across every line; the operator is the single non-blank
     character in the bottom line of the run and the numbers are the per-line
     tokens inside the run. Mirrors `Day06.readFile`/`Day06.evaluate`/`Day06.part1`
     in src/main/scala/aoc2025/Day06.scala.
  2. Interval-union parser (`parse_interval_runs`): forms the same problem strips
     by merging the occupied column intervals of every token across all lines
     (union of non-blank spans) rather than scanning columns for all-blank
     separators. This is an independent reference that must recover the exact
     same (operator, numbers) problem list.

Part 2 (per-column digit stacks, right-to-left):
  Each number is a single column of digits with its most significant digit at
  the top and least significant digit at the bottom (a leading blank column
  appears only above a shorter number). Columns of a strip are read
  right-to-left to form that problem's numbers, and problems themselves are
  emitted right-to-left. Mirrors `Day06.readFilePart2`/`Day06.part2`. Two
  independent parsers are cross-checked:

  1. Column-run parser (`parse_part2_column_runs`): scans columns right-to-left,
     grouping maximal runs of non-separator columns into problems exactly like
     the part-1 scan but reading each column's digit stack.
  2. Interval-union parser (`parse_part2_interval_runs`): forms the problem
     strips by merging the occupied column intervals of every token (same
     independent strip detection as part 1), then reads each strip's columns
     right-to-left. This is an independent reference that must recover the same
     problems.

Modes:
  no args / --self-test          Part-1 self-test: build random worksheets from
                                 known problems, render them to the statement
                                 layout, re-parse them with both parsers, and
                                 require both to recover the known grand total;
                                 then assert the official Day 06 sample (4277556)
                                 and the accepted Day 06 real-input answer
                                 (6378679666679).
  <FILE> [EXPECTED]              Part-1 file mode: read a worksheet, print the
                                 problem count and grand total, and assert the
                                 total when EXPECTED is given.
  --part2                        Part-2 self-test: same generation/re-parse
                                 cross-check on the column-stack layout, plus
                                 the official Day 06 part-2 sample (3263827)
                                 and accepted real-input answer (11494432585168).
  --part2 <FILE> [EXPECTED]      Part-2 file mode: read a worksheet the
                                 right-to-left column way, print the problem
                                 count and grand total, and assert the total
                                 when EXPECTED is given.

Usage:
  scripts/check-worksheet-arithmetic.py
  scripts/check-worksheet-arithmetic.py --self-test
  scripts/check-worksheet-arithmetic.py inputs/Day06.txt 6378679666679
  scripts/check-worksheet-arithmetic.py --part2
  scripts/check-worksheet-arithmetic.py --part2 inputs/Day06.txt 11494432585168

Exit codes:
  0  all checks passed
  1  a check failed or the input is malformed
  2  usage error
"""

import os
import random
import re
import sys

SAMPLE_TOTAL = 4277556
REAL_TOTAL = 6378679666679
SAMPLE_TOTAL_P2 = 3263827
REAL_TOTAL_P2 = 11494432585168
REAL_PROBLEMS = 1000
SELF_TEST_CASES = 1000


def real_input_path():
    """Path to the Day 06 real input, resolved relative to this script."""
    here = os.path.dirname(os.path.abspath(__file__))
    return os.path.join(here, "..", "src", "main", "resources", "inputs", "Day06.txt")


def sample_input_path():
    """Path to the official Day 06 sample worksheet."""
    here = os.path.dirname(os.path.abspath(__file__))
    return os.path.join(here, "..", "src", "main", "resources", "inputs", "Day06Test.txt")


def parse_column_runs(worksheet):
    """Parse a worksheet by locating maximal runs of non-separator columns.

    A column is a separator when every line is blank at that position. Each run
    of non-separator columns is one problem: the operator is the first non-blank
    character of the last line within the run, and the numbers are the non-blank
    tokens read from every earlier line within the run.
    """
    lines = worksheet.splitlines()
    if len(lines) < 2:
        raise ValueError("worksheet needs at least one number row and an operator row")
    width = max(len(line) for line in lines)
    lines = [line.ljust(width) for line in lines]
    number_rows = lines[:-1]
    operator_row = lines[-1]

    def is_separator(column):
        return all(line[column] == " " for line in lines)

    problems = []
    column = 0
    while column < width:
        if is_separator(column):
            column += 1
            continue
        start = column
        while column < width and not is_separator(column):
            column += 1
        end = column - 1

        operator_candidates = [ch for ch in operator_row[start:end + 1] if ch != " "]
        if len(operator_candidates) != 1 or operator_candidates[0] not in "+*":
            raise ValueError(f"problem in columns {start}..{end} has no single operator")
        operator = operator_candidates[0]

        numbers = []
        for row in number_rows:
            token = row[start:end + 1].strip()
            if token:
                numbers.append(int(token))
        problems.append((operator, numbers))
    return lines, problems


def _merge_intervals(intervals):
    """Merge sorted inclusive column intervals into disjoint spans."""
    spans = []
    for lo, hi in sorted(intervals):
        if spans and lo <= spans[-1][1] + 1:
            spans[-1] = (spans[-1][0], max(spans[-1][1], hi))
        else:
            spans.append((lo, hi))
    return spans


def parse_interval_runs(worksheet):
    """Independent reference parser: form problem strips by merging the occupied
    column intervals of every line, then assign tokens to strips.

    A strip is a maximal run of columns that contain at least one non-blank
    character anywhere, i.e. the union of the occupied column spans of all
    tokens across all lines. This yields the same boundaries as the separator
    scan in `parse_column_runs` but computes them by interval union instead of
    a per-column all-blank check. Each strip must contain exactly one operator
    token (bottom line) and exactly one number token per number row.
    """
    lines = worksheet.splitlines()
    if len(lines) < 2:
        raise ValueError("worksheet needs at least one number row and an operator row")
    number_rows = lines[:-1]
    operator_row = lines[-1]

    all_tokens = []  # (row_index, value/op, start, end)
    occupied = []
    for token in re.finditer(r"\S+", operator_row):
        all_tokens.append(("op", token.group(0), token.start(), token.end() - 1))
        occupied.append((token.start(), token.end() - 1))
    for row_index, row in enumerate(number_rows):
        for token in re.finditer(r"\S+", row):
            all_tokens.append(("num", int(token.group(0)), token.start(), token.end() - 1))
            occupied.append((token.start(), token.end() - 1))

    if not any(kind == "op" for kind, _, _, _ in all_tokens):
        raise ValueError("no operator found on the bottom line")

    strips = _merge_intervals(occupied)
    problems = []
    for strip_lo, strip_hi in strips:
        operators = [
            text for kind, text, start, end in all_tokens
            if kind == "op" and start >= strip_lo and end <= strip_hi
        ]
        if operators != ["+"] and operators != ["*"]:
            raise ValueError(f"strip {strip_lo}..{strip_hi} has operators {operators!r}")
        numbers = [
            value for kind, value, start, end in all_tokens
            if kind == "num" and start >= strip_lo and end <= strip_hi
        ]
        problems.append((operators[0], numbers))

    numbers_per_row = len(number_rows)
    for (operator, numbers) in problems:
        if len(numbers) != numbers_per_row:
            raise ValueError(
                f"problem with operator {operator!r} has {len(numbers)} numbers, "
                f"expected {numbers_per_row} (one per number row)"
            )
    return strips, problems


def _padded_lines(worksheet):
    """Right-pad the worksheet lines to a common width; at least one number row
    plus an operator row is required."""
    lines = worksheet.splitlines()
    if len(lines) < 2:
        raise ValueError("worksheet needs at least one number row and an operator row")
    width = max(len(line) for line in lines)
    return [line.ljust(width) for line in lines], width


def parse_part2_column_runs(worksheet):
    """Part-2 parser: read each problem's columns right-to-left as digit stacks.

    Problems are the same maximal runs of non-separator columns as in part 1,
    but this scan walks columns right-to-left, emitting problems in reverse
    order. Within a problem, each column's number is the top-to-bottom run of
    non-blank characters over the number rows (most significant digit at top,
    least significant digit at bottom); leading blanks occur only above a
    shorter number. Mirrors `Day06.readFilePart2` in
    src/main/scala/aoc2025/Day06.scala.
    """
    lines, width = _padded_lines(worksheet)
    number_rows = lines[:-1]
    operator_row = lines[-1]

    def is_separator(column):
        return all(line[column] == " " for line in lines)

    problems = []
    column = width - 1
    while column >= 0:
        if is_separator(column):
            column -= 1
            continue
        end = column
        while column >= 0 and not is_separator(column):
            column -= 1
        start = column + 1

        operator_candidates = [ch for ch in operator_row[start:end + 1] if ch != " "]
        if len(operator_candidates) != 1 or operator_candidates[0] not in "+*":
            raise ValueError(f"problem in columns {start}..{end} has no single operator")
        operator = operator_candidates[0]

        numbers = []
        for col in range(end, start - 1, -1):
            digits = "".join(row[col] for row in number_rows if row[col] != " ")
            if not digits:
                raise ValueError(
                    f"problem in columns {start}..{end} has an empty number column {col}"
                )
            numbers.append(int(digits))
        problems.append((operator, numbers))
    return lines, problems


def parse_part2_interval_runs(worksheet):
    """Independent part-2 reference: form strips by interval union, then read
    each strip's columns right-to-left as digit stacks.

    The strip boundaries are computed by merging the occupied column intervals
    of every token (the same independent interval-union strip detection used for
    part 1), not by scanning for all-blank separators. Problems are emitted
    right-to-left to match `parse_part2_column_runs`.
    """
    lines = worksheet.splitlines()
    if len(lines) < 2:
        raise ValueError("worksheet needs at least one number row and an operator row")
    width = max(len(line) for line in lines)
    lines = [line.ljust(width) for line in lines]
    number_rows = lines[:-1]
    operator_row = lines[-1]

    occupied = []
    for row in lines:
        for token in re.finditer(r"\S+", row):
            occupied.append((token.start(), token.end() - 1))
    if not any(ch != " " for ch in operator_row):
        raise ValueError("no operator found on the bottom line")

    strips = _merge_intervals(occupied)
    problems = []
    # Emit strips right-to-left so problem order matches the column-run scan.
    for strip_lo, strip_hi in reversed(strips):
        operator_candidates = [
            ch for ch in operator_row[strip_lo:strip_hi + 1] if ch != " "
        ]
        if len(operator_candidates) != 1 or operator_candidates[0] not in "+*":
            raise ValueError(f"strip {strip_lo}..{strip_hi} has operators {operator_candidates!r}")
        operator = operator_candidates[0]

        numbers = []
        for col in range(strip_hi, strip_lo - 1, -1):
            digits = "".join(row[col] for row in number_rows if row[col] != " ")
            if not digits:
                raise ValueError(
                    f"strip {strip_lo}..{strip_hi} has an empty number column {col}"
                )
            numbers.append(int(digits))
        problems.append((operator, numbers))
    return strips, problems


def evaluate(problems):
    """Grand total of all problems: each combined with its bottom operator.

    Mirrors `Day06.evaluate`/`Day06.part1` in src/main/scala/aoc2025/Day06.scala.
    """
    total = 0
    for operator, numbers in problems:
        if operator == "*":
            value = 1
            for number in numbers:
                value *= number
        else:
            value = 0
            for number in numbers:
                value += number
        total += value
    return total


def _valid_strip(rendered):
    """A strip is valid when no interior column is blank across all its lines
    (otherwise it would look like a separator and split one visual problem)."""
    width = max(len(line) for line in rendered)
    padded = [line.ljust(width) for line in rendered]
    return all(
        any(row[i] != " " for row in padded)
        for i in range(width)
    )


def render_worksheet(problems, rng):
    """Render known problems to a worksheet matching the statement layout.

    The worksheet format requires every problem to have the same number of
    numbers (the bottom line is the operator row for all problems, one operator
    char per column strip), so `problems` must be uniform. Each problem is a
    vertical strip whose numbers and operator characters get random per-line
    offsets (alignment within a strip is irrelevant per the statement, so long
    as no interior column is blank across every line). Problems are separated by
    at least one fully-blank column so separator detection is well defined.
    Strips whose random offsets would create an interior all-blank column are
    re-rolled so the generated worksheet is always a valid instance.
    """
    specs = [(op, [str(n) for n in nums]) for op, nums in problems]
    counts = {len(nums) for _, nums in specs}
    if len(counts) != 1:
        raise ValueError("worksheet problems must all have the same number count")
    rows = len(specs[0][1]) + 1

    rendered_rows = [[] for _ in range(rows)]

    for operator, numbers in specs:
        texts = numbers + [operator]
        block_width = max(len(t) for t in texts)
        for _ in range(32):
            line_offsets = [rng.randint(0, block_width - len(t)) for t in texts]
            rendered = [
                " " * offset + text
                for offset, text in zip(line_offsets, texts)
            ]
            if _valid_strip(rendered):
                break
        else:
            # Extremely unlikely (a 1-wide block with a 1-char token). Fall back
            # to a left-aligned full-width strip, which is always valid.
            rendered = [text.ljust(block_width) for text in texts]
        rendered = [line.ljust(block_width) for line in rendered]

        for row_index in range(rows):
            rendered_rows[row_index].append(rendered[row_index])
        gap = 1 + rng.randint(0, 3)
        for row_index in range(rows):
            rendered_rows[row_index][-1] = rendered_rows[row_index][-1] + " " * gap

    return [''.join(row).rstrip() for row in rendered_rows]


def self_test():
    """Deterministic validation: random generation/re-parse cross-checks, the
    official sample, and the accepted Day 06 real-input answer."""
    rng = random.Random(20251011)

    total_tokens = 0
    for case in range(SELF_TEST_CASES):
        n_problems = rng.randint(1, 8)
        n_numbers = rng.randint(1, 5)  # uniform: the operator row is shared
        known = []
        for _ in range(n_problems):
            operator = "+" if rng.randint(0, 1) else "*"
            numbers = [rng.randint(1, 10 ** rng.randint(1, 6)) for _ in range(n_numbers)]
            known.append((operator, numbers))
        expected_total = evaluate(known)

        # Re-render two or three times with different random alignments so both
        # parse paths see several layouts; parse the last rendering.
        for _ in range(rng.randint(2, 3)):
            worksheet_lines = render_worksheet(known, rng)
        worksheet = "\n".join(worksheet_lines)

        _, problems_col = parse_column_runs(worksheet)
        _, problems_iv = parse_interval_runs(worksheet)

        if problems_col != known:
            print(f"FAIL: column-run parser recovered different problems "
                  f"(case {case})", file=sys.stderr)
            return 1
        if problems_iv != known:
            print(f"FAIL: interval-union parser recovered different problems "
                  f"(case {case})", file=sys.stderr)
            return 1
        if evaluate(problems_col) != expected_total or evaluate(problems_iv) != expected_total:
            print(f"FAIL: grand total mismatch (case {case})", file=sys.stderr)
            return 1
        total_tokens += sum(len(nums) for _, nums in known)

    with open(sample_input_path(), "r", encoding="utf-8") as fh:
        sample_lines, sample_problems = parse_column_runs(fh.read())
    if evaluate(sample_problems) != SAMPLE_TOTAL:
        print(f"FAIL: sample total != {SAMPLE_TOTAL}", file=sys.stderr)
        return 1
    _, sample_problems_iv = parse_interval_runs("\n".join(sample_lines))
    if sample_problems_iv != sample_problems:
        print("FAIL: sample parses differently between the two parsers", file=sys.stderr)
        return 1

    with open(real_input_path(), "r", encoding="utf-8") as fh:
        real_lines, real_problems = parse_column_runs(fh.read())
    if len(real_problems) != REAL_PROBLEMS:
        print(f"FAIL: real problem count {len(real_problems)} != {REAL_PROBLEMS}",
              file=sys.stderr)
        return 1
    if evaluate(real_problems) != REAL_TOTAL:
        print(f"FAIL: real total != {REAL_TOTAL}", file=sys.stderr)
        return 1
    _, real_problems_iv = parse_interval_runs("\n".join(real_lines))
    if real_problems_iv != real_problems:
        print("FAIL: real parses differently between the two parsers", file=sys.stderr)
        return 1

    print(f"ok: {SELF_TEST_CASES} generated worksheets re-parsed to known totals "
          f"({total_tokens} number tokens), sample total == {SAMPLE_TOTAL}, "
          f"real total == {REAL_TOTAL} ({REAL_PROBLEMS} problems)")
    return 0


def render_part2_worksheet(problems, rng):
    """Render known problems to a part-2 worksheet (numbers as digit columns).

    The part-2 layout shares the bottom operator line with part 1, so every
    problem's number rows have the same height: the longest digit run over all
    numbers in the worksheet. Each number is a single column of its digits,
    bottom-aligned (most significant digit at the top of the run, least
    significant digit at the bottom), with leading blanks above a shorter
    number. Each problem is a horizontal strip of one such column per number,
    with the operator character on the bottom line under the strip's leftmost
    column (matching the sample's `*  +  *  +` operator row). Problems are
    separated by at least one fully-blank column.
    """
    specs = [(op, [str(n) for n in nums]) for op, nums in problems]
    height = max(len(d) for _, nums in specs for d in nums)
    rows = height + 1  # number rows + operator row

    rendered_rows = [[] for _ in range(rows)]
    for operator, numbers in specs:
        columns = []
        for d in numbers:
            padded = " " * (height - len(d)) + d
            columns.append(list(padded))
        for row_index in range(rows - 1):
            rendered_rows[row_index].extend(
                columns[c][row_index] for c in range(len(columns))
            )
        operator_row = [" "] * len(columns)
        operator_row[0] = operator
        rendered_rows[rows - 1].extend(operator_row)
        gap = 1 + rng.randint(0, 3)
        for row_index in range(rows):
            rendered_rows[row_index].extend(" " * gap)

    return [''.join(row).rstrip() for row in rendered_rows]


def self_test_part2():
    """Deterministic validation of the right-to-left column-read reading: random
    generation/re-parse cross-checks, the official part-2 sample, and the
    accepted Day 06 part-2 real-input answer."""
    rng = random.Random(20251011)

    total_numbers = 0
    for case in range(SELF_TEST_CASES):
        n_problems = rng.randint(1, 8)
        known = []
        for _ in range(n_problems):
            operator = "+" if rng.randint(0, 1) else "*"
            numbers = [rng.randint(1, 10 ** rng.randint(1, 6)) for _ in range(rng.randint(1, 5))]
            known.append((operator, numbers))
        expected_total = evaluate(known)

        for _ in range(rng.randint(2, 3)):
            worksheet_lines = render_part2_worksheet(known, rng)
        worksheet = "\n".join(worksheet_lines)

        _, problems_col = parse_part2_column_runs(worksheet)
        _, problems_iv = parse_part2_interval_runs(worksheet)

        # The parsers emit problems right-to-left and each problem's numbers
        # right-to-left (within-strip columns), so a left-to-right render of
        # `known` is recovered reversed both outside and inside.
        expected = [
            (op, list(reversed(nums))) for op, nums in reversed(known)
        ]

        if problems_col != expected:
            print(f"FAIL: part2 column-run parser recovered different problems "
                  f"(case {case})", file=sys.stderr)
            return 1
        if problems_iv != expected:
            print(f"FAIL: part2 interval-union parser recovered different problems "
                  f"(case {case})", file=sys.stderr)
            return 1
        if evaluate(problems_col) != expected_total or evaluate(problems_iv) != expected_total:
            print(f"FAIL: part2 grand total mismatch (case {case})", file=sys.stderr)
            return 1
        total_numbers += sum(len(nums) for _, nums in known)

    with open(sample_input_path(), "r", encoding="utf-8") as fh:
        sample_lines, sample_problems = parse_part2_column_runs(fh.read())
    if evaluate(sample_problems) != SAMPLE_TOTAL_P2:
        print(f"FAIL: part2 sample total != {SAMPLE_TOTAL_P2}", file=sys.stderr)
        return 1
    _, sample_problems_iv = parse_part2_interval_runs("\n".join(sample_lines))
    if sample_problems_iv != sample_problems:
        print("FAIL: part2 sample parses differently between the two parsers",
              file=sys.stderr)
        return 1

    with open(real_input_path(), "r", encoding="utf-8") as fh:
        real_lines, real_problems = parse_part2_column_runs(fh.read())
    if len(real_problems) != REAL_PROBLEMS:
        print(f"FAIL: part2 real problem count {len(real_problems)} != {REAL_PROBLEMS}",
              file=sys.stderr)
        return 1
    if evaluate(real_problems) != REAL_TOTAL_P2:
        print(f"FAIL: part2 real total != {REAL_TOTAL_P2}", file=sys.stderr)
        return 1
    _, real_problems_iv = parse_part2_interval_runs("\n".join(real_lines))
    if real_problems_iv != real_problems:
        print("FAIL: part2 real parses differently between the two parsers",
              file=sys.stderr)
        return 1

    print(f"ok: part2: {SELF_TEST_CASES} generated worksheets re-parsed to known totals "
          f"({total_numbers} number columns), sample total == {SAMPLE_TOTAL_P2}, "
          f"real total == {REAL_TOTAL_P2} ({REAL_PROBLEMS} problems)")
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
        candidate = os.path.join(
            repo_root, "src", "main", "resources", path,
        )
        if os.path.exists(candidate):
            return candidate
    return path


def file_mode(path, expected, part2=False):
    parse_col = parse_part2_column_runs if part2 else parse_column_runs
    parse_iv = parse_part2_interval_runs if part2 else parse_interval_runs
    path = repo_input_path(path)
    try:
        with open(path, "r", encoding="utf-8") as fh:
            lines, problems = parse_col(fh.read())
    except (OSError, ValueError) as exc:
        print(f"error: cannot read {path}: {exc}", file=sys.stderr)
        return 1
    try:
        _, problems_iv = parse_iv("\n".join(lines))
    except ValueError as exc:
        print(f"error: {path}: {exc}", file=sys.stderr)
        return 1
    total = evaluate(problems)
    print(f"problems={len(problems)} total={total}")
    if problems_iv != problems:
        print("error: the two parsers disagree", file=sys.stderr)
        return 1
    if expected is not None:
        if total != expected:
            print(f"error: expected {expected}, got {total}", file=sys.stderr)
            return 1
        print(f"ok: total == {expected}")
    return 0


def main(argv):
    args = [a for a in argv[1:] if a != "--self-test"]
    part2 = "--part2" in args
    args = [a for a in args if a != "--part2"]
    if not args:
        return self_test_part2() if part2 else self_test()
    if len(args) not in (1, 2):
        print("usage: scripts/check-worksheet-arithmetic.py [--part2] [FILE [EXPECTED]]",
              file=sys.stderr)
        return 2
    path = args[0]
    expected = int(args[1]) if len(args) >= 2 else None
    return file_mode(path, expected, part2=part2)


if __name__ == "__main__":
    sys.exit(main(sys.argv))