package aoc2025

import aoc2025.Day09.*

class Day09Test extends munit.FunSuite:

  test("Day09 - readFile - sample"):
    val tiles = readFile("inputs/Day09Test.txt")
    assertEquals(tiles.size, 8)
    assertEquals(tiles.head, Tile(7, 1))
    assertEquals(tiles.last, Tile(7, 3))

  test("Day09 - area - rectangle between (2,5) and (9,7)"):
    assertEquals(area(Tile(2, 5), Tile(9, 7)), 24L)

  test("Day09 - area - thin rectangle sharing a row"):
    assertEquals(area(Tile(7, 3), Tile(2, 3)), 6L)

  test("Day09 - area - degenerate single-tile pair"):
    assertEquals(area(Tile(3, 4), Tile(3, 4)), 1L)

  test("Day09 - part1 - two tiles"):
    assertEquals(part1(Vector(Tile(0, 0), Tile(10, 10))), 121L)

  test("Day09 - part1 - sample"):
    assertEquals(part1(readFile("inputs/Day09Test.txt")), 50L)

  test("Day09 - part1 - real"):
    assertEquals(part1(readFile("inputs/Day09.txt")), 4777967538L)

end Day09Test