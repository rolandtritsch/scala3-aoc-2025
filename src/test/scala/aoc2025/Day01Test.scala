package aoc2025

import aoc2025.Day01.*

class Day01Test extends munit.ScalaCheckSuite:
  val only = new munit.Tag("only")
  val ignore = new munit.Tag("ignore")

  test("Day01 - part1 - example from problem statement"):
    val input = readFile("inputs/Day01Test.txt")
    val obtained = part1(input)
    assertEquals(obtained, 3) // From the example in the problem statement

  test("Day01 - readFile - test"):
    val obtained = readFile("inputs/Day01Test.txt")
    val expected = Seq(
      Rotation('L', 68),
      Rotation('L', 30),
      Rotation('R', 48),
      Rotation('L', 5),
      Rotation('R', 60),
      Rotation('L', 55),
      Rotation('L', 1),
      Rotation('L', 99),
      Rotation('R', 14),
      Rotation('L', 82)
    )
    assertEquals(obtained, expected)

  test("Day01 - readFile"):
    val obtained = readFile("inputs/Day01.txt")
    assertEquals(obtained.length, 4498) // Known from checking the input file

  test("Day01 - part1"):
    val input = readFile("inputs/Day01.txt")
    val obtained = part1(input)
    // TODO: Replace with actual answer after running
    assertEquals(obtained, 1129)

  test("Day01 - part2 - test").tag(ignore):
    val input = readFile("inputs/Day01Test.txt")
    val obtained = part2(input) // Will throw NotImplementedError until implemented
    assertEquals(obtained, 0) // Placeholder

  test("Day01 - part2").tag(ignore):
    val input = readFile("inputs/Day01.txt")
    val obtained = part2(input) // Will throw NotImplementedError until implemented
    assertEquals(obtained, 0) // Placeholder

end Day01Test