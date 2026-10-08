package aoc2025

import aoc2025.Day08.*

class Day08Test extends munit.ScalaCheckSuite:
  val ignore = new munit.Tag("ignore")

  test("Day08 - readFile - test"):
    val obtained = readFile("inputs/Day08Test.txt")
    assertEquals(obtained.size, 20)
    assertEquals(obtained.head, JunctionBox(162, 817, 812))
    assertEquals(obtained.last, JunctionBox(425, 690, 689))

  test("Day08 - readFile"):
    val obtained = readFile("inputs/Day08.txt")
    assertEquals(obtained.size, 1000)

  test("Day08 - part1 - test"):
    val input = readFile("inputs/Day08Test.txt")
    assertEquals(part1(input, connectionCount = 10), BigInt(40))

  test("Day08 - part1"):
    val input = readFile("inputs/Day08.txt")
    assertEquals(part1(input), BigInt(90036))

  test("Day08 - part2 - test".tag(ignore)):
    val input = readFile("inputs/Day08Test.txt")
    assertEquals(part2(input), 0)

end Day08Test
