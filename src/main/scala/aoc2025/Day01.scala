package aoc2025

import com.typesafe.scalalogging.Logger

/** Day01 - Secret Entrance.
  *
  * The dial has positions from 0 through 99 and starts at 50. Part one follows each left or right
  * rotation modulo 100 and counts rotations whose ending position is zero.
  */
object Day01:
  val logger: Logger = Logger(this.getClass.getName)

  /** Reads each rotation instruction from the named classpath resource.
    *
    * @param filename
    *   the resource path, such as `inputs/Day01.txt`
    * @return
    *   the rotation instructions in their original order
    */
  def readFile(filename: String): Seq[String] =
    import scala.io.Source

    require(filename.nonEmpty, "filename.nonEmpty")
    logger.debug(s"filename: ${filename}")

    val source = Source.fromResource(filename)
    try source.getLines().toSeq.map(_.trim)
    finally source.close()
  end readFile

  /** Follows every rotation from position 50, wrapping around the 100-position dial, and counts
    * how many rotations end with the dial at zero.
    *
    * @param instructions
    *   left (`L`) and right (`R`) rotations, each followed by a non-negative distance
    * @return
    *   the number of rotations that leave the dial pointing at zero
    */
  def part1(instructions: Seq[String]): Int =
    require(instructions.nonEmpty, "instructions.nonEmpty")

    val (_, zeroCount) = instructions.foldLeft((50, 0)): (state, instruction) =>
      val (position, count) = state
      val nextPosition = instruction match
        case Rotation("L", distance) => (position - distance.toInt % 100 + 100) % 100
        case Rotation("R", distance) => (position + distance.toInt % 100) % 100
        case _ => throw new IllegalArgumentException(s"Invalid rotation instruction: ${instruction}")

      (nextPosition, count + (if nextPosition == 0 then 1 else 0))

    zeroCount
  end part1

  /** Part two is outside the scope of the Part One task and remains an explicit stub.
    *
    * @param instructions
    *   the rotation instructions, unused for this out-of-scope part
    * @return
    *   zero as a placeholder until Part Two is implemented
    */
  def part2(instructions: Seq[String]): Int = 0
  end part2

  private val Rotation = """([LR])(\d+)""".r

end Day01
