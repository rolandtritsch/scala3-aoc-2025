package aoc2025

import aoc2025.Day08.*

class Day08Test extends munit.FunSuite:

  test("Day08 - readFile - sample"):
    val points = readFile("inputs/Day08Test.txt")
    assertEquals(points.size, 20)
    assertEquals(points.head, Point3D(162, 817, 812))
    assertEquals(points.last, Point3D(425, 690, 689))

  test("Day08 - squaredDistance"):
    assertEquals(squaredDistance(Point3D(0, 0, 0), Point3D(3, 4, 0)), 25L)
    assertEquals(squaredDistance(Point3D(1, 2, 3), Point3D(1, 2, 3)), 0L)

  test("Day08 - part1 - no connections"):
    val points = Vector(Point3D(0, 0, 0), Point3D(1, 0, 0), Point3D(2, 0, 0))
    assertEquals(part1(points, 0), 1L)

  test("Day08 - part1 - two connections on a line"):
    val points = Vector(Point3D(0, 0, 0), Point3D(1, 0, 0), Point3D(3, 0, 0), Point3D(7, 0, 0))
    assertEquals(part1(points, 2), 3L)

  test("Day08 - part1 - redundant connection does nothing"):
    val points = Vector(Point3D(0, 0, 0), Point3D(1, 0, 0), Point3D(3, 0, 0))
    assertEquals(part1(points, 2), 3L)
    assertEquals(part1(points, 3), 3L)

  test("Day08 - part1 - sample"):
    assertEquals(part1(readFile("inputs/Day08Test.txt"), 10), 40L)

  test("Day08 - part1 - real"):
    assertEquals(part1(readFile("inputs/Day08.txt"), 1000), 90036L)

  test("Day08 - part2 - single box"):
    assertEquals(part2(Vector(Point3D(7, 1, 2))), 0L)

  test("Day08 - part2 - two boxes"):
    assertEquals(part2(Vector(Point3D(3, 0, 0), Point3D(5, 0, 0))), 15L)

  test("Day08 - part2 - three collinear boxes"):
    assertEquals(part2(Vector(Point3D(0, 0, 0), Point3D(1, 0, 0), Point3D(10, 0, 0))), 10L)

  test("Day08 - part2 - sample"):
    assertEquals(part2(readFile("inputs/Day08Test.txt")), 25272L)

  test("Day08 - part2 - real"):
    assertEquals(part2(readFile("inputs/Day08.txt")), 6083499488L)

end Day08Test
