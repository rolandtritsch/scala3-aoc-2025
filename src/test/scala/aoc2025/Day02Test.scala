package aoc2025

import aoc2025.Day02.*

class Day02Test extends munit.FunSuite:

  test("Day02 - isInvalid - repeated exactly twice"):
    assert(isInvalid(11))
    assert(isInvalid(22))
    assert(isInvalid(55))
    assert(isInvalid(99))
    assert(isInvalid(6464))
    assert(isInvalid(123123))
    assert(isInvalid(1010))
    assert(isInvalid(1188511885L))
    assert(isInvalid(222222))
    assert(isInvalid(446446))
    assert(isInvalid(38593859L))

  test("Day02 - isInvalid - odd-length and non-repeated IDs"):
    assert(!isInvalid(1))
    assert(!isInvalid(9))
    assert(!isInvalid(12))
    assert(!isInvalid(101))
    assert(!isInvalid(111))
    assert(!isInvalid(999))
    assert(!isInvalid(1001))
    assert(!isInvalid(565656))
    assert(!isInvalid(2121212121L))

  test("Day02 - isInvalid - longer composed repetitions still split into two equal halves"):
    assert(isInvalid(1111)) // "11" twice
    assert(isInvalid(12121212L)) // "1212" twice

  test("Day02 - readFile - sample ranges"):
    assertEquals(
      readFile("inputs/Day02Test.txt"),
      Seq(
        IdRange(11, 22),
        IdRange(95, 115),
        IdRange(998, 1012),
        IdRange(1188511880L, 1188511890L),
        IdRange(222220, 222224),
        IdRange(1698522, 1698528),
        IdRange(446443, 446449),
        IdRange(38593856L, 38593862L),
        IdRange(565653, 565659),
        IdRange(824824821L, 824824827L),
        IdRange(2121212118L, 2121212124L),
      ),
    )

  test("Day02 - part1 - sample"):
    assertEquals(part1(readFile("inputs/Day02Test.txt")), 1227775554L)

  test("Day02 - part1 - single-ID boundaries"):
    assertEquals(part1(Seq(IdRange(11, 11))), 11L)
    assertEquals(part1(Seq(IdRange(12, 12))), 0L)
    assertEquals(part1(Seq(IdRange(1, 9))), 0L)
    assertEquals(part1(Seq(IdRange(99, 99))), 99L)
    assertEquals(part1(Seq(IdRange(1010, 1010))), 1010L)

  test("Day02 - part1 - real"):
    val ranges = readFile("inputs/Day02.txt")
    assertEquals(ranges.size, 34)
    assertEquals(part1(ranges), 44487518055L)

  test("Day02 - isInvalidPart2 - sequence repeated at least twice"):
    assert(isInvalidPart2(11)) // "1" twice
    assert(isInvalidPart2(111)) // "1" thrice
    assert(isInvalidPart2(99))
    assert(isInvalidPart2(999))
    assert(isInvalidPart2(1010))
    assert(isInvalidPart2(12341234L)) // "1234" twice
    assert(isInvalidPart2(123123123L)) // "123" thrice
    assert(isInvalidPart2(1212121212L)) // "12" five times
    assert(isInvalidPart2(1111111)) // "1" seven times
    assert(isInvalidPart2(565656)) // "56" thrice
    assert(isInvalidPart2(824824824L)) // "824" thrice
    assert(isInvalidPart2(2121212121L)) // "21" five times

  test("Day02 - isInvalidPart2 - non-repeated IDs"):
    assert(!isInvalidPart2(1))
    assert(!isInvalidPart2(12))
    assert(!isInvalidPart2(101))
    assert(!isInvalidPart2(1001))
    assert(!isInvalidPart2(112)) // "1" then "12", not a clean repeated prefix
    assert(!isInvalidPart2(111112)) // longer odd case with a trailing digit

  test("Day02 - part2 - sample"):
    assertEquals(part2(readFile("inputs/Day02Test.txt")), 4174379265L)

  test("Day02 - part2 - single-ID boundaries"):
    assertEquals(part2(Seq(IdRange(111, 111))), 111L)
    assertEquals(part2(Seq(IdRange(12, 12))), 0L)
    assertEquals(part2(Seq(IdRange(1, 9))), 0L)
    assertEquals(part2(Seq(IdRange(1111111, 1111111))), 1111111L)
    assertEquals(part2(Seq(IdRange(565656, 565656))), 565656L)

  test("Day02 - part2 - real"):
    val ranges = readFile("inputs/Day02.txt")
    assertEquals(part2(ranges), 53481866137L)

end Day02Test
