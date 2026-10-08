package aoc2025

import com.typesafe.scalalogging.Logger

/** Day04 counts paper rolls that forklifts can access from fewer than four adjacent rolls.
  *
  * Each roll is checked against the eight surrounding grid positions; positions outside the grid
  * are empty.
  */
object Day04:
  val logger: Logger = Logger(this.getClass.getName)

  /** Reads the paper-roll diagram from a classpath resource.
    *
    * @param filename
    *   resource path such as `inputs/Day04.txt`
    * @return
    *   the diagram rows in input order
    */
  def readFile(filename: String): Seq[String] =
    import scala.io.Source

    require(filename.nonEmpty, "filename.nonEmpty")
    logger.debug(s"filename: ${filename}")

    val source = Source.fromResource(filename)
    try source.getLines().map(_.trim).toSeq
    finally source.close()
    end try
  end readFile

  /** Counts rolls with fewer than four rolls in the eight immediately adjacent positions.
    *
    * For each roll, the method examines only in-bounds neighboring cells. The diagram is small, so
    * directly counting the eight possible neighbors is both clear and linear in the grid size.
    *
    * @param diagram
    *   a nonempty rectangular grid containing only `@` rolls and `.` empty cells
    * @return
    *   the number of accessible rolls
    */
  def part1(diagram: Seq[String]): Int =
    require(diagram.nonEmpty, "diagram.nonEmpty")
    require(diagram.head.nonEmpty, "diagram rows are nonempty")
    require(diagram.forall(_.length == diagram.head.length), "diagram is rectangular")
    require(
      diagram.forall(_.forall(cell => cell == '@' || cell == '.')),
      "diagram contains only '@' and '.'",
    )
    logger.debug(s"diagram: ${diagram.size}x${diagram.head.length}")

    val height = diagram.length
    val width = diagram.head.length

    diagram.indices.iterator.flatMap: row =>
      diagram(row).indices.iterator.collect:
        case col if diagram(row)(col) == '@' =>
          val adjacentRolls = (for
            neighborRow <- row - 1 to row + 1
            neighborCol <- col - 1 to col + 1
            if (neighborRow != row || neighborCol != col) && neighborRow >= 0 &&
              neighborRow < height && neighborCol >= 0 && neighborCol < width
          yield if diagram(neighborRow)(neighborCol) == '@' then 1 else 0).sum
          adjacentRolls
    .count(_ < 4)
  end part1

  /** Part 2 is outside the scope of the current implementation.
    *
    * @param diagram
    *   the paper-roll diagram, accepted to match the daily solution API
    * @return
    *   zero as the explicit part-2 placeholder
    */
  def part2(diagram: Seq[String]): Int =
    require(diagram.nonEmpty, "diagram.nonEmpty")
    0
  end part2

end Day04
