package aoc2025

import aoc2025.Day02.*

class Day02Test extends munit.ScalaCheckSuite:
  val ignore = new munit.Tag("ignore")

  test("Day02 - readFile - sample"):
    val ranges = readFile("inputs/Day02Test.txt")
    assertEquals(ranges.size, 11)
    assertEquals(ranges.head, IdRange(11, 22))
    assertEquals(ranges.last, IdRange(2121212118L, 2121212124L))

  test("Day02 - readFile - real"):
    val ranges = readFile("inputs/Day02.txt")
    assertEquals(ranges.size, 34)
    assertEquals(ranges.head, IdRange(7777742220L, 7777814718L))
    assertEquals(ranges.last, IdRange(4743038, 4844422))

  test("Day02 - part1 - sample"):
    assertEquals(part1(readFile("inputs/Day02Test.txt")), BigInt(1227775554))

  test("Day02 - part1 - boundaries and half lengths"):
    assertEquals(part1(Seq(IdRange(1, 10), IdRange(11, 11), IdRange(22, 22))), BigInt(33))
    assertEquals(part1(Seq(IdRange(99, 1010))), BigInt(99 + 1010))
    assertEquals(part1(Seq(IdRange(111, 111), IdRange(1001, 1001), IdRange(1010, 1010))), BigInt(1010))
    assertEquals(part1(Seq(IdRange(11, 99))), BigInt(495))

  test("Day02 - part1 - real"):
    assertEquals(part1(readFile("inputs/Day02.txt")), BigInt("44487518055"))

  test("Day02 - part2 - out of scope".tag(ignore)):
    assertEquals(part2(readFile("inputs/Day02Test.txt")), BigInt("4174379265"))
end Day02Test
