package aoc2025

import aoc2025.Day03.*

class Day03Test extends munit.ScalaCheckSuite:
  val ignore = new munit.Tag("ignore")

  test("Day03 - readFile - test"):
    val obtained = readFile("inputs/Day03Test.txt")
    assertEquals(
      obtained,
      Seq("987654321111111", "811111111111119", "234234234234278", "818181911112111"),
    )

  test("Day03 - readFile"):
    val obtained = readFile("inputs/Day03.txt")
    assertEquals(obtained.size, 200)
    assert(
      obtained
        .forall(bank => bank.length >= 2 && bank.forall(digit => digit >= '1' && digit <= '9'))
    )

  test("Day03 - part1 - test"):
    val input = readFile("inputs/Day03Test.txt")
    assertEquals(part1(input), 357L)

  test("Day03 - part1 - order and boundaries"):
    assertEquals(part1(Seq("12345")), 45L)
    assertEquals(part1(Seq("811")), 81L)
    assertEquals(part1(Seq("919")), 99L)

  test("Day03 - part1"):
    val input = readFile("inputs/Day03.txt")
    assertEquals(part1(input), 17207L)

  test("Day03 - part2 - test".tag(ignore)):
    val input = readFile("inputs/Day03Test.txt")
    assertEquals(part2(input), 0L)

end Day03Test
