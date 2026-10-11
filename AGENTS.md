# CLAUDE.md

This file provides guidance to Claude Code when working with this repository. For basic usage information, see [README.md][]. For contribution guidelines, see [CONTRIBUTING.md][].

Based on the [scala3-aoc-2024][] blueprint (which used Mill; this repo uses sbt).

## Project-local skills

Read applicable `skills/*/SKILL.md` before repeating work. Reusable solution
scripts belong in `scripts/`; reviewed skill/script improvements are validated
and committed in this repository. Company monitoring tooling belongs to the
company's `aoc-watchdog` skill.

### Classify a saved AoC verdict page

Before quoting a server verdict (accepted/rejected/rate-limited), classify the
saved response HTML deterministically instead of paraphrasing it:

```bash
scripts/extract-aoc-verdict.py <verdict.html>   # prints verdict=<CLASS> + wording=<...>
```

Returns `ACCEPTED`, `REJECTED_LOW`, `REJECTED_HIGH`, `REJECTED`, `RATE_LIMITED`,
`ALREADY_COMPLETED`, `LOGIN_REQUIRED`, `SEASON_COMPLETE`, or `UNKNOWN`
(unknown exits 1). `SEASON_COMPLETE` recognizes the noncomputational finale
response ("You've finished every puzzle in Advent of Code") and is
authoritative season-completion evidence. Read-only; never submits. See
`skills/extract-aoc-verdict/SKILL.md`.

After editing the script or its fixtures, re-run the deterministic validation
harness (all fixtures + CLI failure cases, exit 0 on success):

```bash
scripts/test-extract-aoc-verdict.py
```

### Largest length-k subsequence (max value, keep order)

