---
name: largest-k-subsequence
description: Solve "keep exactly k items from a sequence, in order, to maximize the resulting number/value" problems. Use when a puzzle asks for the largest number formed by selecting a fixed count of digits/characters from each line without rearranging them (e.g. Day 03 part 2's "turn on exactly twelve batteries", or "remove n digits so the remaining number is largest"). Provides a validated greedy, a deterministic cross-check script, and the Day 03 evidence.
---

# Skill: largest-k-subsequence

Distilled from the Day 03 part 2 review (AOC-111, reviewed commit 33980c5,
accepted answer 170997883706617). The puzzle: within each 100-digit bank, turn
on exactly twelve batteries and the bank produces the twelve-digit number
formed by the chosen digits in their original order; maximize each bank and sum.

Generalized, the task is: given a sequence (here a digit string), keep exactly
`k` characters **in their original order** so the resulting value is maximal —
the lexicographically largest length-`k` subsequence. Equivalent to dropping
exactly `n - k` digits to maximize the remaining number. This is a recurring
AoC pattern (and the well-known "remove k digits to make the largest number").

## When to use

- A puzzle says "turn on exactly K batteries/digits" and the value is the
  number formed by their original order (Day 03 part 2).
- A puzzle says "remove N digits so the resulting number is as large as
  possible", or "select K items in order to maximize the result".
- Confirming or cross-checking a solution to such a problem before submit or
  review disposition.

## Algorithm (greedy monotonic stack)

Scan left to right, keeping digits on a stack. Before pushing each incoming
digit, while a removal is still available (`remove > 0`) and the stack's top is
strictly smaller than the incoming digit, pop (remove) the top — replacing a
smaller leading digit with a larger one always yields a larger number for a
fixed length. Leftover removals are dropped from the end (least significant).

Reference implementation: `src/main/scala/aoc2025/Day03.scala` `maxJoltage12`
(O(n) time, O(n) space), summed by `part2`. Uses strict `<` so equal digits are
kept in position (they do not change the value, and keeping them minimizes
reordering).

## Cross-check / validation

Deterministic, read-only helper: `scripts/check-largest-k-subsequence.py`.

- `python3 scripts/check-largest-k-subsequence.py` (or `--self-test`) — seeded
  (20251010) random cross-check: 2000 exhaustive `itertools.combinations`
  brute-force cases plus 500 long-bank cases cross-checked against an
  independent sliding-window algorithm, then asserts the Day 03 part 2 sample
  total `3121910778619`. Exit 0 = all pass.
- `python3 scripts/check-largest-k-subsequence.py <FILE> [K] [EXPECTED]` —
  reads one bank per non-blank line, sums the largest K-subsequence of each,
  and asserts `EXPECTED` when given. Example that re-derives the accepted
  Day 03 answer: `python3 scripts/check-largest-k-subsequence.py
  src/main/resources/inputs/Day03.txt 12 170997883706617` → exit 0.
- Failure cases (all exit 1): missing file, non-digit bank, `k > len(bank)`,
  wrong `EXPECTED`, unknown flag.

Never submit or push from this helper; it is read-only and local.

## Edge cases to cover in tests

- `k == n`: no removal — the whole sequence is the answer.
- Descending input: the largest prefix of length `k` is kept
  (`987654321111111` → `987654321111`).
- A large digit late in the sequence replaces smaller leading digits
  (`1111111111119` → `111111111119`).
- All-equal digits: value is `k` copies of that digit regardless of removal
  choices.

## Stopping conditions

- Stop after the cross-check/validation result is recorded. Do not create
  issues, retry submissions, or push from this skill.
- If the cross-check disagrees with the committed solution, record the exact
  mismatch as an unresolved repair for the solver on a later CTO cycle.