package aoc2025

import aoc2025.Day06.*

class Day06Test extends munit.FunSuite:

  test("Day06 - readFile - sample"):
    assertEquals(
      readFile("inputs/Day06Test.txt"),
      Seq(
        Problem('*', Seq(123L, 45L, 6L)),
        Problem('+', Seq(328L, 64L, 98L)),
        Problem('*', Seq(51L, 387L, 215L)),
        Problem('+', Seq(64L, 23L, 314L)),
      ),
    )

  test("Day06 - readFile - real"):
    val problems = readFile("inputs/Day06.txt")
    assertEquals(problems.size, 1000)
    assert(problems.forall(p => p.operator == '+' || p.operator == '*'))
    assert(problems.forall(_.numbers.nonEmpty))
    assert(problems.forall(_.numbers.size == 4))

  test("Day06 - evaluate - single multiplication factor"):
    assertEquals(evaluate(Problem('*', Seq(7L))), 7L)

  test("Day06 - evaluate - multiplication combines all factors"):
    assertEquals(evaluate(Problem('*', Seq(123L, 45L, 6L))), 33210L)

  test("Day06 - evaluate - single addition addend"):
    assertEquals(evaluate(Problem('+', Seq(7L))), 7L)

  test("Day06 - evaluate - addition combines all addends"):
    assertEquals(evaluate(Problem('+', Seq(328L, 64L, 98L))), 490L)

  test("Day06 - part1 - single problem"):
    assertEquals(part1(Seq(Problem('+', Seq(64L, 23L, 314L)))), 401L)

  test("Day06 - part1 - sample"):
    assertEquals(part1(readFile("inputs/Day06Test.txt")), 4277556L)

  test("Day06 - part1 - real"):
    assertEquals(part1(readFile("inputs/Day06.txt")), 6378679666679L)

  test("Day06 - readFilePart2 - sample"):
    assertEquals(
      readFilePart2("inputs/Day06Test.txt"),
      Seq(
        Problem('+', Seq(4L, 431L, 623L)),
        Problem('*', Seq(175L, 581L, 32L)),
        Problem('+', Seq(8L, 248L, 369L)),
        Problem('*', Seq(356L, 24L, 1L)),
      ),
    )

  test("Day06 - readFilePart2 - real"):
    val problems = readFilePart2("inputs/Day06.txt")
    assertEquals(problems.size, 1000)
    assert(problems.forall(p => p.operator == '+' || p.operator == '*'))
    assert(problems.forall(_.numbers.nonEmpty))

  test("Day06 - part2 - sample"):
    assertEquals(part2(readFilePart2("inputs/Day06Test.txt")), 3263827L)

  test("Day06 - part2 - real"):
    assertEquals(part2(readFilePart2("inputs/Day06.txt")), 11494432585168L)

end Day06Test
