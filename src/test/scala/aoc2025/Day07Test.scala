package aoc2025

import aoc2025.Day07.*

class Day07Test extends munit.ScalaCheckSuite:
  val ignore = new munit.Tag("ignore")

  test("Day07 - readFile - test"):
    val obtained = readFile("inputs/Day07Test.txt")
    assertEquals(obtained.size, 16)
    assertEquals(obtained.head, ".......S.......")
    assertEquals(obtained.last, "...............")

  test("Day07 - readFile"):
    val obtained = readFile("inputs/Day07.txt")
    assertEquals(obtained.size, 142)

  test("Day07 - part1 - test"):
    val input = readFile("inputs/Day07Test.txt")
    assertEquals(part1(input), 21)

  test("Day07 - part1"):
    val input = readFile("inputs/Day07.txt")
    assertEquals(part1(input), 1605)

  test("Day07 - part2 - test".tag(ignore)):
    val input = readFile("inputs/Day07Test.txt")
    assertEquals(part2(input), 0)

end Day07Test