For puzzles that keep exactly `k` digits/characters from each line in their
original order to maximize the resulting number (e.g. Day 03 part 2 "turn on
exactly twelve batteries"), use the greedy monotonic-stack technique in
`src/main/scala/aoc2025/Day03.scala` (`maxJoltage12`). Cross-check any such
solution deterministically:

```bash
python3 scripts/check-largest-k-subsequence.py                 # self-test (brute-force + sample)
python3 scripts/check-largest-k-subsequence.py inputs/Day03.txt 12 170997883706617
```

Read-only; never submits. See `skills/largest-k-subsequence/SKILL.md`.

### Accessible rolls / occupancy-threshold grid peeling

For puzzles that count cells in a grid whose eight king-move neighbours contain
fewer than `K` occupied cells (e.g. Day 04 part 1 "rolls of paper a forklift can
access"), and/or peel the grid by repeatedly removing every currently-accessible
cell until none remain (Day 04 part 2 cascade), cross-check deterministically:

```bash
python3 scripts/check-accessible-rolls.py                            # self-test (random + sample 13/43 + real 1356/8713)
python3 scripts/check-accessible-rolls.py inputs/Day04.txt 1356 8713
```

Read-only; never submits. The self-test asserts the accepted Day 04 real-input
answers (count 1356, peel 8713). See `skills/accessible-rolls/SKILL.md`.

### Columnar worksheet arithmetic (numbers stacked under a bottom operator)

For puzzles that lay math problems side by side in a wide horizontal grid where
each problem's numbers sit one per line in a vertical strip with the operator
(`+` or `*`) at the bottom of the strip, problems separated by a full column of
spaces (e.g. Day 06 "trash compactor"), cross-check deterministically. Two
readings are supported: part 1 reads each number as the token on its own number
line left-to-right; part 2 reads each number as a single column of stacked
digits (most significant at top, least significant at bottom), columns
right-to-left within a problem and problems emitted right-to-left:

```bash
python3 scripts/check-worksheet-arithmetic.py                         # part-1 self-test (generated + sample 4277556 + real 6378679666679)
python3 scripts/check-worksheet-arithmetic.py inputs/Day06.txt 6378679666679
python3 scripts/check-worksheet-arithmetic.py --part2                 # part-2 self-test (generated + sample 3263827 + real 11494432585168)
python3 scripts/check-worksheet-arithmetic.py --part2 inputs/Day06.txt 11494432585168
```

Read-only; never submits. The self-tests assert the accepted Day 06 real-input
answers (6378679666679 part 1 and 11494432585168 part 2, 1000 problems each,
grand total of per-problem folds) and cross-check two independent parsers per
reading against generated worksheets. See
`skills/worksheet-arithmetic/SKILL.md`.

### Inclusive range coverage (count IDs in any range)

For puzzles that count how many of a set of item IDs fall inside at least one
inclusive `first-last` range, where ranges may overlap (e.g. Day 05 part 1
"cafeteria fresh ingredient IDs"), cross-check deterministically:

```bash
python3 scripts/check-range-coverage.py                            # self-test (random + sample 3 + real 848)
python3 scripts/check-range-coverage.py inputs/Day05.txt 848
```

For the companion union-count shape — how many IDs the ranges themselves cover
in total, i.e. the merged inclusive-union count, ignoring any available-ID list
(e.g. Day 05 part 2) — add `--union`:

```bash
python3 scripts/check-range-coverage.py --union                     # self-test (random + sample 14 + real 334714395325710)
python3 scripts/check-range-coverage.py --union inputs/Day05.txt 334714395325710
```

Read-only; never submits. The self-tests assert the accepted Day 05 real-input
answers 848 (covered count, 182 ranges vs 1000 IDs) and 334714395325710 (union
count, 83 merged spans) and cross-check a direct scan against a merged-interval
reference. See `skills/range-coverage/SKILL.md`.

### Vertical beam splitting (single downward ray that splits at `^` markers)

For puzzles where a beam enters a grid at `S` on the top row and moves only
downward, passes through `.`, is stopped by a `^` splitter and re-emitted into
the cells immediately left and right of it, with beams that merge into the same
cell deduplicated (e.g. Day 07 part 1 "tachyon manifold"), cross-check
deterministically:

```bash
python3 scripts/check-beam-splits.py                         # self-test (random + sample 21 + real 1605)
python3 scripts/check-beam-splits.py inputs/Day07.txt 1605
```

Read-only; never submits. The self-test cross-checks two independent
implementations (row-by-row active-column set mirroring `Day07.part1`, and a
BFS over occupied cells that visits each merged cell once) on 2000 generated
grids and asserts the accepted Day 07 real-input answer 1605 and the official
sample 21. See `skills/beam-splits/SKILL.md`.

For the many-worlds variant (Day 07 part 2) — a single particle takes BOTH
paths at every splitter and distinct timelines are counted WITHOUT deduplicating
convergent paths (sample 40, real 29893386035180) — add `--many-worlds`:

```bash
python3 scripts/check-beam-splits.py --many-worlds           # self-test (random + sample 40 + real 29893386035180)
python3 scripts/check-beam-splits.py --many-worlds inputs/Day07.txt 29893386035180
```

Read-only; never submits. The `--many-worlds` self-test cross-checks two
independent implementations (row-by-row per-column timeline multiplicities
mirroring `Day07.part2`, and a recursive memoized path-counting reference) on
2000 generated grids and asserts the accepted Day 07 real-input answer
29893386035180 and the official sample 40. See `skills/beam-splits/SKILL.md`.

### Closest-pairs single-linkage clustering (connect K closest pairs, product of 3 largest circuits)

For puzzles that connect the `K` closest pairs of points by straight-line
distance and multiply the sizes of the three largest resulting circuits, where
a pair already in the same circuit does nothing (e.g. Day 08 part 1 "Playground",
junction boxes as `X,Y,Z` triples), cross-check deterministically:

```bash
python3 scripts/check-closest-pairs.py                            # self-test (random + sample 40 + real 90036)
python3 scripts/check-closest-pairs.py inputs/Day08Test.txt 10 40
python3 scripts/check-closest-pairs.py inputs/Day08.txt 1000 90036
```

Read-only; never submits. This is effectively Kruskal's algorithm over the
sorted all-pairs edge list stopping after `K` edges; the self-test cross-checks
two independent implementations (array-based union-find mirroring
`Day08.part1`, and a small-to-large set-merging reference) on 2000 generated
point sets and asserts the accepted Day 08 real-input answer 90036 and the
official sample 40. The `inputs/...` path form resolves against
`src/main/resources/inputs` from anywhere in the repo. See
`skills/closest-pairs-clustering/SKILL.md`.

For the full-merge variant (Day 08 part 2) — keep connecting the closest
unconnected pairs until all boxes form a single circuit and multiply the X
coordinates of the last two boxes connected (sample 25272, real 6083499488) —
add `--full-merge`:

```bash
python3 scripts/check-closest-pairs.py --full-merge                # self-test (random + sample 25272 + real 6083499488)
python3 scripts/check-closest-pairs.py --full-merge inputs/Day08Test.txt 25272
python3 scripts/check-closest-pairs.py --full-merge inputs/Day08.txt 6083499488
```

Read-only; never submits. The `--full-merge` self-test cross-checks the
part-2 union-find mirror (mirroring `Day08.part2`) and the set-merging
reference on 2000 generated point sets and asserts the accepted Day 08
real-input answer 6083499488 and the official sample 25272. See
`skills/closest-pairs-clustering/SKILL.md`.

### Largest axis-aligned rectangle between two red-tile diagonal corners (inclusive spans)

For puzzles that find the largest axis-aligned rectangle whose opposite
(diagonal) corners are two of a set of marked grid tiles, where the rectangle
edges run along grid lines so the width/height are the inclusive spans
`(|dx|+1)`/`(|dy|+1)` and the area is their product (e.g. Day 09 part 1 "Movie
Theater"), cross-check deterministically:

```bash
python3 scripts/check-red-rectangles.py                    # self-test (random + sample 50 + real 4777967538)
python3 scripts/check-red-rectangles.py inputs/Day09Test.txt 50
python3 scripts/check-red-rectangles.py inputs/Day09.txt 4777967538
```

Read-only; never submits. The self-test cross-checks two independent
implementations (an index-based pair scan mirroring `Day09.part1` with
`abs(...) + 1` spans, and a functional `combinations` reference with
`max - min + 1` spans) on 5000 generated tile sets and asserts the accepted
Day 09 real-input answer 4777967538 and the official sample 50 (best pair
(11,1)-(2,5)); file mode also prints the best corner pair (real input:
(83050,86053)-(14405,16451)). The `inputs/...` path form resolves against
`src/main/resources/inputs` from anywhere in the repo. See
`skills/red-rectangles/SKILL.md`.

For the region-restricted variant (Day 09 part 2) — the rectangle may only use
red or green tiles, where every red tile connects to the tile before/after it
(the list wraps) by a straight green segment, so the red tiles are the vertices
of a closed rectilinear loop and every tile inside it is also green; a pair is
valid only when its whole cell span lies inside-or-on the loop (sample 24, real
1439894345) — add `--region`:

```bash
python3 scripts/check-red-rectangles.py --region           # self-test (random + sample 24 + real 1439894345)
python3 scripts/check-red-rectangles.py --region inputs/Day09Test.txt 24
python3 scripts/check-red-rectangles.py --region inputs/Day09.txt 1439894345
```

Read-only; never submits. The `--region` self-test cross-checks two independent
implementations (a coordinate-compressed 2D prefix-sum mirror of `Day09.part2`
and a direct per-cell containment reference) on 3000 generated simple
rectilinear loops and asserts the accepted Day 09 real-input answer 1439894345,
the official sample 24 (best pair (9,5)-(2,3)), and that the whole-loop usable
cell count 7070643880 equals Pick's theorem A + B/2 + 1 (shoelace A=7070349835,
boundary B=588088). The per-cell reference is skipped for very large bounding
boxes (real input) where the Pick's-theorem cell count is the independent check.
See `skills/red-rectangles/SKILL.md`.

### Button-toggle minimum presses (min-weight GF(2) XOR subset)

For puzzles where each button toggles a fixed subset of binary indicator
lights, pressing twice cancels, and the answer is the fewest total presses to
reach every target pattern (e.g. Day 10 part 1 "Factory"), cross-check
deterministically. Each line is `[######] (btn ids) ... {ignored joltages}`:

```bash
python3 scripts/check-toggle-presses.py                 # self-test (random + sample 7 + real 417)
python3 scripts/check-toggle-presses.py inputs/Day10Test.txt 7
python3 scripts/check-toggle-presses.py inputs/Day10.txt 417
```

Read-only; never submits. The self-test cross-checks two independent
implementations (full 2^b subset enumeration mirroring `Day10.part1`, and a
BFS over the XOR light-pattern state graph) on 2000 generated machines and
asserts the accepted Day 10 real-input answer 417, the official sample 7, and
the per-machine minima `[2, 3, 2]`. See `skills/toggle-presses/SKILL.md`.

### Additive joltage counters (min presses to exact additive targets)

For puzzles where each button increments a listed subset of numeric counters by
1 per press and the answer is the fewest total presses to drive every counter
to an exact target joltage, i.e. the minimum-weight non-negative integer
solution of the 0/1 linear system `A x = b` (e.g. Day 10 part 2 "Factory",
joltage requirements), cross-check deterministically. Each line is
`[######] (btn ids) ... {joltage targets}`:

```bash
python3 scripts/check-joltage-counters.py                # self-test (random + sample 33 + real 16765)
python3 scripts/check-joltage-counters.py inputs/Day10Test.txt 33
python3 scripts/check-joltage-counters.py inputs/Day10.txt 16765
```

Read-only; never submits. The self-test cross-checks the exact
branch-and-bound over the rational LP relaxation (mirroring `Day10.part2` /
`CounterSolver`) against an independent exhaustive brute force on 1700
generated machines and asserts the accepted Day 10 real-input answer 16765,
the official sample 33, and the per-machine minima `[10, 12, 11]`. See
`skills/joltage-counters/SKILL.md`.

### Count distinct directed paths through a DAG (from a source node to a target)

For puzzles where each line lists a node followed by the nodes its outputs
attach to (`node: out1 out2 ...`), data flows only forward through outputs so
the graph is a directed acyclic graph, and the answer is the number of distinct
directed paths from a source device (e.g. `you`) to a target device (e.g.
`out`) — e.g. Day 11 "Reactor" — cross-check deterministically:

```bash
python3 scripts/check-dag-paths.py                 # self-test (random + sample 5 + real 543)
python3 scripts/check-dag-paths.py inputs/Day11Test.txt 5
python3 scripts/check-dag-paths.py inputs/Day11.txt 543
```

Read-only; never submits. The self-test cross-checks two independent
implementations (a memoised depth-first sum mirroring `Day11.part1`, and an
explicit path-enumeration reference) on 4000 seeded random acyclic wirings
(3–12 devices, `you` first / `out` last) and asserts the accepted Day 11
real-input answer 543 and the official sample 5 (10 devices). It also covers
the unreachable-source case (both implementations count 0). See
`skills/dag-paths/SKILL.md`.

For the visits-all variant (Day 11 part 2) — count only the `svr -> out` paths
that visit BOTH required devices `dac` and `fft` in any order (sample 2, real
479511112939968) — add `--visits-all`:

```bash
python3 scripts/check-dag-paths.py --visits-all     # self-test (random + sample 2 + real 479511112939968)
python3 scripts/check-dag-paths.py --visits-all inputs/Day11.txt 479511112939968
```

Read-only; never submits. The `--visits-all` self-test cross-checks two
independent implementations (a subset-mask DFS mirroring `Day11.part2`, and an
inclusion-exclusion reference that prunes required devices and counts plain
paths) on 3000 seeded random acyclic wirings (4–12 devices, `svr` first /
`out` last) and asserts the accepted Day 11 real-input answer 479511112939968
and the official part-2 sample 2 (13 devices). See
`skills/dag-paths/SKILL.md`.

### Polyomino present packing (fit every shape into each rectangular region)

For puzzles that ask how many rectangular regions can fit a multiset of small
present shapes, where each present may be rotated/reflected, the `#` cells of
different presents may not overlap, and the `.` cells are shared freely (e.g.
Day 12 part 1 "Christmas Tree Farm"), cross-check deterministically:

```bash
python3 scripts/check-polyomino-packing.py                    # self-test (random + sample 2 + real 408, ~45s)
python3 scripts/check-polyomino-packing.py inputs/Day12Test.txt 2
python3 scripts/check-polyomino-packing.py inputs/Day12.txt 408
```

Read-only; never submits. The self-test cross-checks the anchored backtracking
mirror (of `Day12.part1`) against an independent exact-cover reference on 4000
seeded random small regions, asserts the official Day 12 sample (2) including
its area-feasible-but-geometrically-impossible third region (`12x5: 1 0 1 0 3 2`),
the boundary fixtures, and the accepted Day 12 real-input answer 408 — which
equals the independent area-feasibility count on the real input. The file mode
also prints that area-feasibility count (`area=`) alongside `fits=`. The
self-test takes about 45 seconds because the real input runs the full geometric
search over 1000 regions. See `skills/polyomino-packing/SKILL.md`.

### Verify a committed day solution

Before a submit or review disposition for day NN part 1 and/or part 2, reproduce
the local evidence chain from the synchronized checkout:

```bash
scripts/verify-day.sh <NN> [EXPECTED_PART1] [EXPECTED_PART2]   # e.g. scripts/verify-day.sh 01 1129 6638
```

The script checks `HEAD == origin/main` with a clean tree, runs the day-specific
test suite (`sbt testOnly aoc2025.DayNNTest`), and asserts the `sbt run`
part-1 (and part-2 when `EXPECTED_PART2` is given) output against the supplied
expected values. See `skills/verify-day/SKILL.md` for the full procedure and
stopping conditions.

## Toolchain

Managed with [asdf][] via `.tool-versions` (run `asdf install`):

- Java 25 (LTS, `openjdk-25`) — sbt 2 and Scala 3.9 require JDK 17+
- sbt 2.0.10 (latest stable 2.x line)
- Scala 3.9.0 (LTS, set in `build.sbt`) — the recommended baseline succeeding 3.3 LTS

## Architecture and Design Decisions

### Why sbt (2.x)?

sbt was chosen over the blueprint's Mill because:
- The team manages Java/sbt versions with asdf (pinned in `.tool-versions`)
- sbt 2 is the current stable major line (Scala 3-based build definitions, Bazel-compatible caching, `sbtn` client)
- Standard sbt layout (`src/main/scala`, `src/test/scala`) is understood by Metals/IntelliJ out of the box

The build configuration lives in `build.sbt` with `project/plugins.sbt` (sbt-scalafmt, sbt-scalafix, sbt-scoverage).

### Why This Project Structure?

**Daily Solutions Pattern**: Each day follows a consistent structure because:
- `readFile()` isolates input parsing from algorithm logic
- `part1()` and `part2()` methods allow independent testing
- `Main.scala` (`@main def solve()`, run via `sbt run`) orchestrates execution
- This separation enables better testing and code reuse

**Utility Framework Philosophy**: The `util` package was designed to:
- Extract common patterns that appear across multiple puzzles
- Avoid premature abstraction (utilities are added only after patterns emerge)
- Provide composable building blocks rather than monolithic solutions
- Maintain type safety while keeping the API ergonomic

### Key Design Patterns

**Grid System**: Many AoC puzzles involve 2D grids, so we built a comprehensive grid framework:
- `Grid` class provides a high-level abstraction for grid navigation
- `Position`/`DPosition` handle coordinates with directional movement
- `GridGraph`/`WDGridGraph` convert grids to graph representations for pathfinding
- This design separates spatial representation from algorithm implementation

**Search Algorithms**: BFS and DFS are implemented separately because:
- Different puzzles need different traversal orders
- Keeping them separate makes the code easier to understand and test
- Both integrate with the Grid system through common interfaces

**Path Tracking**: The `Path` utility emerged from puzzles requiring:
- Score accumulation during traversal
- History tracking for constraint checking
- Backtracking for finding all valid paths

### Dependency Choices (all on latest versions)

**scala-graph** (`graph-core` 2.0.3): Used for shortest path and graph algorithms because:
- Provides battle-tested implementations
- Integrates well with Scala's type system
- Supports weighted graphs needed for many puzzles

**breeze** (2.1.0): Chosen for linear algebra because:
- Some puzzles require solving equation systems
- Well-maintained library with good performance
- Familiar API for developers with NumPy experience

**scala-corner** (1.0.4): Specialized library for counting corners in 2D regions because:
- This specific geometric operation appears in multiple puzzles
- Implementing it correctly is non-trivial
- Using a library reduces bug risk

**munit** (1.1.1, plus `munit-scalacheck` 1.1.0): Selected as testing framework because:
- Lightweight and fast
- Good Scala 3 support
- Simple assertion syntax

**scala-logging** (3.9.5) + **logback** (1.5.18): Logging, as in the blueprint.

**scala-parallel-collections** (1.2.0) + **scala-collection-contrib** (0.4.0): Parallel/extra collection ops for heavy puzzles.

**spire** (0.18.0, test scope): Exact numeric types for property-based tests.

Note: `com.eed3si9n.eval` (Scala toolbox, used only by Day24 in the 2024 blueprint) was dropped — it pins artifacts to an old Scala minor version and nothing in this scaffold uses it. Re-add it (latest per-Scala-minor artifact) if a future day needs runtime evaluation.

**sbt plugins** (all with sbt 2 support): sbt-scalafmt 2.6.2, sbt-scalafix 0.14.9 (with `scalafix-rules` 0.6.30), sbt-scoverage 2.4.4. scalafmt binary 3.11.5 (see `.scalafmt.conf`).

### Testing Philosophy

Tests are structured with:
- Separate test data files (`src/main/resources/inputs/`) to isolate test inputs from production code
- One test class per day for organization
- `Test / fork := true` with `-Xss1G -Xmx10G` for stack-hungry puzzles
- Target of 80% code coverage (via `sbt "coverage; test; coverageReport"`) to balance thoroughness with pragmatism

### Code Style Decisions

**Indent-based syntax**: Idiomatic Scala 3:
- More idiomatic in Scala 3
- Reduces visual noise
- Encourages better code structure through indentation awareness

**Strict compiler settings**: Warnings treated as errors (`-Wunused:imports`, `-Werror`, `-deprecation`) because:
- Forces addressing issues immediately
- Prevents accumulation of technical debt
- Maintains high code quality throughout development

**Formatting and linting**: Automated with scalafmt (`sbt scalafmtAll`) and scalafix (`sbt scalafixAll`, enforced pre-push via `hooks/pre-push`) because:
- Removes subjective style debates
- Ensures consistency across the codebase
- Catches common mistakes early

### Day00 - Fibonacci dummy

Day00 is a runnable template/dummy (not a real AoC day):

- Input files `Day00.txt` / `Day00Test.txt` contain `10` (part1) and `1000` (part2), one per line.
- `fib(n: Int): BigInt` is tail-recursive with `fib(0) = 0`, `fib(1) = 1`.
- `part1(is: Seq[Int]): BigInt = fib(is.head)` → `fib(10) = 55`.
- `part2(is: Seq[Int]): BigInt = fib(is.last)` → `fib(1000) = 4346655...28875` (209 digits).
- `BigInt` is required because `fib(1000)` does not fit into `Int`/`Long`.

[README.md]: README.md
[CONTRIBUTING.md]: CONTRIBUTING.md
[asdf]: https://asdf-vm.com
[scala3-aoc-2024]: https://github.com/rolandtritsch/scala3-aoc-2024
