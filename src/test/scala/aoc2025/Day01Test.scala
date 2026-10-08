package aoc2025

class Day01Test extends munit.ScalaCheckSuite:
  val ignore = new munit.Tag("ignore")

  test("Day01 - readFile - test"):
    val obtained = Day01.readFile("inputs/Day01Test.txt")
    assertEquals(obtained, Seq(-68L, -30L, 48L, -5L, 60L, -55L, -1L, -99L, 14L, -82L))

  test("Day01 - readFile"):
    val obtained = Day01.readFile("inputs/Day01.txt")
    assertEquals(obtained.size, 4498)

  test("Day01 - part1 - test"):
    val input = Day01.readFile("inputs/Day01Test.txt")
    assertEquals(Day01.part1(input), 3)

  test("Day01 - part1"):
    val input = Day01.readFile("inputs/Day01.txt")
    assertEquals(Day01.part1(input), 1129)

  test("Day01 - part2 - test".tag(ignore)):
    val input = Day01.readFile("inputs/Day01Test.txt")
    assertEquals(Day01.part2(input), 0)

end Day01Test
