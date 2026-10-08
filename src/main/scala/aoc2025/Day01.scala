package aoc2025

import com.typesafe.scalalogging.Logger

/** Day01 - Secret Entrance.
  *
  * The dial is represented by a position in the range 0 through 99. Rotations are parsed as signed
  * distances, with left rotations negative and right rotations positive. Part 1 counts only the
  * positions reached after each complete rotation.
  */
object Day01:
  val logger: Logger = Logger(this.getClass.getName)

  /** Parses one rotation into a signed distance: left is negative and right is positive. */
  private def parseRotation(line: String): Long =
    line.trim match
      case s"L$distance" => -distance.toLong
      case s"R$distance" => distance.toLong
      case invalid => throw new IllegalArgumentException(s"Invalid rotation: $invalid")
  end parseRotation

  /** Reads the named classpath resource as signed rotation distances. */
  def readFile(filename: String): Seq[Long] =
    import scala.io.Source

    require(filename.nonEmpty, "filename.nonEmpty")
    logger.debug(s"filename: ${filename}")

    val source = Source.fromResource(filename)
    try source.getLines().toVector.map(parseRotation)
    finally source.close()
    end try
  end readFile

  /**
    * Follows each rotation from position 50, wrapping modulo 100, and returns the number of
    * rotations whose ending position is zero.
    */
  def part1(rotations: Seq[Long]): Int =
    require(rotations.nonEmpty, "rotations.nonEmpty")
    logger.debug(s"rotations: ${rotations}")

    rotations.foldLeft((50L, 0)): (state, rotation) =>
      val (position, count) = state
      val nextPosition = Math.floorMod(position + rotation, 100L)
      (nextPosition, count + (if nextPosition == 0 then 1 else 0))
    ._2
  end part1

  /**
    * Part 2 is intentionally out of scope for this task; returns the explicit placeholder value
    * until that puzzle part is implemented.
    */
  def part2(rotations: Seq[Long]): Int = 0

end Day01
