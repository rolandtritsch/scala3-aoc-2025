package aoc2025

import com.typesafe.scalalogging.Logger

/** Day01 solves the safe-dial rotations from the input sequence.
  *
  * The dial has positions from 0 through 99 and begins at 50. Part 1 counts how many rotations
  * finish with the dial at position 0.
  */
object Day01:
  val logger: Logger = Logger(this.getClass.getName)

  /** Reads each rotation instruction from a classpath resource.
    *
    * @param filename
    *   resource path such as `inputs/Day01.txt`
    * @return
    *   the rotation instructions in order
    */
  def readFile(filename: String): Seq[String] =
    import scala.io.Source

    require(filename.nonEmpty, "filename.nonEmpty")
    logger.debug(s"filename: ${filename}")

    val source = Source.fromResource(filename)
    try source.getLines().toSeq.map(_.trim)
    finally source.close()
    end try
  end readFile

  /** Applies every left or right rotation and counts those ending at zero.
    *
    * The position is kept modulo 100 after each instruction, so distances larger than one full dial
    * turn are handled without simulating individual clicks. Only the position after each complete
    * rotation contributes to the count.
    *
    * @param rotations
    *   instructions consisting of `L` or `R` followed by a non-negative distance
    * @return
    *   the number of rotations that end with the dial at zero
    */
  def part1(rotations: Seq[String]): Int =
    require(rotations.nonEmpty, "rotations.nonEmpty")
    logger.debug(s"rotations: ${rotations.size}")

    rotations.foldLeft((50, 0)): (state, instruction) =>
      val (position, zeroCount) = state
      require(instruction.length >= 2, "instruction.length >= 2")
      val direction = instruction.head
      require(direction == 'L' || direction == 'R', "direction is L or R")
      val distance = instruction.tail.toInt
      require(distance >= 0, "distance >= 0")
      val signedDistance = if direction == 'R' then distance else -distance
      val nextPosition = Math.floorMod(position + Math.floorMod(signedDistance, 100), 100)
      (nextPosition, zeroCount + (if nextPosition == 0 then 1 else 0))
    ._2
  end part1

  /** Part 2 is outside the scope of the current implementation.
    *
    * @param rotations
    *   the rotation instructions, accepted to match the daily solution API
    * @return
    *   zero as the explicit part-2 placeholder
    */
  def part2(rotations: Seq[String]): Int =
    require(rotations.nonEmpty, "rotations.nonEmpty")
    0
  end part2

end Day01
