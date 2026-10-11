package aoc2025

import aoc2025.Day10.*

class Day10Test extends munit.FunSuite:

  test("Day10 - readFile - sample"):
    val machines = readFile("inputs/Day10Test.txt")
    assertEquals(machines.size, 3)
    assertEquals(machines(0), Machine(target = 0b0110, buttons = Vector(0b1000, 0b1010, 0b0100, 0b1100, 0b0101, 0b0011)))
    assertEquals(machines(1).target, 0b1000)
    assertEquals(machines(2).target, 0b101110)

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

end Day10Test