package aoc2025

import com.typesafe.scalalogging.Logger
import scala.io.Source

/** Day 1: track the dial's final position after each rotation.
  *
  * The dial has 100 positions and starts at 50. Each instruction adds or subtracts its distance
  * modulo 100; only the position after the entire rotation counts for part 1, not intermediate
  * clicks. A single left-to-right pass takes O(n) time and O(1) additional space.
  */
object Day01:
  val logger: Logger = Logger(this.getClass.getName)

  /** A rotation toward lower (`L`) or higher (`R`) numbers by a nonnegative number of clicks. */
  final case class Rotation(direction: Char, distance: Int):
    require(direction == 'L' || direction == 'R', "direction must be L or R")
    require(distance >= 0, "distance must be nonnegative")

  private val rotationPattern = raw"([LR])(\d+)".r

  /** Parse one rotation per line from a classpath resource such as `inputs/Day01.txt`.
    *
    * @return
    *   rotations in the same order as the input
    */
  def readFile(filename: String): Seq[Rotation] =
    require(filename.nonEmpty, "filename.nonEmpty")
    val source = Source.fromResource(filename)
    try source.getLines().map {
        case rotationPattern(direction, distance) => Rotation(direction.head, distance.toInt)
        case line => throw new IllegalArgumentException(s"Invalid rotation: $line")
      }.toVector
    finally source.close()
    end try
  end readFile

  /** Apply all rotations starting at 50, counting only rotations whose ending position is zero.
    *
    * Reducing each distance modulo 100 avoids overflowing the position arithmetic for large
    * rotations. `floorMod` handles negative (leftward) movement across zero correctly.
    *
    * @return
    *   the number of rotations that leave the dial at zero
    */
  def part1(rotations: Seq[Rotation]): Int = rotations
    .foldLeft((50, 0)) { case ((position, count), rotation) =>
      val steps = rotation.distance % 100
      val displacement = if rotation.direction == 'L' then -steps else steps
      val next = Math.floorMod(position + displacement, 100)
      (next, count + (if next == 0 then 1 else 0))
    }._2
  end part1

  /** Part 2 counts zero at every click, including mid-rotation; it is out of scope for now. */
  def part2(rotations: Seq[Rotation]): Int = ???
end Day01
