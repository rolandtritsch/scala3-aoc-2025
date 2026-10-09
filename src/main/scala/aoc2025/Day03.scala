package aoc2025

import com.typesafe.scalalogging.Logger
import scala.io.Source

/** Day 3: find the largest two-digit joltage obtainable from each ordered battery bank.
  *
  * The first battery must precede the second. As the bank is traversed from left to right,
  * retain the greatest digit seen before each candidate second battery. That digit gives the
  * best possible tens place for that candidate; take the greatest resulting value. Each bank
  * requires O(n) time and O(1) additional space, where n is its number of batteries.
  */
object Day03:
  val logger: Logger = Logger(this.getClass.getName)

  /** Read banks of battery ratings from a classpath resource such as `inputs/Day03.txt`.
    *
    * Each nonempty line is one bank, kept in its original order so battery positions are
    * preserved. Banks need at least two batteries with ratings between 1 and 9.
    *
    * @return the battery banks in input order
    */
  def readFile(filename: String): Seq[String] =
    require(filename.nonEmpty, "filename.nonEmpty")
    val source = Source.fromResource(filename)
    try
      source.getLines().toVector.map { bank =>
        require(bank.length >= 2 && bank.forall(c => c >= '1' && c <= '9'), s"Invalid battery bank: $bank")
        bank
      }
    finally source.close()
  end readFile

  /** Sum the maximum joltage produced by selecting exactly two ordered batteries per bank.
    *
    * For each possible second battery, combine its digit with the highest preceding digit.
    * Keeping the running maximum of preceding digits considers every valid pair without
    * rearranging batteries or scanning the bank twice for each choice.
    *
    * @return the total maximum output joltage across all banks
    */
  def part1(banks: Seq[String]): Long =
    banks.iterator.map { bank =>
      require(bank.length >= 2 && bank.forall(c => c >= '1' && c <= '9'), s"Invalid battery bank: $bank")
      val (_, best) = bank.tail.foldLeft((bank.head.asDigit, 0)) { case ((first, maximum), digit) =>
        (first max digit.asDigit, maximum max (10 * first + digit.asDigit))
      }
      best.toLong
    }.sum
  end part1

  /** Part 2 selects twelve batteries per bank and is out of scope for this part-1 solution. */
  def part2(banks: Seq[String]): Long = ???
end Day03
