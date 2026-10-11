package aoc2025

import aoc2025.Day10.*

class Day10Test extends munit.FunSuite:

  test("Day10 - readFile - sample"):
    val machines = readFile("inputs/Day10Test.txt")
    assertEquals(machines.size, 3)
    assertEquals(
      machines(0),
      Machine(
        target = 0b0110,
        buttons = Vector(0b1000, 0b1010, 0b0100, 0b1100, 0b0101, 0b0011),
        joltages = Vector(3, 5, 4, 7),
      ),
    )
    assertEquals(machines(1).target, 0b1000)
    assertEquals(machines(1).joltages, Vector(7, 5, 12, 7, 2))
    assertEquals(machines(2).target, 0b101110)
    assertEquals(machines(2).joltages, Vector(10, 11, 11, 5, 10, 5))

  test("Day10 - part1 - single light on"):
    // one light, one button toggling it -> a single press turns it on.
    assertEquals(part1(Machine(target = 1, buttons = Vector(1))), 1)

  test("Day10 - part1 - target already off"):
    // no lights need to be on, press nothing.
    assertEquals(part1(Machine(target = 0, buttons = Vector(1, 2, 3))), 0)

  test("Day10 - part1 - sample machine minima"):
    val machines = readFile("inputs/Day10Test.txt")
    assertEquals(part1(machines(0)), 2)
    assertEquals(part1(machines(1)), 3)
    assertEquals(part1(machines(2)), 2)

  test("Day10 - totalPresses - sample"):
    assertEquals(totalPresses(readFile("inputs/Day10Test.txt")), 7)

  test("Day10 - totalPresses - real"):
    assertEquals(totalPresses(readFile("inputs/Day10.txt")), 417)

  test("Day10 - part2 - single counter single button"):
    // one counter, one button covering it: press exactly its joltage times.
    assertEquals(part2(Machine(target = 0, buttons = Vector(1), joltages = Vector(7))), 7)

  test("Day10 - part2 - multi-counter button saves presses"):
    // one counter needs 5 and another needs 5; a shared button covers both in 5 presses.
    assertEquals(part2(Machine(target = 0, buttons = Vector(0b11), joltages = Vector(5, 5))), 5)

  test("Day10 - part2 - sample machine minima"):
    val machines = readFile("inputs/Day10Test.txt")
    assertEquals(part2(machines(0)), 10)
    assertEquals(part2(machines(1)), 12)
    assertEquals(part2(machines(2)), 11)

  test("Day10 - part2 - sample total"):
    assertEquals(totalJoltagePresses(readFile("inputs/Day10Test.txt")), 33L)

  test("Day10 - part2 - real"):
    assertEquals(totalJoltagePresses(readFile("inputs/Day10.txt")), 16765L)

end Day10Test
