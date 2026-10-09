package aoc2025

import scala.io.Source

import com.typesafe.scalalogging.Logger

/** Day 8: connect nearby junction boxes and measure the resulting circuits.
  *
  * Each pair's squared Euclidean distance orders pairs exactly like its straight-line distance,
  * without requiring floating-point arithmetic. Sorting all pairs and applying the first 1000 with
  * a disjoint-set structure models the circuit merges. For n boxes, pair generation uses O(n²)
  * space and sorting takes O(n² log n) time; union by size and path compression make each of the
  * selected pair attempts amortized O(α(n)). Mutation is confined to the local disjoint-set arrays.
  */
object Day08:
  val logger: Logger = Logger(this.getClass.getName)

  /** A junction box's integer coordinates in three-dimensional space. */
  case class Point(x: Long, y: Long, z: Long)

  /** Read comma-separated 3D coordinates from a classpath resource.
    *
    * @return one junction-box position per input line
    */
  def readFile(filename: String): Vector[Point] =
    require(filename.nonEmpty, "filename.nonEmpty")
    val source = Source.fromResource(filename)
    try
      source
        .getLines()
        .map: line =>
          line.split(",").map(_.trim.toLong).toList match
            case x :: y :: z :: Nil => Point(x, y, z)
            case _                  => throw new IllegalArgumentException(s"Invalid coordinate: $line")
        .toVector
    finally source.close()

  /** Multiply the sizes of the three largest circuits after connecting the closest pairs.
    *
    * Generate every unordered pair, rank by squared distance, and apply the requested number of
    * pairs; pairs already in one circuit still count toward that count. Union-find joins components
    * and tracks their sizes, after which the three largest sizes are multiplied. Squared distance
    * preserves Euclidean ordering and avoids floating-point calculations.
    *
    * @param points the junction boxes to connect
    * @param pairCount the number of closest pairs to attempt (1000 for the puzzle input)
    * @return the product of the three largest circuit sizes
    */
  def part1(points: Seq[Point], pairCount: Int = 1000): Long =
    require(points.size >= 3, "at least three junction boxes are required")
    require(pairCount >= 0, "pairCount must be nonnegative")
    val pairs =
      for
        i <- points.indices
        j <- (i + 1) until points.size
      yield
        val a = points(i)
        val b = points(j)
        val dx = a.x - b.x
        val dy = a.y - b.y
        val dz = a.z - b.z
        (dx * dx + dy * dy + dz * dz, i, j)

    val parent = Array.tabulate(points.size)(identity)
    val sizes = Array.fill(points.size)(1)

    /** Find the circuit representative and compress the traversed parent path. */
    def rootOf(index: Int): Int =
      var root = index
      while parent(root) != root do root = parent(root)
      var current = index
      while parent(current) != current do
        val next = parent(current)
        parent(current) = root
        current = next
      root

    pairs.sortBy(_._1).take(pairCount).foreach:
      case (_, left, right) =>
        val leftRoot = rootOf(left)
        val rightRoot = rootOf(right)
        if leftRoot != rightRoot then
          if sizes(leftRoot) < sizes(rightRoot) then
            parent(leftRoot) = rightRoot
            sizes(rightRoot) += sizes(leftRoot)
          else
            parent(rightRoot) = leftRoot
            sizes(leftRoot) += sizes(rightRoot)
        else ()

    points.indices.iterator
      .withFilter(index => parent(index) == index)
      .map(sizes(_))
      .toSeq
      .sorted(using Ordering[Int].reverse)
      .take(3)
      .map(_.toLong)
      .product

  /** Part 2 connects boxes until one circuit remains; it is intentionally out of scope. */
  def part2(points: Seq[Point]): Long = ???
end Day08
