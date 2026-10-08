package aoc2025

import com.typesafe.scalalogging.Logger

/** Day02 identifies product IDs made of a digit sequence repeated exactly twice.
  *
  * For each even digit length, part 1 generates the repeated values directly and sums those that
  * fall inside the inclusive input ranges.
  */
object Day02:
  val logger: Logger = Logger(this.getClass.getName)

  /** Reads comma-separated inclusive product ID ranges from a classpath resource.
    *
    * @param filename
    *   resource path such as `inputs/Day02.txt`
    * @return
    *   each range as its inclusive start and end IDs
    */
  def readFile(filename: String): Seq[(Long, Long)] =
    import scala.io.Source

    require(filename.nonEmpty, "filename.nonEmpty")
    logger.debug(s"filename: ${filename}")

    val source = Source.fromResource(filename)
    try source.getLines().mkString.trim.split(",").toSeq.map: range =>
        val Array(start, end) = range.split("-"): @unchecked
        val parsedRange = (start.toLong, end.toLong)
        require(parsedRange._1 > 0 && parsedRange._1 <= parsedRange._2, "valid inclusive range")
        parsedRange
    finally source.close()
    end try
  end readFile

  /** Sums IDs in the given ranges whose decimal digits form two identical halves.
    *
    * For a half-length of `n`, every candidate is `prefix * (10^n + 1)`. Prefix bounds are derived
    * from each input range so the method checks only candidates that could be included, rather than
    * scanning every ID.
    *
    * @param ranges
    *   inclusive positive product ID ranges
    * @return
    *   the sum of IDs consisting of a digit sequence repeated exactly twice
    */
  def part1(ranges: Seq[(Long, Long)]): Long =
    require(ranges.nonEmpty, "ranges.nonEmpty")
    logger.debug(s"ranges: ${ranges.size}")

    ranges.foldLeft(0L): (total, range) =>
      val (start, end) = range
      require(start > 0 && start <= end, "valid inclusive range")

      val maximumHalfLength = end.toString.length / 2
      val rangeTotal = (1 to maximumHalfLength).foldLeft(0L): (lengthTotal, halfLength) =>
        val placeValue = math.pow(10, halfLength).toLong
        val multiplier = placeValue + 1
        val smallestPrefix = placeValue / 10
        val largestPrefix = placeValue - 1
        val firstInRange = start / multiplier + (if start % multiplier == 0 then 0 else 1)
        val lastInRange = end / multiplier
        val firstPrefix = math.max(smallestPrefix, firstInRange)
        val lastPrefix = math.min(largestPrefix, lastInRange)

        if firstPrefix > lastPrefix then lengthTotal
        else
          (firstPrefix to lastPrefix).foldLeft(lengthTotal): (sum, prefix) =>
            sum + prefix * multiplier
      total + rangeTotal
  end part1

  /** Part 2 is outside the scope of the current implementation.
    *
    * @param ranges
    *   the product ID ranges, accepted to match the daily solution API
    * @return
    *   zero as the explicit part-2 placeholder
    */
  def part2(ranges: Seq[(Long, Long)]): Long =
    require(ranges.nonEmpty, "ranges.nonEmpty")
    0L
  end part2

end Day02
