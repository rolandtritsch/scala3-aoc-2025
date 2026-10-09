package aoc2025

import aoc2025.Day01.*

class Day01Test extends munit.ScalaCheckSuite:
  val ignore = new munit.Tag("ignore")

  test("Day01 - readFile - sample"):
    assertEquals(
      readFile("inputs/Day01Test.txt"),
      Seq(
        Rotation('L', 68),
        Rotation('L', 30),
        Rotation('R', 48),
        Rotation('L', 5),
        Rotation('R', 60),
        Rotation('L', 55),
        Rotation('L', 1),
        Rotation('L', 99),
        Rotation('R', 14),
        Rotation('L', 82),
      ),
    )

  test("Day01 - readFile - real"):
    val rotations = readFile("inputs/Day01.txt")
    assertEquals(rotations.size, 4498)
    assertEquals(rotations.head, Rotation('L', 6))
    assertEquals(rotations.last, Rotation('R', 25))

  test("Day01 - part1 - sample"):
    assertEquals(part1(readFile("inputs/Day01Test.txt")), 3)

  test("Day01 - part1 - wraparound and complete turns"):
    assertEquals(part1(Seq(Rotation('L', 50), Rotation('R', 100), Rotation('L', 101), Rotation('R', 1))), 3)
    assertEquals(part1(Seq(Rotation('R', 1000))), 0)

  test("Day01 - part1 - real"):
    assertEquals(part1(readFile("inputs/Day01.txt")), 1129)

  test("Day01 - part2 - out of scope".tag(ignore)):
    assertEquals(part2(readFile("inputs/Day01Test.txt")), 6)
end Day01Test
