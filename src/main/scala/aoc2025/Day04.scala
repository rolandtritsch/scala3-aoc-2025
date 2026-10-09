package aoc2025

import com.typesafe.scalalogging.Logger
import scala.io.Source

/** Day 4: count paper rolls with fewer than four neighboring rolls.
  *
  * Inspect each occupied cell against the eight surrounding coordinates of the original grid.
  * Coordinates outside the diagram contain no rolls. The grid is never modified, so accessibility
  * is evaluated simultaneously, not after removing any rolls. For a grid of h rows and w columns,
  * this takes O(h * w) time and O(1) auxiliary space beyond the parsed input.
  */
object Day04:
  val logger: Logger = Logger(this.getClass.getName)

  /** Read a rectangular paper-roll diagram from a classpath resource such as `inputs/Day04.txt`.
    *
    * Each line is a row containing `@` for a roll and `.` for an empty location.
    *
    * @return
    *   the diagram in row order
    */
  def readFile(filename: String): Vector[String] =
    require(filename.nonEmpty, "filename.nonEmpty")
    val source = Source.fromResource(filename)
    try
      val rows = source.getLines().toVector
      validate(rows)
      rows
    finally source.close()
    end try
  end readFile

  /** Count rolls accessible in the original diagram.
    *
    * For each roll, visit its eight neighboring positions; only in-bounds `@` cells contribute to
    * its neighbor count. Fewer than four neighboring rolls makes it accessible.
    *
    * @return
    *   the number of accessible rolls
    */
  def part1(rows: Seq[String]): Int =
    validate(rows)
    (for
      row <- rows.indices.iterator
      col <- rows(row).indices.iterator if rows(row)(col) == '@'
      neighbors = (for
        dr <- (-1 to 1).iterator
        dc <- (-1 to 1).iterator if dr != 0 || dc != 0
        nr = row + dr
        nc = col + dc if nr >= 0 && nr < rows.size && nc >= 0 && nc < rows(nr).length
        if rows(nr)(nc) == '@'
      yield 1).sum if neighbors < 4
    yield 1).sum
  end part1

  /** Part 2 repeatedly removes accessible rolls and is out of scope for this part-1 solution. */
  def part2(rows: Seq[String]): Int = ???

  /** Reject empty, non-rectangular, or invalid diagrams before inspecting coordinates. */
  private def validate(rows: Seq[String]): Unit =
    require(rows.nonEmpty && rows.head.nonEmpty, "diagram must not be empty")
    require(
      rows.forall(row => row.length == rows.head.length && row.forall(c => c == '@' || c == '.')),
      "diagram must be rectangular and contain only @ and .",
    )

  end validate

end Day04
