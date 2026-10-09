package aoc2025

import aoc2025.Day06.*

class Day06Test extends munit.ScalaCheckSuite:
  val ignore = new munit.Tag("ignore")

  test("Day06 - readFile - sample"):
    assertEquals(
      readFile("inputs/Day06Test.txt"),
      Vector("123 328  51 64 ", " 45 64  387 23 ", "  6 98  215 314", "*   +   *   +  "),
    )

  test("Day06 - readFile - real"):
    val rows = readFile("inputs/Day06.txt")
    assertEquals(rows.size, 5)
    assert(rows.last.contains('*') && rows.last.contains('+'))

  test("Day06 - part1 - sample"):
    assertEquals(part1(readFile("inputs/Day06Test.txt")), BigInt(4277556))

  test("Day06 - part1 - ragged alignment and blank separators"):
    assertEquals(part1(Vector("12   3", " 4  25", "*    +")), BigInt(76))

  test("Day06 - part1 - real"):
    assertEquals(part1(readFile("inputs/Day06.txt")), BigInt("6378679666679"))

  test("Day06 - part2 - out of scope".tag(ignore)):
    assertEquals(part2(readFile("inputs/Day06Test.txt")), BigInt(3263827))

end Day06Test
