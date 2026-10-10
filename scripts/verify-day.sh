#!/usr/bin/env bash
#
# verify-day.sh — verify a committed AoC day solution in the repo.
#
# Deterministic local re-check of the three things the AoC cycle relies on
# before a submit/review disposition:
#   1. git sync state (HEAD == origin/main, working tree clean)
#   2. the day-specific munit suite passes (sample + boundaries + real input)
#   3. `sbt run` prints the expected part-1 (and optionally part-2) answer(s)
#
# Usage:
#   scripts/verify-day.sh <NN> [EXPECTED_PART1] [EXPECTED_PART2]
#     NN              zero-padded day number, e.g. 01 (also works for 00)
#     EXPECTED_PART1  optional part-1 answer the run must print, e.g. 1129
#     EXPECTED_PART2  optional part-2 answer the run must print, e.g. 6638
#
# The part-1 line is always required from `sbt run`. The part-2 line is
# required only when EXPECTED_PART2 is given (so pre-part-2 days still verify).
#
# Exit codes:
#   0  all checks passed
#   1  any check failed (missing args, unsynced/ dirty tree, test failure,
#      missing/unexpected run output)
#
# This script is read-only on the repository: it never pushes, submits, or
# modifies tracked files. It is meant to be run from the repo root; it derives
# the repo root from its own location so it also works from scripts/.
set -euo pipefail

if [ "$#" -lt 1 ]; then
  echo "usage: scripts/verify-day.sh <NN> [EXPECTED_PART1] [EXPECTED_PART2]" >&2
  exit 1
fi

DAY="$1"
EXPECTED1="${2:-}"
EXPECTED2="${3:-}"
REPO_ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"

if [[ ! "$DAY" =~ ^[0-9]{2}$ ]]; then
  echo "error: day must be zero-padded (e.g. 01), got '$DAY'" >&2
  exit 1
fi

cd "$REPO_ROOT"

fail() {
  echo "FAIL: $*" >&2
  exit 1
}

echo "== repo: $REPO_ROOT =="

echo "-- git sync state --"
HEAD_SHA="$(git rev-parse HEAD)"
ORIGIN_SHA="$(git rev-parse origin/main 2>/dev/null || true)"
if [ -z "$ORIGIN_SHA" ]; then
  fail "no local ref origin/main; run from a synchronized checkout"
fi
if [ "$HEAD_SHA" != "$ORIGIN_SHA" ]; then
  fail "HEAD ($HEAD_SHA) != origin/main ($ORIGIN_SHA)"
fi
if [ -n "$(git status --porcelain)" ]; then
  fail "working tree is not clean"
fi
echo "ok: HEAD == origin/main == $HEAD_SHA, clean tree"

echo "-- sbt testOnly aoc2025.Day${DAY}Test --"
sbt -batch "testOnly aoc2025.Day${DAY}Test"

echo "-- sbt run -> Day${DAY} part1/part2 lines --"
RUN_OUT="$(sbt -batch run)"
PART1_LINE="$(printf '%s\n' "$RUN_OUT" | grep -E "^Day${DAY} - part1: " | tail -n 1 || true)"
if [ -z "$PART1_LINE" ]; then
  fail "sbt run printed no 'Day${DAY} - part1:' line"
fi
echo "$PART1_LINE"

if [ -n "$EXPECTED1" ]; then
  ACTUAL1="$(printf '%s' "$PART1_LINE" | sed -E 's/^Day[0-9]{2} - part1: (.*)$/\1/')"
  if [ "$ACTUAL1" != "$EXPECTED1" ]; then
    fail "expected part-1 '$EXPECTED1', got '$ACTUAL1'"
  fi
  echo "ok: part1 == $EXPECTED1"
fi

if [ -n "$EXPECTED2" ]; then
  PART2_LINE="$(printf '%s\n' "$RUN_OUT" | grep -E "^Day${DAY} - part2: " | tail -n 1 || true)"
  if [ -z "$PART2_LINE" ]; then
    fail "expected part-2 '$EXPECTED2' but sbt run printed no 'Day${DAY} - part2:' line"
  fi
  echo "$PART2_LINE"
  ACTUAL2="$(printf '%s' "$PART2_LINE" | sed -E 's/^Day[0-9]{2} - part2: (.*)$/\1/')"
  if [ "$ACTUAL2" != "$EXPECTED2" ]; then
    fail "expected part-2 '$EXPECTED2', got '$ACTUAL2'"
  fi
  echo "ok: part2 == $EXPECTED2"
fi

echo "ALL CHECKS PASSED for Day ${DAY}"