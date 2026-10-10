package aoc2025

import aoc2025.Day01.*

class Day01Test extends munit.ScalaCheckSuite:
  val only = new munit.Tag("only")
  val ignore = new munit.Tag("ignore")

  test("Day01 - advance - right movement and wrap"):
    assertEquals(advance(11, Rotation(Direction.R, 8)), 19)
    assertEquals(advance(99, Rotation(Direction.R, 1)), 0)
    assertEquals(advance(50, Rotation(Direction.R, 1000)), 50)

  test("Day01 - advance - left movement and wrap"):
    assertEquals(advance(0, Rotation(Direction.L, 1)), 99)
    assertEquals(advance(5, Rotation(Direction.L, 10)), 95)
    assertEquals(advance(50, Rotation(Direction.L, 68)), 82)

  test("Day01 - readFile - sample"):
    assertEquals(
      readFile("inputs/Day01Test.txt"),
      Seq(
        Rotation(Direction.L, 68),
        Rotation(Direction.L, 30),
        Rotation(Direction.R, 48),
        Rotation(Direction.L, 5),
        Rotation(Direction.R, 60),
        Rotation(Direction.L, 55),
        Rotation(Direction.L, 1),
        Rotation(Direction.L, 99),
        Rotation(Direction.R, 14),
        Rotation(Direction.L, 82),
      ),
    )

  test("Day01 - readFile - real"):
    val rotations = readFile("inputs/Day01.txt")
    assertEquals(rotations.size, 4498)
    assertEquals(rotations.head, Rotation(Direction.L, 6))
    assertEquals(rotations.last, Rotation(Direction.R, 25))

  test("Day01 - part1 - sample"):
    assertEquals(part1(readFile("inputs/Day01Test.txt")), 3)

  test("Day01 - part1 - boundaries"):
    assertEquals(part1(Seq.empty), 0)
    assertEquals(part1(Seq(Rotation(Direction.L, 50))), 1)
    assertEquals(part1(Seq(Rotation(Direction.R, 50))), 1)
    assertEquals(part1(Seq(Rotation(Direction.R, 1000))), 0)
    assertEquals(part1(Seq(Rotation(Direction.L, 50), Rotation(Direction.R, 100), Rotation(Direction.L, 101), Rotation(Direction.R, 1))), 3)

  test("Day01 - part1 - real"):
    assertEquals(part1(readFile("inputs/Day01.txt")), 1129)

end Day01Test
