#!/usr/bin/env bash
#
# verify-day.sh — verify a committed AoC day part-1 solution in the repo.
#
# Deterministic local re-check of the three things the AoC cycle relies on
# before a submit/review disposition:
#   1. git sync state (HEAD == origin/main, working tree clean)
#   2. the day-specific munit suite passes (sample + boundaries + real input)
#   3. `sbt run` prints the expected part-1 answer (when EXPECTED is given)
#
# Usage:
#   scripts/verify-day.sh <NN> [EXPECTED_PART1]
#     NN            zero-padded day number, e.g. 01 (also works for 00)
#     EXPECTED_PART1  optional part-1 answer the run must print, e.g. 1129
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
  echo "usage: scripts/verify-day.sh <NN> [EXPECTED_PART1]" >&2
  exit 1
fi

DAY="$1"
EXPECTED="${2:-}"
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

echo "-- sbt run -> Day${DAY} part1 line --"
RUN_LINE="$(sbt -batch run | grep -E "^Day${DAY} - part1: " | tail -n 1 || true)"
if [ -z "$RUN_LINE" ]; then
  fail "sbt run printed no 'Day${DAY} - part1:' line"
fi
echo "$RUN_LINE"

if [ -n "$EXPECTED" ]; then
  ACTUAL="$(printf '%s' "$RUN_LINE" | sed -E 's/^Day[0-9]{2} - part1: (.*)$/\1/')"
  if [ "$ACTUAL" != "$EXPECTED" ]; then
    fail "expected part-1 '$EXPECTED', got '$ACTUAL'"
  fi
  echo "ok: part1 == $EXPECTED"
fi

echo "ALL CHECKS PASSED for Day ${DAY}"