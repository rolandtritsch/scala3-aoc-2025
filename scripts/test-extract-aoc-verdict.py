#!/usr/bin/env python3
"""test-extract-aoc-verdict.py - deterministic validation for extract-aoc-verdict.

Re-runs scripts/extract-aoc-verdict.py against every fixture under
skills/extract-aoc-verdict/fixtures/ and asserts the expected verdict class and
exit code, so the skill's "validated on fixtures" claim stays machine-checked and
reproducible. Also exercises the CLI failure cases (missing file, missing arg)
and a compile check of the script.

Read-only on the repository: it never submits, never touches the network, and
never modifies tracked files.

Usage:
  scripts/test-extract-aoc-verdict.py

Exit codes:
  0  all fixture expectations and failure cases passed
  1  any expectation failed or the harness itself errored
"""

import subprocess
import sys
from pathlib import Path

REPO_ROOT = Path(__file__).resolve().parent.parent
SCRIPT = REPO_ROOT / "scripts" / "extract-aoc-verdict.py"
FIXTURES = REPO_ROOT / "skills" / "extract-aoc-verdict" / "fixtures"

# fixture filename -> expected (verdict class, exit code)
EXPECTED = {
    "accepted.html": ("ACCEPTED", 0),
    "already-completed.html": ("ALREADY_COMPLETED", 0),
    "login.html": ("LOGIN_REQUIRED", 0),
    "rate-limit.html": ("RATE_LIMITED", 0),
    "rejected.html": ("REJECTED", 0),
    "too-high.html": ("REJECTED_HIGH", 0),
    "too-low.html": ("REJECTED_LOW", 0),
    "unknown.html": ("UNKNOWN", 1),
    "recorded-answer.html": ("ACCEPTED", 0),
    "garbage.html": ("UNKNOWN", 1),
}


def run_script(*args):
    proc = subprocess.run(
        [sys.executable, str(SCRIPT), *args],
        capture_output=True,
        text=True,
    )
    return proc.returncode, proc.stdout, proc.stderr


def main():
    failures = []

    if not SCRIPT.exists():
        print(f"FAIL: missing script {SCRIPT}", file=sys.stderr)
        return 1

    # Compile check of the script under test.
    check = subprocess.run(
        [sys.executable, "-m", "py_compile", str(SCRIPT)],
        capture_output=True,
        text=True,
    )
    if check.returncode != 0:
        failures.append("py_compile of extract-aoc-verdict.py failed")

    if not FIXTURES.is_dir():
        print(f"FAIL: missing fixtures dir {FIXTURES}", file=sys.stderr)
        return 1

    # Every committed fixture must be in the expected table (no silent drift).
    committed = sorted(p.name for p in FIXTURES.glob("*.html"))
    for name in committed:
        if name not in EXPECTED:
            failures.append(f"fixture {name} has no expected class in the harness")

    for name, (want_class, want_code) in sorted(EXPECTED.items()):
        path = FIXTURES / name
        if not path.exists():
            failures.append(f"expected fixture missing: {name}")
            continue
        code, out, _ = run_script(str(path))
        got_class = ""
        for line in out.splitlines():
            if line.startswith("verdict="):
                got_class = line.split("=", 1)[1]
        if code != want_code or got_class != want_class:
            failures.append(
                f"{name}: expected verdict={want_class} exit={want_code}, "
                f"got verdict={got_class} exit={code}"
            )

    # CLI failure cases.
    code, _, err = run_script()
    if code != 2 or "usage:" not in err:
        failures.append(f"no-arg case: expected usage+exit 2, got exit {code}")

    code, _, _ = run_script(str(FIXTURES / "does-not-exist.html"))
    if code != 1:
        failures.append(f"missing-file case: expected exit 1, got exit {code}")

    if failures:
        print("extract-aoc-verdict validation FAILED:", file=sys.stderr)
        for f in failures:
            print(f"  - {f}", file=sys.stderr)
        return 1

    print(f"OK: {len(EXPECTED)} fixtures + 2 CLI failure cases passed")
    print(f"OK: extract-aoc-verdict.py compiles; deterministic and read-only")
    return 0


if __name__ == "__main__":
    sys.exit(main())