package aoc2025

import com.typesafe.scalalogging.Logger
import scala.io.Source

/** Day 7: count the distinct splitters reached by downward-moving tachyon beams.
  *
  * At each row, a set of columns records occupied beam positions. A beam crossing empty space
  * keeps its column; a beam hitting a splitter moves to its two neighboring columns. Using a set
  * merges beams that meet, so a splitter hit by coincident paths is counted only once. Each row
  * visits at most the diagram's width, for O(height * width) time and O(width) space.
  */
object Day07:
  val logger: Logger = Logger(this.getClass.getName)

  /** Read a manifold diagram from a classpath resource such as `inputs/Day07.txt`.
    *
    * @return the diagram, one unmodified row per line
    */
  def readFile(filename: String): Vector[String] =
    require(filename.nonEmpty, "filename.nonEmpty")
    val source = Source.fromResource(filename)
    try source.getLines().toVector
    finally source.close()

  /** Count each splitter reached by at least one beam exactly once.
    *
    * The unique starting point must be in the first row of a rectangular diagram. Process rows
    * from top to bottom; at a splitter, replace its beam with left and right beams in the same row
    * (which continue downward on the next row). Set union models beams merging in one location.
    * Beams emitted outside the manifold simply exit it.
    *
    * @return the number of distinct splitter encounters
    */
  def part1(rows: Seq[String]): Int =
    require(rows.nonEmpty && rows.head.nonEmpty, "expected a nonempty diagram")
    val width = rows.head.length
    require(rows.forall(row => row.length == width && row.forall(c => c == '.' || c == '^' || c == 'S')),
      "expected a rectangular diagram containing only '.', '^', and 'S'")
    val starts = rows.zipWithIndex.flatMap: (row, y) =>
      row.indices.collect { case x if row(x) == 'S' => (x, y) }
    require(starts.size == 1 && starts.head._2 == 0, "expected exactly one start in the first row")

    val (_, splits) = rows.foldLeft((Set(starts.head._1), 0)):
      case ((beams, count), row) =>
        val hit = beams.filter(x => row(x) == '^')
        val next = (beams -- hit) ++ hit.flatMap(x => Seq(x - 1, x + 1)).filter(x => x >= 0 && x < width)
        (next, count + hit.size)
    splits

  /** Part 2 counts distinct particle histories rather than merged beams; it is out of scope. */
  def part2(rows: Seq[String]): Long = ???
end Day07
