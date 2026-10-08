package aoc2025

import aoc2025.Day04.*

class Day04Test extends munit.ScalaCheckSuite:
  val ignore = new munit.Tag("ignore")

  test("Day04 - readFile - test"):
    val obtained = readFile("inputs/Day04Test.txt")
    assertEquals(obtained.size, 10)
    assertEquals(obtained.head, "..@@.@@@@.")
    assertEquals(obtained.last, "@.@.@@@.@.")

  test("Day04 - readFile"):
    val obtained = readFile("inputs/Day04.txt")
    assertEquals(obtained.size, 138)
    assert(obtained.forall(row =>
      row.length == obtained.head.length && row.forall(cell => cell == '@' || cell == '.')
    ))

  test("Day04 - part1 - test"):
    val input = readFile("inputs/Day04Test.txt")
    assertEquals(part1(input), 13)

  test("Day04 - part1 - boundaries"):
    assertEquals(part1(Seq("@")), 1)
    assertEquals(part1(Seq("@@@")), 3)
    assertEquals(part1(Seq("@@@", "@@@", "@@@")), 4)

  test("Day04 - part1"):
    val input = readFile("inputs/Day04.txt")
    assertEquals(part1(input), 1356)

  test("Day04 - part2 - test".tag(ignore)):
    val input = readFile("inputs/Day04Test.txt")
    assertEquals(part2(input), 0)

end Day04Test
