package aoc2025

@main
def solve() =

  val input00 = Day00.readFile("inputs/Day00.txt")
  println(s"Day00 - part1: ${Day00.part1(input00)}")
  println(s"Day00 - part2: ${Day00.part2(input00)}")

  val input01 = Day01.readFile("inputs/Day01.txt")
  println(s"Day01 - part1: ${Day01.part1(input01)}")

  val input02 = Day02.readFile("inputs/Day02.txt")
  println(s"Day02 - part1: ${Day02.part1(input02)}")

end solve
