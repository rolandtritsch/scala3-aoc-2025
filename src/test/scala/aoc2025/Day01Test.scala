package aoc2025

import aoc2025.Day01.*

class Day01Test extends munit.ScalaCheckSuite:
  val ignore = new munit.Tag("ignore")

  test("Day01 - readFile - test"):
    val obtained = readFile("inputs/Day01Test.txt")
    assertEquals(obtained, Seq("L68", "L30", "R48", "L5", "R60", "L55", "L1", "L99", "R14", "L82"))

  test("Day01 - readFile"):
    val obtained = readFile("inputs/Day01.txt")
    assertEquals(obtained.size, 4498)
    assert(obtained.forall(_.matches("[LR]\\d+")))

  test("Day01 - part1 - test"):
    val input = readFile("inputs/Day01Test.txt")
    assertEquals(part1(input), 3)

  test("Day01 - part1"):
    val input = readFile("inputs/Day01.txt")
    assertEquals(part1(input), 1129)

  test("Day01 - part2 - test".tag(ignore)):
    val input = readFile("inputs/Day01Test.txt")
    assertEquals(part2(input), 0)

end Day01Test
