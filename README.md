# scala3-aoc-2025

![aoctree][]

Solutions to [Advent of Code 2025][aoc2025] challenges implemented in Scala 3.

Based on the [scala3-aoc-2024][] blueprint (migrated from Mill to sbt).

The repository has been reset to the Day00 template. Other problems, inputs,
solutions, and tests have been removed. The build and reusable utilities remain;
`.gitkeep` files preserve empty folders.

## Toolchain (managed with asdf)

| Tool | Version | Source         |
| ---- | ------- | -------------- |
| Java | 25 (LTS) | `.tool-versions` (`openjdk-25`) |
| sbt  | 2.0.10  | `.tool-versions` |
| Scala | 3.9.0 (LTS) | `build.sbt` |

```bash
# Install the pinned toolchain
asdf install
```

## What is this?

This repository contains my solutions to the [Advent of Code 2025][aoc2025] programming puzzles. Advent of Code is an annual event featuring daily programming challenges throughout December. Each day presents a two-part puzzle that can be solved using any programming language.

This implementation focuses on:
- Learning Scala 3 features and idioms
- Building reusable utilities for common puzzle patterns
- Exploring AI-assisted development with GitHub Copilot and Codeium

## Quick Start

### Prerequisites

- [asdf][] with the `java` and `sbt` plugins (versions come from `.tool-versions`)

### Running Solutions

```bash
# Run the Day00 template
sbt run

# Run tests
sbt test
```

### Project Structure

- `src/main/scala/aoc2025/` - Daily puzzle solutions (Day00.scala through Day25.scala)
- `src/main/scala/util/` - Reusable utility classes for common patterns
- `src/main/resources/inputs/` - Puzzle input files
- `src/test/scala/` - Test suites with sample data

## Implementation Details

For information about the architecture, design decisions, and implementation patterns, see [CLAUDE.md][].

## Contributing

Interested in contributing? Check out the [CONTRIBUTING.md][] file for development workflow and guidelines.

## What I Learned

- Scala 3 features: extensions, given/using, indent-based syntax
- sbt build system and Scala tooling (scalafmt, scalafix, scoverage)
- Graph algorithms with `scala-graph`
- Linear algebra with `breeze`
- Geometry algorithms with `scala-corner`
- AI-assisted development workflows

[aoc2025]: https://adventofcode.com/2025
[scala3-aoc-2024]: https://github.com/rolandtritsch/scala3-aoc-2024
[aoctree]: assets/aoc-tree.png
[asdf]: https://asdf-vm.com
[mill]: https://mill-build.org
[CLAUDE.md]: CLAUDE.md
[CONTRIBUTING.md]: CONTRIBUTING.md
