package aoc2025

import aoc2025.Day07.*

class Day07Test extends munit.ScalaCheckSuite:
  val ignore = new munit.Tag("ignore")

  test("Day07 - readFile - sample"):
    val rows = readFile("inputs/Day07Test.txt")
    assertEquals(rows.size, 16)
    assertEquals(rows.head, ".......S.......")
    assertEquals(rows(rows.size - 2), ".^.^.^.^.^...^.")
    assertEquals(rows.last, "...............")

  test("Day07 - readFile - real"):
    val rows = readFile("inputs/Day07.txt")
    assertEquals(rows.size, 142)
    assertEquals(rows.head.count(_ == 'S'), 1)
    assert(rows.exists(_.contains('^')))

  test("Day07 - part1 - sample"):
    assertEquals(part1(readFile("inputs/Day07Test.txt")), 21)

  test("Day07 - part1 - merging and splitter counted once"):
    assertEquals(part1(Vector("..S..", "..^..", ".^.^.", "..^..")), 4)

  test("Day07 - part1 - real"):
    assertEquals(part1(readFile("inputs/Day07.txt")), 1605)

  test("Day07 - part2 - out of scope".tag(ignore)):
    assertEquals(part2(readFile("inputs/Day07Test.txt")), 40L)

end Day07Test
