package aoc2025

import aoc2025.Day05.*

class Day05Test extends munit.FunSuite:

  test("Day05 - readFile - sample"):
    assertEquals(
      readFile("inputs/Day05Test.txt"),
      Database(
        Seq(IdRange(3, 5), IdRange(10, 14), IdRange(16, 20), IdRange(12, 18)),
        Seq(1L, 5L, 8L, 11L, 17L, 32L),
      ),
    )

  test("Day05 - readFile - real"):
    val db = readFile("inputs/Day05.txt")
    assertEquals(db.ranges.size, 182)
    assertEquals(db.available.size, 1000)
    assert(db.ranges.forall(r => r.first <= r.last))
    assert(db.available.forall(_ >= 0L))

  test("Day05 - isFresh - boundary IDs and overlapping ranges"):
    val ranges = Seq(IdRange(3, 5), IdRange(10, 14), IdRange(16, 20), IdRange(12, 18))
    assert(isFresh(3, ranges)) // first boundary of the first range
    assert(isFresh(5, ranges)) // last boundary of the first range
    assert(isFresh(10, ranges)) // first boundary of 10-14
    assert(isFresh(14, ranges)) // last boundary of 10-14
    assert(isFresh(15, ranges)) // inside 12-18 only, in the 14-16 gap
    assert(isFresh(17, ranges)) // inside 16-20 and 12-18
    assert(isFresh(20, ranges)) // last boundary of 16-20
    assert(!isFresh(2, ranges))
    assert(!isFresh(9, ranges))
    assert(!isFresh(21, ranges))
    assert(!isFresh(32, ranges))

  test("Day05 - part1 - single-element range"):
    val db = Database(Seq(IdRange(7, 7)), Seq(6L, 7L, 8L))
    assertEquals(part1(db), 1)

  test("Day05 - part1 - no available ID is fresh"):
    val db = Database(Seq(IdRange(1, 5)), Seq(10L, 20L, 30L))
    assertEquals(part1(db), 0)

  test("Day05 - part1 - every available ID is fresh"):
    val db = Database(Seq(IdRange(0, 100)), Seq(1L, 50L, 100L))
    assertEquals(part1(db), 3)

  test("Day05 - part1 - overlapping ranges do not double-count"):
    val db = Database(Seq(IdRange(1, 10), IdRange(5, 15)), Seq(7L, 12L, 20L))
    assertEquals(part1(db), 2)

  test("Day05 - part1 - sample"):
    assertEquals(part1(readFile("inputs/Day05Test.txt")), 3)

  test("Day05 - part1 - real"):
    val db = readFile("inputs/Day05.txt")
    assertEquals(part1(db), 848)

end Day05Test