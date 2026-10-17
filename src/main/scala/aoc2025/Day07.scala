package aoc2025

import scala.io.Source

import com.typesafe.scalalogging.Logger

/** Day07 - Laboratories (tachyon manifold).
  *
  * A tachyon beam enters a manifold diagram at the cell marked `S` in the top row and always moves
  * downward. Beams pass freely through empty space (`.`), but a beam that reaches a splitter (`^`)
  * is stopped and replaced by two new beams emitted from the cells immediately to the left and to
  * the right of the splitter, which also move downward. Multiple beams merging into the same cell
  * are treated as a single beam.
  *
  * Part 1 counts the total number of times any beam is split.
  */
object Day07:
  val logger: Logger = Logger(this.getClass.getName)

  /** Read a manifold diagram into its rectangular grid of rows, preserving every character.
    *
    * @param filename
    *   resource path of the diagram
    * @return
    *   the grid as a vector of rows, each a string of '.'/`S`/`^`
    */
  def readFile(filename: String): Vector[String] =
    require(filename.nonEmpty, "filename.nonEmpty")
    logger.debug(s"filename: ${filename}")

    val source = Source.fromResource(filename)
    try source.getLines().toVector
    finally source.close()
    end try
  end readFile

  /** Count how many times a beam is split as it descends through the manifold.
    *
    * A set of active columns tracks which downward beams exist at the current row. Starting from
    * the column of `S` on the top row, each row advances every active beam: an empty cell (`S` or
    * `.`) carries the beam into the same column on the next row, while a splitter (`^`) stops the
    * beam, records one split, and emits new beams into the neighbouring columns. Columns outside
    * the grid are discarded (the beam exits the manifold), and the set deduplicates beams that
    * merge into a cell. With an `h`-row by `w`-column grid this is O(h * w) time and O(w) space.
    *
    * @param grid
    *   the manifold diagram
    * @return
    *   the number of beam splits
    */
  def part1(grid: Vector[String]): Int =
    require(grid.nonEmpty, "grid.nonEmpty")
    require(grid.distinctBy(_.length).size == 1, "all rows must have equal width")
    logger.debug(s"grid: ${grid.size} rows x ${grid.head.length} cols")

    val width = grid.head.length
    val start = grid.head.indexOf('S')
    require(start >= 0, "grid must contain a start cell 'S'")

    var active: Set[Int] = Set(start)
    var splits = 0
    for row <- grid do
      var next = Set.empty[Int]
      for column <- active do
        row(column) match
          case '^' =>
            splits += 1
            next += column - 1
            next += column + 1
          case _ => next += column
      end for
      active = next.filter(column => column >= 0 && column < width)
    end for
    splits
  end part1

  /** Count how many distinct timelines a single quantum tachyon particle ends up on.
    *
    * Under the many-worlds interpretation, a single particle takes both the left and right path of
    * every splitter it reaches, so each splitter doubles the number of timelines. Unlike part 1,
    * timelines that converge on the same cell are never deduplicated: two different decision
    * sequences ending at the same position are two distinct timelines. The particle always moves
    * one row downward, carrying its cell unchanged through `.`/`S`, and branching into cells
    * `column - 1` and `column + 1` at a `^`. A branch emitted outside the grid exits the manifold,
    * ending that timeline; every timeline that survives to the bottom row also exits there.
    *
    * Each column carries a multiplicity counting the distinct timelines currently at that cell.
    * This is a top-down propagation O(h * w) in time and O(w) in space (the per-column counts can
    * grow combinatorially, so they are tracked as `Long`).
    *
    * @param grid
    *   the manifold diagram
    * @return
    *   the number of distinct timelines the particle ends up on
    */
  def part2(grid: Vector[String]): Long =
    require(grid.nonEmpty, "grid.nonEmpty")
    require(grid.distinctBy(_.length).size == 1, "all rows must have equal width")
    logger.debug(s"grid: ${grid.size} rows x ${grid.head.length} cols")

    val width = grid.head.length
    val start = grid.head.indexOf('S')
    require(start >= 0, "grid must contain a start cell 'S'")

    var counts: Map[Int, Long] = Map(start -> 1L)
    var exited = 0L
    for row <- grid do
      var next = Map.empty[Int, Long]
      for (col, multiplicity) <- counts do
        val targets = if row(col) == '^' then Seq(col - 1, col + 1) else Seq(col)
        for target <- targets do
          if target < 0 || target >= width then exited += multiplicity
          else next = next.updated(target, multiplicity + next.getOrElse(target, 0L))
      end for
      counts = next
    end for
    exited + counts.values.sum
  end part2

end Day07
