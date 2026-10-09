package aoc2025

import com.typesafe.scalalogging.Logger
import scala.io.Source

/** Day 2: sum product IDs whose decimal representation is two identical nonempty halves.
  *
  * For a half of `k` digits with numeric value `x`, its repeated ID is `x * (10^k + 1)`.
  * Within each inclusive input range, the qualifying values of `x` form an interval: intersect
  * the bounds obtained by division with the `k`-digit bounds, then sum the resulting arithmetic
  * progression. This avoids scanning every ID in a potentially large range. With `d` digits in
  * the largest endpoint, each range takes O(d) BigInt arithmetic operations.
  */
object Day02:
  val logger: Logger = Logger(this.getClass.getName)

  /** An inclusive interval of positive product IDs. */
  final case class IdRange(first: BigInt, last: BigInt):
    require(first > 0 && last >= first, "range must contain positive IDs")

  private val rangePattern = raw"(\d+)-(\d+)".r

  /** Parse comma-separated inclusive ranges from a classpath resource such as `inputs/Day02.txt`.
    *
    * Whitespace around ranges, including a line break in the example, is ignored.
    *
    * @return ranges in input order
    */
  def readFile(filename: String): Seq[IdRange] =
    require(filename.nonEmpty, "filename.nonEmpty")
    val source = Source.fromResource(filename)
    try
      source.mkString.trim.split(",").toVector.map(_.trim).map {
        case rangePattern(first, last) => IdRange(BigInt(first), BigInt(last))
        case text => throw new IllegalArgumentException(s"Invalid ID range: $text")
      }
    finally source.close()
  end readFile

  /** Sum all IDs consisting of exactly two copies of a digit sequence in each inclusive range.
    *
    * For each possible half-length, divide the interval endpoints by `10^k + 1` to find the
    * smallest and largest eligible halves. Restrict these to `k` digits so leading zeroes never
    * occur. The sum from `low` through `high` is `(low + high) * (high - low + 1) / 2`; multiply
    * by `10^k + 1` to recover the sum of the repeated IDs. BigInt prevents overflow.
    *
    * @return the sum of all invalid IDs in the supplied ranges
    */
  def part1(ranges: Seq[IdRange]): BigInt =
    ranges.iterator.map { range =>
      (1 to range.last.toString.length / 2).iterator.map { k =>
        val power = BigInt(10).pow(k)
        val factor = power + 1
        val low = (range.first + factor - 1) / factor max (power / 10)
        val high = (range.last / factor) min (power - 1)
        if low > high then BigInt(0)
        else factor * (low + high) * (high - low + 1) / 2
      }.sum
    }.sum
  end part1

  /** Part 2 also accepts sequences repeated more than twice; it is out of scope for now. */
  def part2(ranges: Seq[IdRange]): BigInt = ???
end Day02
