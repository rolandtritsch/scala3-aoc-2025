package aoc2025

import com.typesafe.scalalogging.Logger

/** One vertically arranged arithmetic problem from the Day06 worksheet.
  *
  * @param numbers
  *   the operands in top-to-bottom order
  * @param operator
  *   the operation displayed below the operands, either `+` or `*`
  */
final case class WorksheetProblem(numbers: Seq[BigInt], operator: Char)

/** Evaluates each side-by-side arithmetic problem in the Day06 worksheet.
  *
  * Worksheet problems occupy runs of columns separated by a full blank column. Each run contains
  * vertically arranged operands with its operator on the last row; the grand total is the sum of
  * every problem's result.
  */
object Day06:
  val logger: Logger = Logger(this.getClass.getName)

  /** Reads a worksheet resource and parses its column-separated problems.
    *
    * @param filename
    *   resource path such as `inputs/Day06.txt`
    * @return
    *   the parsed worksheet problems in left-to-right order
    */
  def readFile(filename: String): Seq[WorksheetProblem] =
    import scala.io.Source

    require(filename.nonEmpty, "filename.nonEmpty")
    logger.debug(s"filename: ${filename}")

    val source = Source.fromResource(filename)
    try parseWorksheet(source.getLines().toVector)
    finally source.close()
    end try
  end readFile

  /** Parses column blocks separated by columns containing only whitespace.
    *
    * Each block's non-empty rows before the last row are parsed as operands, and the last row must
    * contain the block's `+` or `*` operator. Padding shorter lines with spaces keeps all row
    * slices aligned without relying on the operands' horizontal alignment.
    *
    * @param lines
    *   the worksheet's rows, including the operator row
    * @return
    *   the individual arithmetic problems from left to right
    */
  private def parseWorksheet(lines: Seq[String]): Seq[WorksheetProblem] =
    require(lines.length >= 2, "worksheet must contain operands and an operator row")
    val width = lines.map(_.length).max
    val paddedLines = lines.map(_.padTo(width, ' '))
    val activeColumns = (0 until width).filter: column =>
      paddedLines.exists(line => !line(column).isWhitespace)

    val columnBlocks = activeColumns.foldLeft(Vector.empty[(Int, Int)]): (blocks, column) =>
      blocks.lastOption match
        case Some((start, end)) if end == column => blocks.init :+ (start, column + 1)
        case _ => blocks :+ (column, column + 1)

    val problems = columnBlocks.map: (start, end) =>
      val operandRows = paddedLines.dropRight(1).map(_.slice(start, end).trim).filter(_.nonEmpty)
      val operatorText = paddedLines.last.slice(start, end).trim
      require(operandRows.nonEmpty, s"problem at columns ${start}-${end} has no operands")
      require(operatorText.length == 1, s"problem at columns ${start}-${end} must have one operator")
      val operator = operatorText.head
      require(operator == '+' || operator == '*', s"invalid worksheet operator: ${operator}")
      WorksheetProblem(operandRows.map(BigInt(_)), operator)

    require(problems.nonEmpty, "worksheet must contain at least one problem")
    problems
  end parseWorksheet

  /** Evaluates each problem and sums the individual results.
    *
    * Addition and multiplication are applied only within their own vertical column block; the
    * resulting values are then added to produce the worksheet's grand total.
    *
    * @param problems
    *   the parsed worksheet problems
    * @return
    *   the sum of all problem results
    */
  def part1(problems: Seq[WorksheetProblem]): BigInt =
    require(problems.nonEmpty, "problems.nonEmpty")
    logger.debug(s"problems: ${problems.size}")

    problems.foldLeft(BigInt(0)): (total, problem) =>
      require(problem.numbers.nonEmpty, "problem.numbers.nonEmpty")
      val result = problem.operator match
        case '+' => problem.numbers.sum
        case '*' => problem.numbers.product
        case operator => throw new IllegalArgumentException(s"invalid worksheet operator: ${operator}")
      total + result
  end part1

  /** Part 2 is outside the scope of the current implementation.
    *
    * @param problems
    *   the worksheet problems, accepted to match the daily solution API
    * @return
    *   zero as the explicit part-2 placeholder
    */
  def part2(problems: Seq[WorksheetProblem]): Int =
    require(problems.nonEmpty, "problems.nonEmpty")
    0
  end part2

end Day06
