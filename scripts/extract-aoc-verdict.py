#!/usr/bin/env python3
"""extract-aoc-verdict.py - classify a saved AoC answer-response HTML file.

Deterministic reader for the response AoC serves after a level POST
(https://adventofcode.com/2025/day/<D>/answer) or for a puzzle-day page.
It never submits anything and never reads the network; it only parses a
local HTML file. Input and output are deterministic, so submit and review
stages can re-confirm the same verdict from the same artifact.

Usage:
  scripts/extract-aoc-verdict.py <verdict.html> [--verbose]

Prints one line per verdict field to stdout:
  verdict=<ACCEPTED|REJECTED_LOW|REJECTED_HIGH|REJECTED|RATE_LIMITED|ALREADY_COMPLETED|LOGIN_REQUIRED|UNKNOWN>
  wording=<the verdict message text, newlines collapsed>

Exit codes:
  0  a known verdict class was matched (including LOGIN_REQUIRED / RATE_LIMITED)
  1  unknown verdict (no known pattern found) or an unreadable file
"""

import html.parser
import re
import sys


class TextExtractor(html.parser.HTMLParser):
    """Collect visible text, dropping script/style blocks."""

    def __init__(self):
        super().__init__(convert_charrefs=True)
        self.parts = []
        self._skip_depth = 0
        self._skip_tags = {"script", "style", "svg", "head", "noscript"}

    def handle_starttag(self, tag, attrs):
        if tag in self._skip_tags:
            self._skip_depth += 1

    def handle_endtag(self, tag):
        if tag in self._skip_tags and self._skip_depth > 0:
            self._skip_depth -= 1

    def handle_data(self, data):
        if self._skip_depth == 0:
            self.parts.append(data)

    def text(self):
        collapsed = re.sub(r"[ \t\r\f\v]+", " ", "".join(self.parts))
        collapsed = re.sub(r"\n{2,}", "\n", collapsed)
        return collapsed.strip()


def classify(page_text):
    """Map a page's visible text to a verdict class and the verbatim wording.

    Order matters: 'That's the right answer' must be checked before any
    rejection phrasing, and a page recording the accepted answer
    ('Your puzzle answer was') is itself acceptance evidence.
    """
    patterns = [
        (r"That's the right answer", "ACCEPTED"),
        (r"Your puzzle answer was", "ACCEPTED"),
        (r"Your answer is too low", "REJECTED_LOW"),
        (r"Your answer is too high", "REJECTED_HIGH"),
        (r"not the right answer", "REJECTED"),
        (r"gave an answer too recently", "RATE_LIMITED"),
        (r"have to wait after submitting", "RATE_LIMITED"),
        (r"don't seem to be solving the right level", "ALREADY_COMPLETED"),
        (r"did you already complete it", "ALREADY_COMPLETED"),
        (r"log in", "LOGIN_REQUIRED"),
    ]
    for pattern, verdict in patterns:
        m = re.search(pattern, page_text, flags=re.IGNORECASE)
        if m:
            return verdict, _wording(page_text, m.start())
    return "UNKNOWN", _wording(page_text, 0)


def _wording(page_text, start):
    """Extract the sentence containing the verdict phrase as the wording."""
    if start < 0:
        start = 0
    end = page_text.find(".", start)
    if end == -1:
        end = len(page_text)
    sentence = page_text[start:end].strip()
    return sentence[:500]


def main(argv):
    if len(argv) < 2:
        print("usage: scripts/extract-aoc-verdict.py <verdict.html> [--verbose]", file=sys.stderr)
        return 2
    path = argv[1]
    verbose = "--verbose" in argv[2:]
    try:
        with open(path, "rb") as fh:
            raw = fh.read()
        extractor = TextExtractor()
        extractor.feed(raw.decode("utf-8", errors="replace"))
        page_text = extractor.text()
    except OSError as exc:
        print(f"error: cannot read {path}: {exc}", file=sys.stderr)
        return 1

    verdict, wording = classify(page_text)
    print(f"verdict={verdict}")
    print(f"wording={wording}")
    if verbose:
        print(f"file={path}")
    return 0 if verdict != "UNKNOWN" else 1


if __name__ == "__main__":
    sys.exit(main(sys.argv))