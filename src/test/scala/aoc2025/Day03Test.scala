package aoc2025

import aoc2025.Day03.*

class Day03Test extends munit.ScalaCheckSuite:
  val ignore = new munit.Tag("ignore")

  test("Day03 - readFile - sample"):
    assertEquals(
      readFile("inputs/Day03Test.txt"),
      Seq("987654321111111", "811111111111119", "234234234234278", "818181911112111"),
    )

  test("Day03 - readFile - real"):
    val banks = readFile("inputs/Day03.txt")
    assertEquals(banks.size, 200)
    assertEquals(banks.head, "3223323232423342133321323321133325222233342332323323343713331321434231231232333333232334233323322122")
    assertEquals(banks.last, "1112121122222223222222222222112242222232323212322222213112322622222132213212123253423222223242122232")

  test("Day03 - part1 - sample"):
    assertEquals(part1(readFile("inputs/Day03Test.txt")), 357L)

  test("Day03 - part1 - order and bank boundaries"):
    assertEquals(part1(Seq("98", "89", "91", "19", "12345", "919")), 98L + 89 + 91 + 19 + 45 + 99)

  test("Day03 - part1 - real"):
    assertEquals(part1(readFile("inputs/Day03.txt")), 17207L)

  test("Day03 - part2 - out of scope".tag(ignore)):
    assertEquals(part2(readFile("inputs/Day03Test.txt")), 3121910778619L)
end Day03Test
