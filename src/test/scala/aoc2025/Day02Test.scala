package aoc2025

import aoc2025.Day02.*

class Day02Test extends munit.ScalaCheckSuite:
  val ignore = new munit.Tag("ignore")

  test("Day02 - readFile - test"):
    val obtained = readFile("inputs/Day02Test.txt")
    assertEquals(obtained.size, 11)
    assertEquals(obtained.head, (11L, 22L))
    assertEquals(obtained.last, (2121212118L, 2121212124L))

  test("Day02 - readFile"):
    val obtained = readFile("inputs/Day02.txt")
    assertEquals(obtained.size, 34)
    assert(obtained.forall((start, end) => start > 0 && start <= end))

  test("Day02 - part1 - test"):
    val input = readFile("inputs/Day02Test.txt")
    assertEquals(part1(input), 1227775554L)

  test("Day02 - part1 - boundaries"):
    assertEquals(part1(Seq((11L, 22L))), 33L)
    assertEquals(part1(Seq((100L, 101L))), 0L)
    assertEquals(part1(Seq((1010L, 1010L))), 1010L)

  test("Day02 - part1"):
    val input = readFile("inputs/Day02.txt")
    assertEquals(part1(input), 44487518055L)

  test("Day02 - part2 - test".tag(ignore)):
    val input = readFile("inputs/Day02Test.txt")
    assertEquals(part2(input), 0L)

end Day02Test
