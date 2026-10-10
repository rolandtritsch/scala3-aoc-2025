package aoc2025

import com.typesafe.scalalogging.Logger
import scala.io.Source

/** Day05 - Cafeteria.
  *
  * The Elves' new inventory management database lists a set of fresh-ingredient ID ranges, a blank
  * line, and a list of available ingredient IDs. The ranges are inclusive (`3-5` covers IDs 3, 4
  * and 5), may overlap, and an ID is fresh when it falls inside at least one range. Part 1 counts
  * how many of the available ingredient IDs are fresh.
  */
object Day05:
  val logger: Logger = Logger(this.getClass.getName)

  /** An inclusive range of fresh ingredient IDs bounded by its first and last IDs. */
  final case class IdRange(first: Long, last: Long):
    require(first <= last, s"first ($first) must be <= last ($last)")

  /** A parsed database: the fresh-ingredient ranges followed by the available ingredient IDs. */
  final case class Database(ranges: Seq[IdRange], available: Seq[Long])

  /** Parse the database from a classpath resource such as `inputs/Day05.txt`.
    *
    * The file is a list of `first-last` fresh-ingredient ranges, one per line, a single blank line,
    * then a list of available ingredient IDs, one per line. Everything before the first blank line
    * is a range and everything after it is an available ID. This is O(n) time in the number of
    * input lines and O(n) space.
    *
    * @param filename
    *   resource path of the database
    * @return
    *   the parsed fresh ranges and available IDs, each in input order
    */
  def readFile(filename: String): Database =
    require(filename.nonEmpty, "filename.nonEmpty")
    logger.debug(s"filename: ${filename}")

    val source = Source.fromResource(filename)
    val lines =
      try source.getLines().toSeq.map(_.trim)
      finally source.close()
      end try

    val separator = lines.indexWhere(_.isEmpty)
    require(separator > 0, "missing blank line separating ranges from available IDs")

    val ranges = lines.take(separator).map: line =>
      val dash = line.indexOf('-')
      require(dash > 0, s"invalid range (missing '-'): $line")
      IdRange(line.substring(0, dash).toLong, line.substring(dash + 1).toLong)

    val available = lines.drop(separator + 1).filter(_.nonEmpty).map(_.toLong)
    Database(ranges, available)
  end readFile

  /** True when `id` is fresh, i.e. it falls inside at least one fresh-ingredient range.
    *
    * Every range is inclusive, so `id` is fresh exactly when `first <= id <= last` for some range.
    * This is O(r) time for `r` ranges and O(1) extra space.
    *
    * @param id
    *   an ingredient ID
    * @param ranges
    *   the fresh-ingredient ranges
    * @return
    *   whether the ID is fresh
    */
  def isFresh(id: Long, ranges: Seq[IdRange]): Boolean =
    require(ranges.nonEmpty, "ranges.nonEmpty")
    ranges.exists(r => id >= r.first && id <= r.last)
  end isFresh

  /** Count how many of the available ingredient IDs are fresh.
    *
    * Each available ID is checked against the fresh ranges via [[isFresh]]. With `r` ranges and `a`
    * available IDs this is O(r * a) time and O(1) extra space, which is trivial for the puzzle's
    * 182 ranges and 1000 available IDs. Overlapping ranges need no special handling because
    * membership in any single range already decides the result.
    *
    * @param database
    *   the parsed database
    * @return
    *   the number of fresh available ingredient IDs
    */
  def part1(database: Database): Int =
    require(database.ranges.nonEmpty, "database.ranges.nonEmpty")
    require(database.available.nonEmpty, "database.available.nonEmpty")
    logger.debug(s"ranges: ${database.ranges.size}, available: ${database.available.size}")

    database.available.count(id => isFresh(id, database.ranges))
  end part1

end Day05