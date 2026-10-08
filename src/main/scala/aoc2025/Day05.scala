package aoc2025

import com.typesafe.scalalogging.Logger

/** The Day05 inventory database, containing fresh-ID ranges and available ingredient IDs.
  *
  * @param freshRanges
  *   inclusive lower and upper bounds for each range of fresh ingredient IDs
  * @param availableIds
  *   ingredient IDs whose freshness must be checked
  */
final case class IngredientDatabase(freshRanges: Seq[(Long, Long)], availableIds: Seq[Long])

/** Counts available ingredients whose IDs occur in at least one inclusive fresh range.
  *
  * The ranges may overlap; an available ID is counted once whenever any range contains it.
  */
object Day05:
  val logger: Logger = Logger(this.getClass.getName)

  /** Reads and parses the fresh ranges and available IDs from a classpath resource.
    *
    * @param filename
    *   resource path such as `inputs/Day05.txt`
    * @return
    *   the parsed inventory database
    */
  def readFile(filename: String): IngredientDatabase =
    import scala.io.Source

    require(filename.nonEmpty, "filename.nonEmpty")
    logger.debug(s"filename: ${filename}")

    val source = Source.fromResource(filename)
    try
      val lines = source.getLines().map(_.trim).toVector
      val separator = lines.indexWhere(_.isEmpty)
      require(separator >= 0, "database must separate ranges and available IDs with a blank line")

      val freshRanges = lines.take(separator).filter(_.nonEmpty).map(parseRange)
      val availableIds = lines.drop(separator + 1).filter(_.nonEmpty).map(_.toLong)
      require(freshRanges.nonEmpty, "freshRanges.nonEmpty")
      require(availableIds.nonEmpty, "availableIds.nonEmpty")

      IngredientDatabase(freshRanges, availableIds)
    finally source.close()
    end try
  end readFile

  /** Parses one inclusive fresh-ID range.
    *
    * @param line
    *   a range in `lower-upper` form
    * @return
    *   the inclusive lower and upper IDs
    */
  private def parseRange(line: String): (Long, Long) =
    val bounds = line.split("-", -1)
    require(bounds.length == 2, s"invalid fresh range: ${line}")
    val lower = bounds(0).toLong
    val upper = bounds(1).toLong
    require(lower <= upper, s"fresh range lower bound exceeds upper bound: ${line}")
    (lower, upper)
  end parseRange

  /** Counts available IDs contained in at least one inclusive fresh range.
    *
    * @param database
    *   the fresh ranges and available IDs to evaluate
    * @return
    *   the number of available ingredient IDs that are fresh
    */
  def part1(database: IngredientDatabase): Int =
    require(database.freshRanges.nonEmpty, "database.freshRanges.nonEmpty")
    require(database.availableIds.nonEmpty, "database.availableIds.nonEmpty")
    logger
      .debug(s"fresh ranges: ${database.freshRanges.size}, available IDs: ${database.availableIds
          .size}")

    database.availableIds.count: id =>
      database.freshRanges.exists: (lower, upper) =>
        lower <= id && id <= upper
  end part1

  /** Part 2 is outside the scope of the current implementation.
    *
    * @param database
    *   the inventory database, accepted to match the daily solution API
    * @return
    *   zero as the explicit part-2 placeholder
    */
  def part2(database: IngredientDatabase): Int =
    require(database.freshRanges.nonEmpty, "database.freshRanges.nonEmpty")
    require(database.availableIds.nonEmpty, "database.availableIds.nonEmpty")
    0
  end part2

end Day05
