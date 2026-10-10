---
name: extract-aoc-verdict
description: Classify a saved AoC answer-response HTML file into a deterministic verdict (ACCEPTED, REJECTED_LOW, REJECTED_HIGH, REJECTED, RATE_LIMITED, ALREADY_COMPLETED, LOGIN_REQUIRED, UNKNOWN) with its verbatim wording. Use when the submitter or reviewer must re-confirm what the AoC server actually returned from a saved response page without re-submitting or trusting a paraphrase.
---

# Skill: extract-aoc-verdict

Part of the AoC 2025 cycle in this repository. The submit and review stages
must judge authoritative AoC responses, but the server's answer is an HTML
page that should be judged from its verbatim text, not from a paraphrase.
`scripts/extract-aoc-verdict.py` turns a saved response page into a
deterministic verdict line that submit and review can quote identically.

## When to use

- The submitter saved the AoC POST response (`/tmp/aoc-verdict.html` or an
  issue artifact) and needs a machine-checked classification plus wording.
- The reviewer wants to re-confirm the submitter's verdict from the saved
  artifact before recording a review disposition.
- Any reconciliation where a saved page must be judged as accepted, rejected
  (low/high), rate-limited, already-completed, login-required, or unknown.

## Procedure

1. Locate the saved response HTML file (submitter run scratch, issue artifact,
   or a fixture under this skill's `fixtures/`).
2. Classify it deterministically:

   ```bash
   scripts/extract-aoc-verdict.py <verdict.html>
   ```

   Output is two lines:
   - `verdict=<CLASS>` with `ACCEPTED`, `REJECTED_LOW`, `REJECTED_HIGH`,
     `REJECTED`, `RATE_LIMITED`, `ALREADY_COMPLETED`, `LOGIN_REQUIRED`, or
     `UNKNOWN`.
   - `wording=<sentence containing the verdict phrase>` (verbatim, trimmed).
3. Read the classification. Exit code 0 means a known class matched;
   exit 1 means `UNKNOWN` or an unreadable file; exit 2 means bad usage.
4. Record the exact `verdict=` and `wording=` on the issue. Treat
   `ALREADY_COMPLETED` and `LOGIN_REQUIRED` as requiring reconciliation, not
   as acceptance or rejection. `UNKNOWN` means the markup or wording changed;
   do not assume.

## Evidence and validation

- Deterministic: the same file always yields the same verdict line. No
  network, no submission, read-only on the repository.
- Fixtures under `fixtures/` cover accepted, rejected, too-high, too-low,
  rate-limit, already-completed, login, and unknown pages; each produces the
  expected class (unknown exits 1). Missing-file and no-arg cases also exit
  non-zero with a message.
- Validate any real saved page through this script before quoting its verdict.

## Stopping conditions

- Stop after the verdict line is recorded; do not re-submit, retry, create
  issues, or push from this skill.
- For a rejection review, record the unresolved repair and leave the puzzle
  repair to the ProblemSolver on a later CTO cycle.