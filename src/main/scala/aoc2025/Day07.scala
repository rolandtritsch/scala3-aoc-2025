package aoc2025

import com.typesafe.scalalogging.Logger

/** Simulates tachyon beams descending through the Day07 manifold.
  *
  * A beam travels straight through empty cells. At a splitter it stops and produces beams in the
  * immediately adjacent columns; beams that reach the same column merge and are counted once.
  */
object Day07:
  val logger: Logger = Logger(this.getClass.getName)

  /** Reads the manifold rows from a classpath resource.
    *
    * @param filename
    *   resource path such as `inputs/Day07.txt`
    * @return
    *   manifold rows in top-to-bottom order
    */
  def readFile(filename: String): Seq[String] =
    import scala.io.Source

    require(filename.nonEmpty, "filename.nonEmpty")
    logger.debug(s"filename: ${filename}")

    val source = Source.fromResource(filename)
    try source.getLines().toVector
    finally source.close()
    end try
  end readFile

  /** Counts every splitter reached by at least one beam.
    *
    * The active beam positions are represented as a set for each row, so beams arriving at the same
    * column merge before reaching the next row. A splitter replaces its incoming beam with positions
    * one column to either side; positions beyond the manifold edge have exited and are discarded.
    *
    * @param manifold
    *   the rectangular manifold containing one `S` and otherwise `.` or `^`
    * @return
    *   the number of splitter encounters
    */
  def part1(manifold: Seq[String]): Int =
    require(manifold.nonEmpty, "manifold.nonEmpty")
    val width = manifold.head.length
    require(width > 0, "manifold rows must not be empty")
    require(manifold.forall(_.length == width), "manifold must be rectangular")
    require(manifold.flatten.forall(cell => cell == '.' || cell == '^' || cell == 'S'))
    val starts = manifold.zipWithIndex.flatMap: (row, rowIndex) =>
      row.zipWithIndex.collect:
        case ('S', column) => (rowIndex, column)
    require(starts.length == 1, "manifold must contain exactly one start")
    val (startRow, startColumn) = starts.head
    logger.debug(s"manifold rows: ${manifold.size}, width: ${width}")

    manifold.drop(startRow + 1).foldLeft((0, Set(startColumn))): (state, row) =>
      val (splitCount, beams) = state
      val splitterColumns = beams.filter(column => row(column) == '^')
      val nextBeams = beams.flatMap: column =>
        if row(column) == '^' then Set(column - 1, column + 1) else Set(column)
      val inBoundsBeams = nextBeams.filter(column => column >= 0 && column < width)
      (splitCount + splitterColumns.size, inBoundsBeams)
    ._1
  end part1

  /** Part 2 is outside the scope of the current implementation.
    *
    * @param manifold
    *   the manifold, accepted to match the daily solution API
    * @return
    *   zero as the explicit part-2 placeholder
    */
  def part2(manifold: Seq[String]): Int =
    require(manifold.nonEmpty, "manifold.nonEmpty")
    0
  end part2

end Day07
