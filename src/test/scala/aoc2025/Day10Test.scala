package aoc2025

import aoc2025.Day10.*

class Day10Test extends munit.ScalaCheckSuite:
  val ignore = new munit.Tag("ignore")

  test("Day10 - readFile - test"):
    val obtained = readFile("inputs/Day10Test.txt")
    assertEquals(
      obtained,
      Seq(
        Machine(6, Vector(8, 10, 4, 12, 5, 3)),
        Machine(8, Vector(29, 12, 17, 7, 30)),
        Machine(46, Vector(31, 25, 55, 6)),
      ),
    )

  test("Day10 - readFile"):
    val obtained = readFile("inputs/Day10.txt")
    assertEquals(obtained.length, 163)

  test("Day10 - part1 - test"):
    val input = readFile("inputs/Day10Test.txt")
    assertEquals(part1(input), 7)

  test("Day10 - part1"):
    val input = readFile("inputs/Day10.txt")
    assertEquals(part1(input), 417)

  test("Day10 - part2 - test".tag(ignore)):
    val input = readFile("inputs/Day10Test.txt")
    assertEquals(part2(input), 0)

end Day10Test
