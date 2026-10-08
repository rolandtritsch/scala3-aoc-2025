package aoc2025

import aoc2025.Day02.*

class Day02Test extends munit.ScalaCheckSuite:
  val ignore = new munit.Tag("ignore")

  test("Day02 - readFile - test"):
    val obtained = readFile("inputs/Day02Test.txt")
    assertEquals(obtained.size, 11)
    assertEquals(obtained.head, (BigInt(11), BigInt(22)))

  test("Day02 - readFile"):
    val obtained = readFile("inputs/Day02.txt")
    assertEquals(obtained.size, 34)
    assert(obtained.forall((start, end) => start >= 1 && start <= end))

  test("Day02 - part1 - test"):
    val input = readFile("inputs/Day02Test.txt")
    assertEquals(part1(input), BigInt(1227775554L))

  test("Day02 - part1"):
    val input = readFile("inputs/Day02.txt")
    assertEquals(part1(input), BigInt("44487518055"))

  test("Day02 - part2 - test".tag(ignore)):
    val input = readFile("inputs/Day02Test.txt")
    assertEquals(part2(input), BigInt(0))

end Day02Test
