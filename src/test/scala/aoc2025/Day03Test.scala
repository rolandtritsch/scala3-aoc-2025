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

  test("Day03 - maxJoltage12 - sample banks"):
    assertEquals(maxJoltage12("987654321111111"), 987654321111L)
    assertEquals(maxJoltage12("811111111111119"), 811111111119L)
    assertEquals(maxJoltage12("234234234234278"), 434234234278L)
    assertEquals(maxJoltage12("818181911112111"), 888911112111L)

  test("Day03 - maxJoltage12 - exactly twelve digits"):
    assertEquals(maxJoltage12("999999999999"), 999999999999L)
    assertEquals(maxJoltage12("111111111111"), 111111111111L)

  test("Day03 - maxJoltage12 - keeps largest in order, drops least significant"):
    // All nines and ones: the twelve nines win, dropping the twelve trailing ones.
    assertEquals(maxJoltage12("999999999999111111111111"), 999999999999L)
    // A larger digit near the end replaces smaller leading digits.
    assertEquals(maxJoltage12("1111111111119"), 111111111119L)

  test("Day03 - maxJoltage12 - descending must keep the largest prefix"):
    assertEquals(maxJoltage12("987654321111111"), 987654321111L)

  test("Day03 - part2 - sample"):
    assertEquals(part2(readFile("inputs/Day03Test.txt")), 3121910778619L)

  test("Day03 - part2 - real"):
    val banks = readFile("inputs/Day03.txt")
    assertEquals(banks.size, 200)
    assertEquals(part2(banks), 170997883706617L)

end Day03Test
