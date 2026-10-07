package aoc2025

import aoc2025.Day00.*

class Day00Test extends munit.ScalaCheckSuite:
  val only = new munit.Tag("only")
  val ignore = new munit.Tag("ignore")

  test("Day00 - dummy".tag(ignore)):
    assert(true)

  test("Day00 - fib - base cases"):
    assertEquals(fib(0), BigInt(0))
    assertEquals(fib(1), BigInt(1))
    assertEquals(fib(2), BigInt(1))

  test("Day00 - fib - small"):
    assertEquals(fib(10), BigInt(55))
    assertEquals(fib(20), BigInt(6765))

  test("Day00 - readFile - test"):
    val obtained = readFile("inputs/Day00Test.txt")
    assertEquals(obtained, Seq(10, 1000))

  test("Day00 - readFile"):
    val obtained = readFile("inputs/Day00.txt")
    assertEquals(obtained, Seq(10, 1000))

  test("Day00 - part1 - test"):
    val input = readFile("inputs/Day00Test.txt")
    val obtained = part1(input)
    assertEquals(obtained, BigInt(55))

  test("Day00 - part1"):
    val input = readFile("inputs/Day00.txt")
    val obtained = part1(input)
    assertEquals(obtained, BigInt(55))

  test("Day00 - part2 - test"):
    val input = readFile("inputs/Day00Test.txt")
    val obtained = part2(input)
    assertEquals(
      obtained,
      BigInt(
        "43466557686937456435688527675040625802564660517371780402481729089536555417949051890403879840079255169295922593080322634775209689623239873322471161642996440906533187938298969649928516003704476137795166849228875"
      ),
    )

  test("Day00 - part2"):
    val input = readFile("inputs/Day00.txt")
    val obtained = part2(input)
    assertEquals(
      obtained,
      BigInt(
        "43466557686937456435688527675040625802564660517371780402481729089536555417949051890403879840079255169295922593080322634775209689623239873322471161642996440906533187938298969649928516003704476137795166849228875"
      ),
    )

end Day00Test
