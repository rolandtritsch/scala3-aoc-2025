package aoc2025

import com.typesafe.scalalogging.Logger
import scala.io.Source

/** Day 5: count available ingredients whose IDs lie in at least one fresh range.
  *
  * Ranges are inclusive and can overlap, so each available ID is tested for membership rather
  * than expanding ranges into sets of potentially enormous size. For r ranges and a available
  * IDs, the scan takes O(r * a) time and O(1) additional space beyond the parsed input.
  */
object Day05:
  val logger: Logger = Logger(this.getClass.getName)

  /** Read fresh ranges and available IDs from a classpath resource such as `inputs/Day05.txt`.
    *
    * The first section contains one inclusive `start-end` range per line; a blank line separates
    * it from the second section, which contains one available ID per line. IDs use Long because
    * the puzzle input exceeds the Int range.
    *
    * @return the ranges and available IDs in input order
    */
  def readFile(filename: String): (Vector[(Long, Long)], Vector[Long]) =
    require(filename.nonEmpty, "filename.nonEmpty")
    val source = Source.fromResource(filename)
    try
      val lines = source.getLines().toVector
      val (rangeLines, remainder) = lines.span(_.nonEmpty)
      require(rangeLines.nonEmpty && remainder.nonEmpty && remainder.tail.nonEmpty,
        "expected ranges, a blank line, and available IDs")
      val ranges = rangeLines.map: line =>
        val bounds = line.split("-", -1)
        require(bounds.length == 2, s"invalid range: $line")
        val start = bounds(0).toLong
        val end = bounds(1).toLong
        require(start <= end, s"reversed range: $line")
        (start, end)
      val available = remainder.tail.map(_.toLong)
      (ranges, available)
    finally source.close()
  end readFile

  /** Count available IDs that occur in any inclusive fresh range.
    *
    * A membership check includes both endpoints. Overlapping ranges do not double-count an ID:
    * each entry in the available list contributes at most one to the result.
    *
    * @return the number of available fresh ingredients
    */
  def part1(input: (Seq[(Long, Long)], Seq[Long])): Int =
    val (ranges, available) = input
    available.count(id => ranges.exists { case (start, end) => start <= id && id <= end })

  /** Part 2 counts every ID covered by the ranges and is out of scope for this part-1 solution. */
  def part2(input: (Seq[(Long, Long)], Seq[Long])): Long = ???
end Day05
