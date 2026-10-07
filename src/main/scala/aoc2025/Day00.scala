package aoc2025

import com.typesafe.scalalogging.Logger

/** Day00 - dummy/template (Fibonacci)
  *
  * Input files (`Day00.txt`, `Day00Test.txt`) contain one integer per line:
  *   - first line: n for part1 (10)
  *   - last line: n for part2 (1000)
  *
  * part1: fib(10) = 55 part2: fib(1000) =
  * 43466557686937456435688527675040625802564660517371780402481729089536555417949051890403879840079255169295922593080322634775209689623239873322471161642996440906533187938298969649928516003704476137795166849228875
  *
  * Uses [[BigInt]] because fib(1000) does not fit into Int/Long. Standard definition: fib(0) = 0,
  * fib(1) = 1.
  */

object Day00:
  val logger: Logger = Logger(this.getClass.getName)

  /** @return fib(n) with fib(0) = 0, fib(1) = 1 */
  def fib(n: Int): BigInt =
    require(n >= 0, "n >= 0")
    logger.debug(s"n: ${n}")

    @scala.annotation.tailrec
    def go(i: Int, a: BigInt, b: BigInt): BigInt = if i >= n then a else go(i + 1, b, a + b)

    go(0, 0, 1)
  end fib

  /** @return the file for the given filename as parsed elements */
  def readFile(filename: String): Seq[Int] =
    import scala.io.Source

    require(filename.nonEmpty, "filename.nonEmpty")
    logger.debug(s"filename: ${filename}")

    val source = Source.fromResource(filename)
    try source.getLines().toSeq.map: line =>
        logger.debug(s"line: ${line}")
        val parsed = line.trim.toInt
        logger.debug(s"parsed: ${parsed}")
        parsed
    finally source.close()
    end try
  end readFile

  /** @return fib(input(0)), i.e. fib(10) = 55 for the dummy input */
  def part1(is: Seq[Int]): BigInt =
    require(is.nonEmpty, "is.nonEmpty")
    logger.debug(s"is: ${is}")

    fib(is.head)
  end part1

  /** @return fib(input.last), i.e. fib(1000) for the dummy input */
  def part2(is: Seq[Int]): BigInt =
    require(is.nonEmpty, "is.nonEmpty")
    logger.debug(s"is: ${is}")

    fib(is.last)
  end part2

end Day00
