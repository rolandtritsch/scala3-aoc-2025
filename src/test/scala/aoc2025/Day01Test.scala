package aoc2025

import aoc2025.Day01.*

class Day01Test extends munit.ScalaCheckSuite:
  val only = new munit.Tag("only")
  val ignore = new munit.Tag("ignore")

  test("Day01 - processRotation - basic"):
    assertEquals(processRotation(50, "L68"), (82, false))
    assertEquals(processRotation(82, "L30"), (52, false))
    assertEquals(processRotation(52, "R48"), (0, true))
    assertEquals(processRotation(0, "L5"), (95, false))
    assertEquals(processRotation(95, "R60"), (55, false))
    assertEquals(processRotation(55, "L55"), (0, true))
    assertEquals(processRotation(0, "L1"), (99, false))
    assertEquals(processRotation(99, "L99"), (0, true))
    assertEquals(processRotation(0, "R14"), (14, false))
    assertEquals(processRotation(14, "L82"), (32, false))

  test("Day01 - readFile - test"):
    val obtained = readFile("inputs/Day01Test.txt")
    val expected = Seq("L68", "L30", "R48", "L5", "R60", "L55", "L1", "L99", "R14", "L82")
    assertEquals(obtained, expected)

  test("Day01 - part1 - test"):
    val input = readFile("inputs/Day01Test.txt")
    val obtained = part1(input)
    assertEquals(obtained, 3)

  test("Day01 - part1"):
    val input = readFile("inputs/Day01.txt")
    val obtained = part1(input)
    assertEquals(obtained, 261)

  test(("Day01 - part2 - test").tag(ignore)):
    val input = readFile("inputs/Day01Test.txt")
    val obtained = part2(input)
    assertEquals(obtained, 0) // Placeholder

  test(("Day01 - part2").tag(ignore)):
    val input = readFile("inputs/Day01.txt")
    val obtained = part2(input)
    assertEquals(obtained, 0) // Placeholder

end Day01Test
