package aoc2025

import com.typesafe.scalalogging.Logger

/** Day03 finds the maximum two-battery joltage from each bank and sums the bank outputs.
  *
  * Battery order is fixed, so the first selected digit is the tens digit and must occur before the
  * selected units digit.
  */
object Day03:
  val logger: Logger = Logger(this.getClass.getName)

  /** Reads each battery bank from a classpath resource.
    *
    * @param filename
    *   resource path such as `inputs/Day03.txt`
    * @return
    *   the banks in input order
    */
  def readFile(filename: String): Seq[String] =
    import scala.io.Source

    require(filename.nonEmpty, "filename.nonEmpty")
    logger.debug(s"filename: ${filename}")

    val source = Source.fromResource(filename)
    try source.getLines().map(_.trim).toSeq
    finally source.close()
    end try
  end readFile

  /** Sums the maximum joltage obtainable from exactly two ordered batteries per bank.
    *
    * For every possible tens-digit position, the best matching units digit is the maximum digit
    * after it. Comparing those candidates handles ties without rearranging any batteries.
    *
    * @param banks
    *   nonempty banks of battery ratings from 1 through 9
    * @return
    *   the sum of the largest two-battery joltage for each bank
    */
  def part1(banks: Seq[String]): Long =
    require(banks.nonEmpty, "banks.nonEmpty")
    logger.debug(s"banks: ${banks.size}")

    banks.foldLeft(0L): (total, bank) =>
      require(bank.length >= 2, "bank.length >= 2")
      require(bank.forall(digit => digit >= '1' && digit <= '9'), "bank contains ratings from 1 through 9")

      val joltage = (0 until (bank.length - 1)).foldLeft(0): (maximum, tensIndex) =>
        val tens = bank(tensIndex) - '0'
        val units = bank.substring(tensIndex + 1).map(_ - '0').max
        math.max(maximum, tens * 10 + units)
      total + joltage
  end part1

  /** Part 2 is outside the scope of the current implementation.
    *
    * @param banks
    *   the battery banks, accepted to match the daily solution API
    * @return
    *   zero as the explicit part-2 placeholder
    */
  def part2(banks: Seq[String]): Long =
    require(banks.nonEmpty, "banks.nonEmpty")
    0L
  end part2

end Day03
