package aoc2025

import aoc2025.Day04.*

class Day04Test extends munit.ScalaCheckSuite:
  val ignore = new munit.Tag("ignore")

  test("Day04 - readFile - sample"):
    val rows = readFile("inputs/Day04Test.txt")
    assertEquals(rows.size, 10)
    assertEquals(rows.head, "..@@.@@@@.")
    assertEquals(rows.last, "@.@.@@@.@.")

  test("Day04 - readFile - real"):
    val rows = readFile("inputs/Day04.txt")
    assertEquals(rows.size, 138)
    assert(rows.forall(_.length == rows.head.length))

  test("Day04 - part1 - sample"):
    assertEquals(part1(readFile("inputs/Day04Test.txt")), 13)

  test("Day04 - part1 - boundaries and diagonals"):
    assertEquals(part1(Seq("@.@", "...", "@.@")), 4)
    assertEquals(part1(Seq("@@@", "@@@", "@@@")), 4)
    assertEquals(part1(Seq("@")), 1)

  test("Day04 - part1 - real"):
    assertEquals(part1(readFile("inputs/Day04.txt")), 1356)

  test("Day04 - part2 - out of scope".tag(ignore)):
    assertEquals(part2(readFile("inputs/Day04Test.txt")), 43)
end Day04Test
