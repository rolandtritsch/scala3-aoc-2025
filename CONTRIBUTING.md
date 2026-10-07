# Contributing

Thank you for your interest in contributing! This document explains how to set up your development environment and submit changes. For architectural details, see [CLAUDE.md][].

## Roles

This project has two contributor roles:

- **Contributors**: Submit pull requests with changes
- **Committers**: Review, merge PRs, and publish releases

## Development Workflow

### 1. Clone the Repository

```bash
git clone https://github.com/rolandtritsch/scala3-aoc-2025.git
cd scala3-aoc-2025
```

### 2. Set Up Your Environment

Install the pinned toolchain (Java 25, sbt 2.0.10 — see `.tool-versions`):

```bash
asdf install
```

Install the pre-push hook to ensure code quality:

```bash
cd .git/hooks
ln -s ../../hooks/pre-push .
```

This hook runs formatting and linting checks before each push.

### 3. Create a Branch

Branch names follow the format:

```
roland/<ticket-id>/<3-word-description>
```

If there's no ticket, use `ad-hoc` instead:

```
roland/ad-hoc/<3-word-description>
```

Examples:

```bash
# With ticket
git checkout -b roland/AOC-123/fix-grid-parsing

# Without ticket (ad-hoc work)
git checkout -b roland/ad-hoc/improve-test-coverage
```

### 4. Make Your Changes

**Commit frequently**: Make small, focused commits as you work. Each commit should represent a logical unit of work.

```bash
git add .
git commit -m "Add Grid utility tests"
```

**Write tests**: All new functionality must have tests. Aim for 80% code coverage.

```bash
# Run tests
sbt test

# Check coverage
sbt "coverage; test; coverageReport"
```

**Follow code style**: The pre-push hook will enforce formatting, but you can run checks manually:

```bash
# Format code
sbt scalafmtAll

# Run linter
sbt scalafixAll
```

### 5. Create a Pull Request

Create your PR using the GitHub CLI:

```bash
# Push your branch
git push -u origin roland/<ticket-id>/<3-word-description>

# Create PR with required format
gh pr create --title "<ticket-id>: <3-word-description>" --body ""
```

**PR Title Format**: `<ticket-id>: <3-word-description>`

Examples:
- `AOC-123: fix-grid-parsing`
- `ad-hoc: improve-test-coverage`

**PR Body**: Leave empty (the body should be empty as per project conventions).

### 6. Verify CI Passes

After pushing, ensure all GitHub Actions workflows succeed:

```bash
# View CI status
gh pr checks

# View detailed logs if needed
gh run list
gh run view <run-id>
```

If checks fail, fix the issues and push again.

### 7. Merge and Clean Up

Once your PR is approved and all checks pass, a committer will squash-merge it:

```bash
# For committers only
gh pr merge <pr-number> --squash --delete-branch
```

## Testing Requirements

All contributions must meet these testing standards:

1. **Unit tests** for new functionality
2. **Test data files** in `src/main/resources/inputs/` for puzzle solutions
3. **80% code coverage** minimum
4. **All tests passing** before merge

### Running Tests

```bash
# Run all tests
sbt test

# Run specific test
sbt "testOnly aoc2025.Day01Test"

# Run with coverage
sbt "coverage; test; coverageReport"
# View report at target/scala-3.9.0/scoverage-report/index.html
```

## Code Quality Standards

This project maintains high code quality through:

- **Scalafmt**: Automatic code formatting
- **Scalafix**: Linting and code analysis
- **Compiler warnings as errors**: All warnings must be addressed
- **No unused imports**: Keep imports clean and minimal

Run quality checks:

```bash
sbt scalafmtAll  # Format code
sbt scalafixAll  # Run linter
sbt compile      # Check for warnings
```

## Common Commands

```bash
# Development
sbt compile           # Compile code
sbt test              # Run tests
sbt run               # Run all solutions

# Code quality
sbt scalafmtAll       # Format code
sbt scalafixAll       # Run linter
sbt "coverage; test; coverageReport"  # Generate coverage report

# Git workflow
git checkout -b roland/<ticket-id>/<3-word-description>
git add .
git commit -m "Your commit message"
git push -u origin <branch-name>
gh pr create --title "<ticket-id>: <3-word-description>" --body ""
gh pr checks
```

## Getting Help

- Check [README.md][] for basic usage
- Read [CLAUDE.md][] for architecture details
- Open an issue for questions or bug reports

[README.md]: README.md
[CLAUDE.md]: CLAUDE.md
