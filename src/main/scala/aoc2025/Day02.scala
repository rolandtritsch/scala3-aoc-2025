package aoc2025

import com.typesafe.scalalogging.Logger
import scala.io.Source

/** Day02 - Gift Shop.
  *
  * A gift-shop clerk needs the invalid product IDs identified. The database lists inclusive ranges
  * of IDs as `first-last`, comma-separated on a single line.
  *
  * In part 1 an ID is invalid when its decimal representation is some digit sequence written
  * exactly twice, e.g. `55` ("5" twice), `6464` ("64" twice) or `123123` ("123" twice). This holds
  * exactly when the ID has an even number of digits and its first half equals its second half.
  */
object Day02:
  val logger: Logger = Logger(this.getClass.getName)

  /** An inclusive range of product IDs bounded by its first and last IDs. */
  final case class IdRange(first: Long, last: Long):
    require(first <= last, s"first ($first) must be <= last ($last)")

  /** Parse the comma-separated `first-last` ID ranges from a classpath resource.
    *
    * The input is a single logical line of ranges, but `readFile` tolerates manual line wrapping by
    * splitting every line on commas and dropping the empty fragments this produces at wrapped line
    * ends.
    *
    * @param filename
    *   resource path such as `inputs/Day02.txt`
    * @return
    *   the inclusive ID ranges in input order
    */
  def readFile(filename: String): Seq[IdRange] =
    require(filename.nonEmpty, "filename.nonEmpty")
    logger.debug(s"filename: ${filename}")

    val source = Source.fromResource(filename)
    try source.getLines().toSeq.flatMap: line =>
        line.trim.split(",").filter(_.nonEmpty).map: token =>
          val dash = token.indexOf('-')
          require(dash > 0, s"invalid range (missing '-'): $token")
          val first = token.substring(0, dash).trim.toLong
          val last = token.substring(dash + 1).trim.toLong
          IdRange(first, last)
    finally source.close()
    end try
  end readFile

  /** True when `id` is some digit sequence repeated exactly twice.
    *
    * The ID's decimal string must have an even length, and its two halves must be equal: `55`
    * halves into `5` and `5`, `6464` into `64` and `64`, and `222222` into `222` and `222`.
    * Odd-length IDs never qualify, and no ID in the input has a leading zero, so no padding is
    * required.
    *
    * @param id
    *   a product ID
    * @return
    *   whether the ID is invalid under the part 1 rule
    */
  def isInvalid(id: Long): Boolean =
    val digits = id.toString
    val half = digits.length / 2
    (digits.length % 2 == 0) && digits.substring(0, half) == digits.substring(half)
  end isInvalid

  /** Sum every invalid ID appearing in the given ranges.
    *
    * Every range is bounded in size, so iterating each ID and summing the ones matching
    * [[isInvalid]] visits at most a few million values with an O(digits) check each. The result is
    * returned as [[Long]] because the sum of ten-digit IDs exceeds [[Int]] range.
    *
    * @param ranges
    *   the inclusive product-ID ranges
    * @return
    *   the sum of the invalid IDs
    */
  def part1(ranges: Seq[IdRange]): Long =
    require(ranges.nonEmpty, "ranges.nonEmpty")
    logger.debug(s"ranges: ${ranges}")

    ranges.iterator
      .flatMap(range => (range.first to range.last).iterator)
      .filter(isInvalid)
      .sum
  end part1

  /** Part 2 counts IDs made of a digit sequence repeated at least twice; out of scope for now. */
  def part2(ranges: Seq[IdRange]): Long = ???
  end part2

end Day02