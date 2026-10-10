# CLAUDE.md

This file provides guidance to Claude Code when working with this repository. For basic usage information, see [README.md][]. For contribution guidelines, see [CONTRIBUTING.md][].

Based on the [scala3-aoc-2024][] blueprint (which used Mill; this repo uses sbt).

## Project-local skills and monitoring

Read applicable `skills/*/SKILL.md` before repeating work. Reusable scripts belong
in `scripts/`; reviewed skill/script improvements are validated and committed in
this repository. The [WatchDog telemetry skill][watchdog-telemetry] documents the
read-only collector and its runtime inputs. It creates no report tasks or live
mutations; the company observer handles its own reports separately.

Validate collector changes with `python3 -B -m unittest discover -s scripts/tests -v`.
Use `-B` to avoid Python bytecode in the checkout. Monitoring does not need an sbt
build and does not alter puzzle solutions, agent state, or submission history.

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

[watchdog-telemetry]: skills/aoc-watchdog-telemetry/SKILL.md
