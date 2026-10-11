package aoc2025

import com.typesafe.scalalogging.Logger
import scala.io.Source

/** Day06 - Trash Compactor.
  *
  * A cephalopod math worksheet lists problems side by side in a wide horizontal layout. Each problem
  * has its numbers arranged one per line in a vertical strip, with the operator (`+` or `*`) at the
  * bottom of that strip. Problems are separated by a full column of only spaces, and the left/right
  * alignment of numbers within a problem is irrelevant. Part 1 combines each problem's numbers with
  * its operator and sums every result into a grand total.
  */
object Day06:
  val logger: Logger = Logger(this.getClass.getName)

  /** A single worksheet problem: an operator (`'+'` or `'*'`) and its numbers in top-to-bottom
    * order.
    */
  final case class Problem(operator: Char, numbers: Seq[Long]):
    require(operator == '+' || operator == '*', s"operator must be '+' or '*', got '$operator'")
    require(numbers.nonEmpty, "numbers.nonEmpty")

  /** Parse a worksheet into its problems from a classpath resource such as `inputs/Day06.txt`.
    *
    * Every line but the last is a row of digits/spaces, and the last line is the operator row. The
    * lines are padded to equal width (alignment within a problem is ignored, so padding only needs
    * to be consistent), and a column is a separator when every line is blank at that position. Each
    * maximal run of non-separator columns is one problem: its operator is the single non-blank
    * character in that run of the operator row, and its numbers are the non-blank tokens read from
    * each number row within that run. With `n` lines of width `w`, this is O(n * w) time and O(n)
    * space.
    *
    * @param filename
    *   resource path of the worksheet
    * @return
    *   the problems in left-to-right order
    */
  def readFile(filename: String): Seq[Problem] =
    require(filename.nonEmpty, "filename.nonEmpty")
    logger.debug(s"filename: ${filename}")

    val source = Source.fromResource(filename)
    val linesNoTrim =
      try source.getLines().toSeq
      finally source.close()
      end try
    require(linesNoTrim.size >= 2, "worksheet needs at least one number row and an operator row")

    val width = linesNoTrim.map(_.length).max
    val lines = linesNoTrim.map(_.padTo(width, ' '))
    val numberRows = lines.dropRight(1)
    val operatorRow = lines.last

    def isSeparator(column: Int): Boolean = lines.forall(_ (column) == ' ')

    val problems = Vector.newBuilder[Problem]
    var column = 0
    while column < width do
      if isSeparator(column) then column += 1
      else
        val start = column
        while column < width && !isSeparator(column) do column += 1
        val end = column - 1

        val operator = operatorRow
          .substring(start, end + 1)
          .find(_ != ' ')
          .getOrElse(throw IllegalArgumentException(s"problem has no operator in columns $start..$end"))

        val numbers = numberRows.flatMap: row =>
          val token = row.substring(start, end + 1).trim
          if token.nonEmpty then Some(token.toLong) else None

        problems += Problem(operator, numbers)
      end if
    end while

    problems.result()
  end readFile

  /** Combine a problem's numbers with its operator.
    *
    * For multiplication the fold starts at 1 (so a single factor yields that factor); for addition
    * it starts at 0 (so a single addend yields itself). This is O(m) time for `m` numbers and O(1)
    * extra space.
    *
    * @param problem
    *   the problem to evaluate
    * @return
    *   the value of the problem
    */
  def evaluate(problem: Problem): Long = problem.operator match
    case '*' => problem.numbers.foldLeft(1L)(_ * _)
    case '+' => problem.numbers.foldLeft(0L)(_ + _)
  end evaluate

  /** Sum the values of every problem on the worksheet.
    *
    * Each problem is evaluated via [[evaluate]] and the results are added together. With `p`
    * problems of at most `m` numbers each this is O(p * m) time and O(1) extra space.
    *
    * @param problems
    *   the problems in worksheet order
    * @return
    *   the grand total
    */
  def part1(problems: Seq[Problem]): Long =
    require(problems.nonEmpty, "problems.nonEmpty")
    logger.debug(s"problems: ${problems.size}")

    problems.foldLeft(0L)((sum, problem) => sum + evaluate(problem))
  end part1

end Day06