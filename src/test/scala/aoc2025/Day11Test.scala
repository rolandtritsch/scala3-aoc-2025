package aoc2025

import aoc2025.Day11.*

class Day11Test extends munit.FunSuite:

  test("Day11 - readFile - sample"):
    val wiring = readFile("inputs/Day11Test.txt")
    assertEquals(wiring("you"), Vector("bbb", "ccc"))
    assertEquals(wiring("bbb"), Vector("ddd", "eee"))
    assertEquals(wiring("hhh"), Vector("ccc", "fff", "iii"))
    assertEquals(wiring("iii"), Vector("out"))
    assertEquals(wiring.size, 10)

  test("Day11 - part1 - sample"):
    assertEquals(part1(readFile("inputs/Day11Test.txt")), 5L)

  test("Day11 - part1 - direct single edge"):
    assertEquals(part1(Map("you" -> Vector("out"))), 1L)

  test("Day11 - part1 - fork then converge"):
    // you -> a -> out and you -> b -> out: two distinct paths.
    assertEquals(
      part1(Map("you" -> Vector("a", "b"), "a" -> Vector("out"), "b" -> Vector("out"))),
      2L,
    )

  test("Day11 - part1 - dead end device"):
    // a device with no path to out contributes nothing.
    assertEquals(
      part1(Map("you" -> Vector("a", "b"), "a" -> Vector("dead"), "b" -> Vector("out"))),
      1L,
    )

  test("Day11 - part1 - real"):
    assertEquals(part1(readFile("inputs/Day11.txt")), 543L)

  // Part two sample wiring (different devices from the part-one sample), given inline so the
  // single committed sample file keeps serving part one.
  private val part2Sample = Map(
    "svr" -> Vector("aaa", "bbb"),
    "aaa" -> Vector("fft"),
    "fft" -> Vector("ccc"),
    "bbb" -> Vector("tty"),
    "tty" -> Vector("ccc"),
    "ccc" -> Vector("ddd", "eee"),
    "ddd" -> Vector("hub"),
    "hub" -> Vector("fff"),
    "eee" -> Vector("dac"),
    "dac" -> Vector("fff"),
    "fff" -> Vector("ggg", "hhh"),
    "ggg" -> Vector("out"),
    "hhh" -> Vector("out"),
  )

  test("Day11 - part2 - sample"):
    assertEquals(part2(part2Sample), 2L)

  test("Day11 - part2 - required devices in order"):
    assertEquals(
      part2(Map("svr" -> Vector("dac"), "dac" -> Vector("fft"), "fft" -> Vector("out"))),
      1L,
    )

  test("Day11 - part2 - required devices reversed order"):
    assertEquals(
      part2(Map("svr" -> Vector("fft"), "fft" -> Vector("dac"), "dac" -> Vector("out"))),
      1L,
    )

  test("Day11 - part2 - missing one required device"):
    // svr -> dac -> out skips fft entirely.
    assertEquals(part2(Map("svr" -> Vector("dac"), "dac" -> Vector("out"))), 0L)

  test("Day11 - part2 - direct edge with no required device"):
    assertEquals(part2(Map("svr" -> Vector("out"))), 0L)

  test("Day11 - part2 - real"):
    assertEquals(part2(readFile("inputs/Day11.txt")), 479511112939968L)

end Day11Test
