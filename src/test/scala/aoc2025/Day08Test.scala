package aoc2025

import aoc2025.Day08.*

class Day08Test extends munit.ScalaCheckSuite:
  val ignore = new munit.Tag("ignore")

  test("Day08 - readFile - sample"):
    val points = readFile("inputs/Day08Test.txt")
    assertEquals(points.size, 20)
    assertEquals(points.head, Point(162, 817, 812))
    assertEquals(points.last, Point(425, 690, 689))

  test("Day08 - readFile - real"):
    val points = readFile("inputs/Day08.txt")
    assertEquals(points.size, 1000)
    assertEquals(points.head, Point(19811, 63901, 80424))

  test("Day08 - part1 - sample"):
    assertEquals(part1(readFile("inputs/Day08Test.txt"), 10), 40L)

  test("Day08 - part1 - real"):
    assertEquals(part1(readFile("inputs/Day08.txt")), 90036L)

  test("Day08 - part1 - redundant pairs still consume the connection budget"):
    val points = Vector(0L, 1L, 3L, 100L, 200L, 300L).map(x => Point(x, 0, 0))
    assertEquals(part1(points, 3), 3L)
    assertEquals(part1(points, 4), 4L)

  test("Day08 - part1 - zero connections leave singleton circuits"):
    assertEquals(part1(Vector(Point(0, 0, 0), Point(1, 0, 0), Point(2, 0, 0)), 0), 1L)

  test("Day08 - part1 - squared distances use Long arithmetic"):
    val points = Vector(0L, 50000L, 110000L, 180000L).map(x => Point(x, 0, 0))
    assertEquals(part1(points, 1), 2L)

  test("Day08 - part1 - invalid arguments"):
    intercept[IllegalArgumentException](part1(Vector.empty, 0))
    intercept[IllegalArgumentException](part1(Vector.fill(3)(Point(0, 0, 0)), -1))

  test("Day08 - part2 - out of scope".tag(ignore)):
    intercept[NotImplementedError](part2(readFile("inputs/Day08Test.txt")))
end Day08Test
