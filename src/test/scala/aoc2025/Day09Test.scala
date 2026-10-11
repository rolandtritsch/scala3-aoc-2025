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

  test("Day09 - part2 - sample"):
    assertEquals(part2(readFile("inputs/Day09Test.txt")), 24L)

  test("Day09 - part2 - rectangular loop"):
    assertEquals(part2(Vector(Tile(0, 0), Tile(4, 0), Tile(4, 3), Tile(0, 3))), 20L)

  test("Day09 - part2 - concave loop rejects out-of-region rectangle"):
    val tiles = readFile("inputs/Day09Test.txt")
    assert(part2(tiles) < part1(tiles))
    assertEquals(part2(tiles), 24L)

  test("Day09 - part2 - real"):
    assertEquals(part2(readFile("inputs/Day09.txt")), 1439894345L)

end Day09Test
