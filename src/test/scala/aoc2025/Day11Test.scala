package aoc2025

import aoc2025.Day11.*

class Day11Test extends munit.FunSuite:

  test("Day11 - readFile - sample"):
    val wiring = readFile("inputs/Day11Test.txt")
    assertEquals(wiring("you"), Vector("bbb", "ccc"))
    assertEquals(wiring("bbb"), Vector("ddd", "eee"))
    assertEquals(wiring("hhh"), Vector("ccc", "fff", "iii"))
    assertEquals(wiring("iii"), Vector("out"))
    assertEquals(wiring.size, 10)

  test("Day11 - part1 - sample"):
    assertEquals(part1(readFile("inputs/Day11Test.txt")), 5L)

  test("Day11 - part1 - direct single edge"):
    assertEquals(part1(Map("you" -> Vector("out"))), 1L)

  test("Day11 - part1 - fork then converge"):
    // you -> a -> out and you -> b -> out: two distinct paths.
    assertEquals(
      part1(Map("you" -> Vector("a", "b"), "a" -> Vector("out"), "b" -> Vector("out"))),
      2L,
    )

  test("Day11 - part1 - dead end device"):
    // a device with no path to out contributes nothing.
    assertEquals(
      part1(Map("you" -> Vector("a", "b"), "a" -> Vector("dead"), "b" -> Vector("out"))),
      1L,
    )

  test("Day11 - part1 - real"):
    assertEquals(part1(readFile("inputs/Day11.txt")), 543L)

end Day11Test
