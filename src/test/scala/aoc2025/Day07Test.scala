package aoc2025

import aoc2025.Day07.*

class Day07Test extends munit.FunSuite:

  test("Day07 - readFile - sample"):
    val grid = readFile("inputs/Day07Test.txt")
    assertEquals(grid.size, 16)
    assertEquals(grid.head, ".......S.......")
    assertEquals(grid.head.indexOf('S'), 7)
    assert(grid.forall(_.forall(c => c == '.' || c == 'S' || c == '^')))

  test("Day07 - part1 - no splitter"):
    val grid = Vector("S", ".", ".")
    assertEquals(part1(grid), 0)

  test("Day07 - part1 - single splitter"):
    val grid = Vector("S", ".", "^", ".")
    assertEquals(part1(grid), 1)

  test("Day07 - part1 - splitter emits one beam off the left edge"):
    // S at the left edge; the splitter below it emits left into the wall (dropped) and right.
    val grid = Vector("S", ".", ".", "^")
    assertEquals(part1(grid), 1)

  test("Day07 - part1 - merged beams split each of two splitters once"):
    // S splits at row 1 (1 split); the two beams hit splitters at cols 1 and 3 on row 3
    // (2 splits), both dumping into the shared centre column 2.
    val grid = Vector("..S..", "..^..", ".....", ".^.^.", ".....")
    assertEquals(part1(grid), 3)

  test("Day07 - part1 - sample"):
    assertEquals(part1(readFile("inputs/Day07Test.txt")), 21)

  test("Day07 - part1 - real"):
    assertEquals(part1(readFile("inputs/Day07.txt")), 1605)

  test("Day07 - part2 - no splitter"):
    val grid = Vector("S", ".", ".")
    assertEquals(part2(grid), 1L)

  test("Day07 - part2 - single splitter"):
    val grid = Vector("S", ".", "^", ".")
    assertEquals(part2(grid), 2L)

  test("Day07 - part2 - splitter at left edge emits one timeline off-grid"):
    // S directly above a splitter at column 0: left branch exits the manifold (1 timeline),
    // right branch continues one column and exits the bottom (1 timeline).
    val grid = Vector("S.", "^.", "..")
    assertEquals(part2(grid), 2L)

  test("Day07 - part2 - merged timelines are not deduplicated"):
    // Two splitters dump both their branches into the shared centre column: the two distinct
    // paths to that column remain two separate timelines, so the totals accumulate.
    val grid = Vector("..S..", "..^..", ".....", ".^.^.", ".....")
    assertEquals(part2(grid), 4L)

  test("Day07 - part2 - sample"):
    assertEquals(part2(readFile("inputs/Day07Test.txt")), 40L)

  test("Day07 - part2 - real"):
    assertEquals(part2(readFile("inputs/Day07.txt")), 29893386035180L)

end Day07Test
