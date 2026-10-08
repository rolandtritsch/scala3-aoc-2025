package aoc2025

import com.typesafe.scalalogging.Logger

/** Day02 - Gift Shop.
  *
  * An invalid product ID has an even number of digits and consists of two identical digit
  * sequences. Each input range is searched by digit width and sequence prefix, avoiding a scan over
  * every ID in potentially large ranges.
  */
object Day02:
  val logger: Logger = Logger(this.getClass.getName)

  /** Reads comma-separated inclusive product ID ranges from a classpath resource.
    *
    * @param filename
    *   the resource path, such as `inputs/Day02.txt`
    * @return
    *   the lower and upper bound of each product ID range
    */
  def readFile(filename: String): Seq[(BigInt, BigInt)] =
    import scala.io.Source

    require(filename.nonEmpty, "filename.nonEmpty")
    logger.debug(s"filename: ${filename}")

    val source = Source.fromResource(filename)
    try source.getLines().flatMap(_.trim.split(",")).map: range =>
        val bounds = range.split("-", 2).map(BigInt(_))
        require(
          bounds.length == 2 && bounds(0) >= 1 && bounds(0) <= bounds(1),
          s"Invalid range: ${range}",
        )
        (bounds(0), bounds(1))
      .toSeq
    finally source.close()
    end try
  end readFile

  /** Sums all invalid IDs in the inclusive ranges.
    *
    * For a repeated sequence of width `w`, the ID is `prefix * (10^w + 1)`. Intersecting the prefix
    * bounds with each range lets the solver sum candidates arithmetically instead of inspecting
    * every ID.
    *
    * @param ranges
    *   inclusive lower and upper product ID bounds
    * @return
    *   the sum of all IDs made of a digit sequence repeated exactly twice
    */
  def part1(ranges: Seq[(BigInt, BigInt)]): BigInt =
    require(ranges.nonEmpty, "ranges.nonEmpty")
    ranges.map: (start, end) =>
      require(start >= 1 && start <= end, s"Invalid range: ${start}-${end}")
      invalidIdSum(start, end)
    .sum
  end part1

  /** Part two is outside the scope of the Part One task and remains an explicit stub.
    *
    * @param ranges
    *   the product ID ranges, unused for this out-of-scope part
    * @return
    *   zero as a placeholder until Part Two is implemented
    */
  def part2(ranges: Seq[(BigInt, BigInt)]): BigInt = 0
  end part2

  /** Sums IDs formed by repeating one prefix twice within the supplied range.
    *
    * @param start
    *   inclusive lower bound
    * @param end
    *   inclusive upper bound
    * @return
    *   the sum of matching repeated-sequence IDs
    */
  private def invalidIdSum(start: BigInt, end: BigInt): BigInt =
    val maxDigits = end.toString.length
    (1 to maxDigits / 2).foldLeft(BigInt(0)): (sum, width) =>
      val prefixScale = BigInt(10).pow(width)
      val repeatedScale = prefixScale + 1
      val minId = BigInt(10).pow(width * 2 - 1)
      val maxId = BigInt(10).pow(width * 2) - 1
      val lowerBound = start max minId
      val upperBound = end min maxId
      val minPrefix = (lowerBound + repeatedScale - 1) / repeatedScale max (prefixScale / 10)
      val maxPrefix = (upperBound / repeatedScale) min (prefixScale - 1)

      if minPrefix > maxPrefix then sum
      else
        val count = maxPrefix - minPrefix + 1
        val prefixSum = count * (minPrefix + maxPrefix) / 2
        sum + prefixSum * repeatedScale
      end if
  end invalidIdSum

end Day02
