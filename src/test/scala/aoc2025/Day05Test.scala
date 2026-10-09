package aoc2025

import aoc2025.Day05.*

class Day05Test extends munit.ScalaCheckSuite:
  val ignore = new munit.Tag("ignore")

  test("Day05 - readFile - sample"):
    val (ranges, available) = readFile("inputs/Day05Test.txt")
    assertEquals(ranges, Vector((3L, 5L), (10L, 14L), (16L, 20L), (12L, 18L)))
    assertEquals(available, Vector(1L, 5L, 8L, 11L, 17L, 32L))

  test("Day05 - readFile - real"):
    val (ranges, available) = readFile("inputs/Day05.txt")
    assertEquals(ranges.size, 182)
    assertEquals(available.size, 1000)
    assert(ranges.exists(_._1 > Int.MaxValue.toLong))

  test("Day05 - part1 - sample"):
    assertEquals(part1(readFile("inputs/Day05Test.txt")), 3)

  test("Day05 - part1 - inclusive boundaries and overlaps"):
    assertEquals(part1((Seq((3L, 5L), (5L, 7L)), Seq(2L, 3L, 5L, 7L, 8L))), 3)
    assertEquals(part1((Seq((10L, 10L)), Seq(10L, 11L))), 1)

  test("Day05 - part1 - real"):
    assertEquals(part1(readFile("inputs/Day05.txt")), 848)

  test("Day05 - part2 - out of scope".tag(ignore)):
    assertEquals(part2(readFile("inputs/Day05Test.txt")), 14L)

end Day05Test
