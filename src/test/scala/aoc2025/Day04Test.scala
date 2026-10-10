package aoc2025

import aoc2025.Day04.*

class Day04Test extends munit.FunSuite:

  test("Day04 - readFile - sample"):
    assertEquals(
      readFile("inputs/Day04Test.txt"),
      Seq(
        "..@@.@@@@.",
        "@@@.@.@.@@",
        "@@@@@.@.@@",
        "@.@@@@..@.",
        "@@.@@@@.@@",
        ".@@@@@@@.@",
        ".@.@.@.@@@",
        "@.@@@.@@@@",
        ".@@@@@@@@.",
        "@.@.@@@.@.",
      ),
    )

  test("Day04 - readFile - real"):
    val rows = readFile("inputs/Day04.txt")
    assertEquals(rows.size, 138)
    assert(rows.forall(_.nonEmpty))
    assert(rows.forall(_.forall(c => c == '@' || c == '.')))

  test("Day04 - adjacentRolls - interior of an all-roll 3x3 block"):
    val block = Seq("@@@", "@@@", "@@@")
    assertEquals(adjacentRolls(block, 1, 1), 8)
    assertEquals(adjacentRolls(block, 0, 0), 3)
    assertEquals(adjacentRolls(block, 0, 1), 5)

  test("Day04 - adjacentRolls - grid edges are not wrapped"):
    assertEquals(adjacentRolls(Seq("@"), 0, 0), 0)
    assertEquals(adjacentRolls(Seq("@@"), 0, 0), 1)
    assertEquals(adjacentRolls(Seq("@@", "@@"), 0, 0), 3)

  test("Day04 - part1 - single roll has no neighbours"):
    assertEquals(part1(Seq("@")), 1)

  test("Day04 - part1 - no rolls"):
    assertEquals(part1(Seq("...", "...", "...")), 0)

  test("Day04 - part1 - all-roll 3x3 block has four accessible corners"):
    assertEquals(part1(Seq("@@@", "@@@", "@@@")), 4)

  test("Day04 - part1 - sample"):
    assertEquals(part1(readFile("inputs/Day04Test.txt")), 13)

  test("Day04 - part1 - real"):
    val rows = readFile("inputs/Day04.txt")
    assertEquals(part1(rows), 1356)

  test("Day04 - part2 - single roll is removed"):
    assertEquals(part2(Seq("@")), 1)

  test("Day04 - part2 - no rolls"):
    assertEquals(part2(Seq("...", "...", "...")), 0)

  test("Day04 - part2 - all-roll 3x3 block removes the four corners, then the rest"):
    assertEquals(part2(Seq("@@@", "@@@", "@@@")), 9)

  test("Day04 - part2 - a 2x2 block is fully removable"):
    assertEquals(part2(Seq("@@", "@@")), 4)

  test("Day04 - part2 - sample"):
    assertEquals(part2(readFile("inputs/Day04Test.txt")), 43)

  test("Day04 - part2 - real"):
    val rows = readFile("inputs/Day04.txt")
    assertEquals(part2(rows), 8713)

end Day04Test
