package aoc2025

import aoc2025.Day06.*

class Day06Test extends munit.ScalaCheckSuite:
  val ignore = new munit.Tag("ignore")

  test("Day06 - readFile - test"):
    val obtained = readFile("inputs/Day06Test.txt")
    assertEquals(
      obtained,
      Seq(
        WorksheetProblem(Seq(BigInt(123), BigInt(45), BigInt(6)), '*'),
        WorksheetProblem(Seq(BigInt(328), BigInt(64), BigInt(98)), '+'),
        WorksheetProblem(Seq(BigInt(51), BigInt(387), BigInt(215)), '*'),
        WorksheetProblem(Seq(BigInt(64), BigInt(23), BigInt(314)), '+'),
      ),
    )

  test("Day06 - readFile"):
    val obtained = readFile("inputs/Day06.txt")
    assertEquals(obtained.size, 1000)

  test("Day06 - part1 - test"):
    val input = readFile("inputs/Day06Test.txt")
    assertEquals(part1(input), BigInt(4277556))

  test("Day06 - part1"):
    val input = readFile("inputs/Day06.txt")
    assertEquals(part1(input), BigInt("6378679666679"))

  test("Day06 - part2 - test".tag(ignore)):
    val input = readFile("inputs/Day06Test.txt")
    assertEquals(part2(input), 0)

end Day06Test
