package aoc2025

import com.typesafe.scalalogging.Logger
import scala.io.Source

/** Day 6: evaluate vertically arranged addition and multiplication problems.
  *
  * A column containing only spaces separates adjacent problems. Within each contiguous block of
  * columns, each row above the operator is a separate number, regardless of its alignment. The
  * answers are combined using arbitrary-precision integers to avoid overflow when multiplying.
  * Scanning the columns and extracting each row takes O(rows * width) time.
  */
object Day06:
  val logger: Logger = Logger(this.getClass.getName)

  /** Read the worksheet from a classpath resource such as `inputs/Day06.txt`.
    *
    * Spaces, including trailing spaces, are preserved because they determine the column boundaries.
    *
    * @return the number rows followed by the operator row
    */
  def readFile(filename: String): Vector[String] =
    require(filename.nonEmpty, "filename.nonEmpty")
    val source = Source.fromResource(filename)
    try source.getLines().toVector
    finally source.close()

  /** Sum the answers to the worksheet's left-to-right vertical problems.
    *
    * Columns consisting entirely of spaces (or lying past a shorter row's end) divide problems.
    * Within each block, trim and parse each number row independently, then apply the sole operator
    * on the final row. An empty or malformed problem is rejected rather than silently skipped.
    *
    * @return the grand total of all problem answers
    */
  def part1(rows: Seq[String]): BigInt =
    require(rows.size >= 2, "expected number rows and an operator row")
    val width = rows.map(_.length).max
    val blank = (0 until width).map(column => rows.forall(row => column >= row.length || row(column) == ' '))
    val boundaries = Vector(-1) ++ blank.indices.filter(blank) ++ Vector(width)
    val problems = boundaries.sliding(2).collect:
      case Seq(left, right) if right > left + 1 =>
        val fields = rows.map(row => row.slice(left + 1, right).trim)
        require(fields.forall(_.nonEmpty), s"missing value in columns ${left + 1} to $right")
        val numbers = fields.init.map(value => BigInt(value))
        fields.last match
          case "+" => numbers.sum
          case "*" => numbers.product
          case operation => throw new IllegalArgumentException(s"invalid operation: $operation")
    problems.sum

  /** Part 2 changes the reading direction and number layout; it is out of scope for part 1. */
  def part2(rows: Seq[String]): BigInt = ???
end Day06
