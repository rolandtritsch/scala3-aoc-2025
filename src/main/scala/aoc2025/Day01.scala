package aoc2025

import scala.io.Source

import com.typesafe.scalalogging.Logger

/** Day01 - Secret Entrance.
  *
  * A safe's dial is labelled with the numbers 0 through 99 in order and starts pointing at 50. Each
  * input line is a rotation of the form `<direction><distance>`: `L` turns toward lower numbers
  * (wrapping 0 -> 99) and `R` turns toward higher numbers (wrapping 99 -> 0).
  *
  * part1 answers the number of rotations after which the dial is left pointing at 0.
  */
object Day01:
  val logger: Logger = Logger(this.getClass.getName)

  /** The direction of a rotation, encoded as the signed movement per click. */
  enum Direction(val delta: Int):
    case L extends Direction(-1)
    case R extends Direction(1)

  /** A single rotation: a direction and a nonnegative number of clicks. */
  final case class Rotation(direction: Direction, distance: Int):
    require(distance >= 0, "distance >= 0")

  private val Modulus = 100
  private val StartPosition = 50

  /** Parse one rotation per line from a classpath resource such as `inputs/Day01.txt`.
    *
    * @param filename
    *   resource path of the rotation sequence
    * @return
    *   the rotations in input order
    */
  def readFile(filename: String): Seq[Rotation] =
    require(filename.nonEmpty, "filename.nonEmpty")
    logger.debug(s"filename: ${filename}")

    val source = Source.fromResource(filename)
    try source.getLines().toSeq.map: line =>
        val trimmed = line.trim
        require(trimmed.length >= 2, s"Invalid rotation: $line")
        val direction = trimmed.head match
          case 'L' => Direction.L
          case 'R' => Direction.R
          case c   => throw IllegalArgumentException(s"Invalid direction '$c' in: $line")
        val distance = trimmed.tail.toInt
        Rotation(direction, distance)
    finally source.close()
    end try
  end readFile

  /** Advance the dial by a single rotation, wrapping around the 100 positions.
    *
    * The distance is reduced modulo the dial's 100 positions before applying the direction, so
    * arbitrarily large rotations cannot overflow the position arithmetic.
    *
    * @param position
    *   current dial position in `[0, 100)`
    * @param rotation
    *   the rotation to apply
    * @return
    *   the new dial position in `[0, 100)`
    */
  def advance(position: Int, rotation: Rotation): Int =
    require(position >= 0 && position < Modulus, s"position in [0, $Modulus)")
    val steps = rotation.distance % Modulus
    Math.floorMod(position + rotation.direction.delta * steps, Modulus)
  end advance

  /** Count the rotations that leave the dial pointing at 0, starting from 50.
    *
    * A single left-to-right fold applies each rotation to the running position and increments the
    * counter whenever the resulting position is 0. This is O(n) time and O(1) extra space.
    *
    * @param rotations
    *   the rotation sequence
    * @return
    *   the number of rotations ending at position 0
    */
  def part1(rotations: Seq[Rotation]): Int =
    val (_, count) = rotations.foldLeft((StartPosition, 0)) { case ((position, count), rotation) =>
      val next = advance(position, rotation)
      (next, count + (if next == 0 then 1 else 0))
    }
    count
  end part1

  /** Count the clicks of a single rotation that land the dial on 0.
    *
    * The dial moves one position per click, so a rotation of distance `d` visits `d` intermediate
    * positions. The first click that reaches 0 is `first` clicks away (100 when the dial already
    * points at 0, since a full lap is required to return to it). Every subsequent lap of 100 clicks
    * reaches 0 again, so the total is `(d - first) / 100 + 1` when `d >= first`, otherwise 0.
    *
    * @param position
    *   current dial position in `[0, 100)`
    * @param rotation
    *   the rotation to apply
    * @return
    *   the number of clicks (including the final one) that land on 0
    */
  private def zeroHits(position: Int, rotation: Rotation): Int =
    val first = rotation.direction match
      case Direction.R =>
        val k = (Modulus - position) % Modulus
        if k == 0 then Modulus else k
      case Direction.L =>
        val k = position % Modulus
        if k == 0 then Modulus else k
    val distance = rotation.distance
    if distance < first then 0 else (distance - first) / Modulus + 1
  end zeroHits

  /** Count every click that leaves the dial pointing at 0, starting from 50.
    *
    * Unlike part1, a rotation contributes one hit for every click that lands on 0, whether that
    * happens mid-rotation or at the end. A single left-to-right fold accumulates the running
    * position and the running count, so this is O(n) time and O(1) extra space.
    *
    * @param rotations
    *   the rotation sequence
    * @return
    *   the number of clicks landing on position 0
    */
  def part2(rotations: Seq[Rotation]): Int = rotations
    .foldLeft((StartPosition, 0)) { case ((position, count), rotation) =>
      (advance(position, rotation), count + zeroHits(position, rotation))
    }._2
  end part2

end Day01
