package aoc2025

import aoc2025.Day11.*

class Day11Test extends munit.ScalaCheckSuite:
  val ignore = new munit.Tag("ignore")

  test("Day11 - readFile - test"):
    val obtained = readFile("inputs/Day11Test.txt")
    assertEquals(
      obtained,
      Seq(
        Device("aaa", Vector("you", "hhh")),
        Device("you", Vector("bbb", "ccc")),
        Device("bbb", Vector("ddd", "eee")),
        Device("ccc", Vector("ddd", "eee", "fff")),
        Device("ddd", Vector("ggg")),
        Device("eee", Vector("out")),
        Device("fff", Vector("out")),
        Device("ggg", Vector("out")),
        Device("hhh", Vector("ccc", "fff", "iii")),
        Device("iii", Vector("out")),
      ),
    )

  test("Day11 - readFile"):
    val obtained = readFile("inputs/Day11.txt")
    assertEquals(obtained.length, 581)

  test("Day11 - part1 - test"):
    val input = readFile("inputs/Day11Test.txt")
    assertEquals(part1(input), BigInt(5))

  test("Day11 - part1"):
    val input = readFile("inputs/Day11.txt")
    assertEquals(part1(input), BigInt(543))

  test("Day11 - part2 - test".tag(ignore)):
    val input = readFile("inputs/Day11Test.txt")
    assertEquals(part2(input), BigInt(0))

end Day11Test
