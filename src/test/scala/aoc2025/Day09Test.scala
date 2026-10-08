package aoc2025

import aoc2025.Day09.*

class Day09Test extends munit.ScalaCheckSuite:
  val ignore = new munit.Tag("ignore")

  test("Day09 - readFile - test"):
    val obtained = readFile("inputs/Day09Test.txt")
    assertEquals(
      obtained,
      Seq((7L, 1L), (11L, 1L), (11L, 7L), (9L, 7L), (9L, 5L), (2L, 5L), (2L, 3L), (7L, 3L)),
    )

  test("Day09 - readFile"):
    val obtained = readFile("inputs/Day09.txt")
    assertEquals(obtained.length, 496)

  test("Day09 - part1 - test"):
    val input = readFile("inputs/Day09Test.txt")
    assertEquals(part1(input), 50L)

  test("Day09 - part1"):
    val input = readFile("inputs/Day09.txt")
    assertEquals(part1(input), 4777967538L)

  test("Day09 - part2 - test".tag(ignore)):
    val input = readFile("inputs/Day09Test.txt")
    assertEquals(part2(input), 0L)

end Day09Test
