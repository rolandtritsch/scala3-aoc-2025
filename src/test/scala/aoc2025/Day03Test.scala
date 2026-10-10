package aoc2025

import aoc2025.Day03.*

class Day03Test extends munit.FunSuite:

  test("Day03 - maxJoltage - sample banks"):
    assertEquals(maxJoltage("987654321111111"), 98)
    assertEquals(maxJoltage("811111111111119"), 89)
    assertEquals(maxJoltage("234234234234278"), 78)
    assertEquals(maxJoltage("818181911112111"), 92)

  test("Day03 - maxJoltage - single pair"):
    assertEquals(maxJoltage("12"), 12)
    assertEquals(maxJoltage("21"), 21)
    assertEquals(maxJoltage("99"), 99)
    assertEquals(maxJoltage("19"), 19)

  test("Day03 - maxJoltage - ordered pair requires left-to-right order"):
    // 199 -> the pair (1,2) at indices 1,2 is 99; non-decreasing order still picks 99.
    assertEquals(maxJoltage("199"), 99)
    // 91 -> only possible pair is 91 (9 then 1), not 19.
    assertEquals(maxJoltage("91"), 91)

  test("Day03 - readFile - sample"):
    assertEquals(
      readFile("inputs/Day03Test.txt"),
      Seq("987654321111111", "811111111111119", "234234234234278", "818181911112111"),
    )

  test("Day03 - part1 - sample"):
    assertEquals(part1(readFile("inputs/Day03Test.txt")), 357L)

  test("Day03 - part1 - single-line boundaries"):
    assertEquals(part1(Seq("12")), 12L)
    assertEquals(part1(Seq("21")), 21L)
    assertEquals(part1(Seq("99")), 99L)

  test("Day03 - part1 - real"):
    val banks = readFile("inputs/Day03.txt")
    assertEquals(banks.size, 200)
    assertEquals(part1(banks), 17207L)

end Day03Test