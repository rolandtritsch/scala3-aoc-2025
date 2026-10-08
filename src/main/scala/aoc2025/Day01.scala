package aoc2025

import com.typesafe.scalalogging.Logger

/** Day01 - Secret Entrance
  *
  * Input files (`Day01.txt`, `Day01Test.txt`) contain rotation instructions, one per line:
  *   - Each line: [L|R][distance]
  *   - L means rotate left (toward lower numbers)
  *   - R means rotate right (toward higher numbers)
  *   - The dial has numbers 0 through 99 in order (100 positions)
  *   - The dial starts at position 50
  *
  * part1: Count how many times the dial points at 0 after any rotation in the sequence.
  *   Example from problem statement: 3
  *
  * part2: Not yet implemented (to be continued...)
  *
  * Uses modular arithmetic to handle the circular dial.
  */

object Day01: {
  val logger: Logger = Logger(this.getClass.getName)

  /** Represents a single rotation instruction */
  case class Rotation(direction: Char, distance: Int):
    require(direction == 'L' || direction == 'R', s"Direction must be L or R, got $direction")
    require(distance >= 0, s"Distance must be non-negative, got $distance")

    /** @return the file for the given filename as parsed rotation instructions */
    def readFile(filename: String): Seq[Rotation] = {
      import scala.io.Source

      require(filename.nonEmpty, "filename.nonEmpty")
      logger.debug(s"filename: ${filename}")

      val source = Source.fromFile(filename)
      try source.getLines().toSeq.map: line =>
        logger.debug(s"line: $line")
        val trimmed = line.trim
        require(trimmed.nonEmpty, "line must not be empty after trimming")
        val direction = trimmed.charAt(0)
        val distance = trimmed.substring(1).toInt
        Rotation(direction, distance)
      finally source.close()
    }

  /** Simulate the dial rotations and count how many times the dial points at 0 after any rotation
    *
    * @param rotations sequence of rotation instructions
    * @return count of times the dial points at 0 after any rotation
    */
  def part1(rotations: Seq[Rotation]): Int =
    require(rotations.nonEmpty, "rotations.nonEmpty")
    logger.debug(s"Number of rotations: ${rotations.length}")

    // Dial starts at position 50
    var position = 50
    var countAtZero = 0

    for rotation <- rotations do
      // Update position based on rotation
      position = rotation.direction match
        case 'L' => (position - rotation.distance) % 100
        case 'R' => (position + rotation.distance) % 100
        case _   => position // Should not happen due to validation in Rotation case class

      // Ensure position is non-negative (Scala's % can return negative values)
      if position < 0 then position += 100

      // Check if dial points at 0 after this rotation
      if position == 0 then countAtZero += 1

    logger.debug(s"Count at zero: $countAtZero")
    countAtZero
  }

  /** @return TODO: Part 2 not yet implemented */
  def part2(rotations: Seq[Rotation]): Int =
    require(rotations.nonEmpty, "rotations.nonEmpty")
    logger.debug("part2 not yet implemented")
    ??? // TODO: Implement part 2
  }

}