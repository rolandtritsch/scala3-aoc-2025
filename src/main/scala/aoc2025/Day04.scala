package aoc2025

import com.typesafe.scalalogging.Logger
import scala.io.Source

/** Day04 - Printing Department.
  *
  * Large rolls of paper (`@`) are arranged on a grid (`.` marks an empty position). A forklift can
  * access a roll of paper only when there are fewer than four rolls of paper in the eight adjacent
  * (king-move) positions surrounding it. Part 1 counts how many rolls of paper a forklift can
  * access.
  */
object Day04:
  val logger: Logger = Logger(this.getClass.getName)

  /** Parse the grid of paper rolls from a classpath resource such as `inputs/Day04.txt`.
    *
    * Each non-blank line is one row of the grid; `@` marks a roll of paper and `.` marks an empty
    * position. Rows are returned preserving their left-to-right order.
    *
    * @param filename
    *   resource path of the paper-roll grid
    * @return
    *   the grid rows in input order
    */
  def readFile(filename: String): Seq[String] =
    require(filename.nonEmpty, "filename.nonEmpty")
    logger.debug(s"filename: ${filename}")

    val source = Source.fromResource(filename)
    try source.getLines().toSeq.map(_.trim).filter(_.nonEmpty)
    finally source.close()
    end try
  end readFile

  /** The number of paper rolls in the eight king-move positions adjacent to `(r, c)`.
    *
    * Every offset `(dr, dc)` in `[-1, 1]^2` other than `(0, 0)` is considered; an offset is counted
    * only when it lands inside the grid and holds a `@`. This is O(1) time and O(1) extra space.
    *
    * @param rows
    *   the paper-roll grid
    * @param r
    *   the row of the position to inspect
    * @param c
    *   the column of the position to inspect
    * @return
    *   the count of adjacent paper rolls (between 0 and 8)
    */
  def adjacentRolls(rows: Seq[String], r: Int, c: Int): Int =
    require(rows.nonEmpty, "rows.nonEmpty")
    require(r >= 0 && r < rows.size, s"row $r out of bounds (0 until ${rows.size})")
    require(c >= 0 && c < rows(r).length, s"column $c out of bounds for row $r")

    (for
      dr <- -1 to 1
      dc <- -1 to 1
      if dr != 0 || dc != 0
      nr = r + dr
      nc = c + dc
      if nr >= 0 && nr < rows.size && nc >= 0 && nc < rows(nr).length && rows(nr)(nc) == '@'
    yield 1).size
  end adjacentRolls

  /** Count the rolls of paper a forklift can access.
    *
    * A roll at position `(r, c)` is accessible exactly when [[adjacentRolls]] is less than four.
    * Every `@` is inspected once, so this is O(R * C) time for a grid of `R` rows and `C` columns
    * (each cell doing O(1) work) and O(1) extra space.
    *
    * @param rows
    *   the paper-roll grid
    * @return
    *   the number of accessible rolls of paper
    */
  def part1(rows: Seq[String]): Int =
    require(rows.nonEmpty, "rows.nonEmpty")
    logger.debug(s"rows: ${rows.size} rows")

    (for
      r <- rows.indices
      c <- rows(r).indices
      if rows(r)(c) == '@'
      if adjacentRolls(rows, r, c) < 4
    yield 1).size
  end part1

  /** Placeholder for Part Two; not yet assigned.
    *
    * Part Two asks for the total number of rolls the Elves can remove by repeatedly removing every
    * currently accessible roll.
    */
  def part2(rows: Seq[String]): Int =
    require(rows.nonEmpty, "rows.nonEmpty")
    ??? // Part Two not yet assigned.
  end part2

end Day04
