package aoc2025

import com.typesafe.scalalogging.Logger
import scala.io.Source

/** Day03 - Lobby.
  *
  * An offline escalator must be powered from nearby batteries, each labeled with a joltage rating
  * from 1 to 9. The batteries are arranged into banks; each input line is the sequence of ratings
  * for a single bank. Within a bank you turn on exactly two batteries, and the joltage the bank
  * produces is the two-digit number formed by the turned-on batteries in their original order (no
  * rearranging). For part 1 the total output joltage is the sum of the maximum possible joltage of
  * each bank.
  */
object Day03:
  val logger: Logger = Logger(this.getClass.getName)

  /** Parse one bank per line from a classpath resource such as `inputs/Day03.txt`.
    *
    * Each non-blank line is a single bank of battery ratings (digits `1`-`9`), returned as a string
    * preserving their left-to-right order.
    *
    * @param filename
    *   resource path of the bank sequence
    * @return
    *   the banks in input order
    */
  def readFile(filename: String): Seq[String] =
    require(filename.nonEmpty, "filename.nonEmpty")
    logger.debug(s"filename: ${filename}")

    val source = Source.fromResource(filename)
    try source.getLines().toSeq.map(_.trim).filter(_.nonEmpty)
    finally source.close()
    end try
  end readFile

  /** The largest joltage a single bank can produce by turning on exactly two batteries.
    *
    * Enumerate every ordered pair of positions `(i, j)` with `i < j` and form the two-digit number
    * `rating(i) * 10 + rating(j)`; the bank's maximum joltage is the largest such value. A bank has
    * at least two batteries, so at least one pair always exists. This is O(n^2) time for a bank of
    * length `n`, with `n` bounded by the input line length, and O(1) extra space.
    *
    * @param bank
    *   a non-blank bank of battery ratings (at least two digits)
    * @return
    *   the maximum two-digit joltage this bank can produce
    */
  def maxJoltage(bank: String): Int =
    require(bank.length >= 2, s"bank must hold at least two batteries: '$bank'")
    val ratings = bank.map(_ - '0')
    (for
      i <- 0 until ratings.length
      j <- (i + 1) until ratings.length
    yield ratings(i) * 10 + ratings(j)).max
  end maxJoltage

  /** Sum the maximum joltage of every bank.
    *
    * Each bank is independent, so the answer is the sum of [[maxJoltage]] over all banks. The
    * result fits comfortably in [[Int]] (at most 99 per bank), but is returned as [[Long]] for
    * symmetry with larger parts. O(total digits) time overall.
    *
    * @param banks
    *   the banks read from the input
    * @return
    *   the total output joltage
    */
  def part1(banks: Seq[String]): Long =
    require(banks.nonEmpty, "banks.nonEmpty")
    logger.debug(s"banks: ${banks.size} banks")
    banks.map(maxJoltage).sum.toLong
  end part1

  /** The largest joltage a single bank can produce by turning on exactly twelve batteries.
    *
    * The joltage is the twelve-digit number formed by the chosen batteries in their original
    * left-to-right order, so a bank of length `n` produces a length-12 subsequence. To maximize the
    * value we must remove exactly `n - 12` digits while keeping the remaining twelve in order, i.e.
    * find the lexicographically largest length-12 subsequence.
    *
    * A greedy monotonic stack does this in one left-to-right pass: keep every digit on a stack, but
    * while a removal is still allowed (`remove` > 0) and the keystack's top is strictly smaller
    * than the incoming digit, pop (remove) the top — replacing a smaller leading digit with a
    * larger one always yields a larger number for a fixed length. Any leftover removals are dropped
    * from the end, where they are least significant. This is O(n) time and O(n) space for a bank of
    * length `n`.
    *
    * @param bank
    *   a non-blank bank of battery ratings (at least twelve digits)
    * @return
    *   the maximum twelve-digit joltage this bank can produce
    */
  def maxJoltage12(bank: String): Long =
    require(bank.length >= 12, s"bank must hold at least twelve batteries: '$bank'")
    var remove = bank.length - 12
    val kept = scala.collection.mutable.Stack[Char]()
    for digit <- bank do
      while remove > 0 && kept.nonEmpty && kept.top < digit do
        kept.pop()
        remove -= 1
      kept.push(digit)
    end for
    // Any removals left over are dropped from the end (least significant positions).
    val chosen = kept.toList.reverse.dropRight(remove)
    chosen.foldLeft(0L)((acc, d) => acc * 10 + (d - '0'))
  end maxJoltage12

  /** Sum the maximum twelve-digit joltage of every bank.
    *
    * Each bank is independent, so the answer is the sum of [[maxJoltage12]] over all banks. Each
    * bank's value is at most twelve nines (less than 10^12), so [[Long]] is safe up to the number
    * of banks in the input. O(total digits) time overall.
    *
    * @param banks
    *   the banks read from the input
    * @return
    *   the total output joltage
    */
  def part2(banks: Seq[String]): Long =
    require(banks.nonEmpty, "banks.nonEmpty")
    logger.debug(s"banks: ${banks.size} banks")
    banks.map(maxJoltage12).sum
  end part2

end Day03
