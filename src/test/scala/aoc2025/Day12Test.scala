package aoc2025

import aoc2025.Day12.*

class Day12Test extends munit.FunSuite:

  private def ring = Shape(Vector((0, 0), (1, 0), (2, 0), (1, 1), (0, 2), (1, 2), (2, 2)))

  private def triomino = Shape(Vector((0, 0), (1, 0), (2, 0)))

  private def domino = Shape(Vector((0, 0), (1, 0)))

  test("Day12 - readFile - sample shapes"):
    val input = readFile("inputs/Day12Test.txt")
    assertEquals(input.shapes.size, 6)
    assertEquals(input.shapes.map(_.cellCount), Vector(7, 7, 7, 7, 7, 7))
    assertEquals(input.regions.size, 3)
    assertEquals(input.regions.head, Region(4, 4, Vector(0, 0, 0, 0, 2, 0)))

  test("Day12 - part1 - sample"):
    assertEquals(part1(readFile("inputs/Day12Test.txt")), 2)

  test("Day12 - part1 - no presents fits any region"):
    // A region requiring zero presents always fits.
    assertEquals(part1(Input(Vector(ring), Vector(Region(5, 5, Vector(0))))), 1)

  test("Day12 - part1 - total cells exceed region area"):
    // 7 cells into a 6-cell (2x3) region is impossible regardless of geometry.
    assertEquals(part1(Input(Vector(ring), Vector(Region(2, 3, Vector(1))))), 0)

  test("Day12 - part1 - geometry can forbid a fit despite enough area"):
    // A 1x3 triomino (3 cells) cannot fit into a 2x2 region (4 cells) in any rotation.
    assertEquals(part1(Input(Vector(triomino), Vector(Region(2, 2, Vector(1))))), 0)

  test("Day12 - part1 - exact tiling with dominoes"):
    // Two 1x2 dominoes exactly tile a 2x2 region.
    assertEquals(part1(Input(Vector(domino), Vector(Region(2, 2, Vector(2))))), 1)

  test("Day12 - part1 - mixed regions counted separately"):
    // A fitting region plus a too-small region yields one fit.
    val input = Input(Vector(domino), Vector(Region(2, 2, Vector(2)), Region(1, 2, Vector(2))))
    assertEquals(part1(input), 1)

  test("Day12 - part1 - real"):
    assertEquals(part1(readFile("inputs/Day12.txt")), 408)

end Day12Test
