package aoc2025

import com.typesafe.scalalogging.Logger

/** Connects the closest junction-box pairs and measures the largest resulting circuits.
  *
  * Every unordered pair is ranked by squared Euclidean distance, then the requested number of
  * closest pairs are connected using a disjoint-set structure. Squared distances preserve the
  * Euclidean ordering without computing square roots.
  */
object Day08:
  val logger: Logger = Logger(this.getClass.getName)

  /** A junction box's location in three-dimensional space.
    *
    * @param x
    *   the X coordinate
    * @param y
    *   the Y coordinate
    * @param z
    *   the Z coordinate
    */
  final case class JunctionBox(x: Int, y: Int, z: Int)

  /** Reads junction-box coordinates from a classpath resource.
    *
    * @param filename
    *   resource path such as `inputs/Day08.txt`
    * @return
    *   coordinates in input order
    */
  def readFile(filename: String): Seq[JunctionBox] =
    import scala.io.Source

    require(filename.nonEmpty, "filename.nonEmpty")
    logger.debug(s"filename: ${filename}")

    val source = Source.fromResource(filename)
    try
      source.getLines().map: line =>
        val coordinates = line.split(",").map(_.toInt)
        require(coordinates.length == 3, s"expected three coordinates: ${line}")
        JunctionBox(coordinates(0), coordinates(1), coordinates(2))
      .toVector
    finally source.close()
    end try
  end readFile

  /** Multiplies the sizes of the three largest circuits after connecting the closest pairs.
    *
    * The algorithm generates each unordered pair once, orders pairs by squared distance, and
    * applies exactly `connectionCount` connections, including pairs whose endpoints are already
    * in the same circuit. The final component sizes are sorted descending and the largest three
    * are multiplied.
    *
    * @param boxes
    *   the junction-box coordinates
    * @param connectionCount
    *   the number of closest pairs to process
    * @return
    *   the product of the three largest circuit sizes
    */
  def part1(boxes: Seq[JunctionBox], connectionCount: Int = 1000): BigInt =
    require(boxes.size >= 3, "at least three junction boxes are required")
    require(connectionCount >= 0, "connectionCount must not be negative")
    val possiblePairCount = boxes.size.toLong * (boxes.size - 1) / 2
    require(connectionCount <= possiblePairCount, "connectionCount exceeds the number of pairs")
    logger.debug(s"junction boxes: ${boxes.size}, connections: ${connectionCount}")

    val pairs = for
      first <- boxes.indices
      second <- first + 1 until boxes.size
    yield (squaredDistance(boxes(first), boxes(second)), first, second)

    val circuits = new DisjointSet(boxes.size)
    pairs.sortBy(_._1).take(connectionCount).foreach: (_, first, second) =>
      circuits.union(first, second)

    circuits.componentSizes.sorted.takeRight(3).map(BigInt(_)).product
  end part1

  /** Computes squared Euclidean distance between two junction boxes.
    *
    * Long arithmetic avoids overflow for the puzzle's integer coordinates while preserving exact
    * distance ordering.
    *
    * @param first
    *   the first box
    * @param second
    *   the second box
    * @return
    *   the sum of squared coordinate differences
    */
  private def squaredDistance(first: JunctionBox, second: JunctionBox): Long =
    val dx = first.x.toLong - second.x
    val dy = first.y.toLong - second.y
    val dz = first.z.toLong - second.z
    dx * dx + dy * dy + dz * dz
  end squaredDistance

  /** Tracks connected components with path compression and union by size. */
  private final class DisjointSet(size: Int):
    private val parents = Array.tabulate(size)(identity)
    private val sizes = Array.fill(size)(1)

    /** Finds a node's root while compressing the traversed path.
      *
      * @param node
      *   the node whose root is requested
      * @return
      *   the component root
      */
    private def find(node: Int): Int =
      if parents(node) != node then parents(node) = find(parents(node))
      parents(node)
    end find

    /** Joins the components containing two nodes, if they are distinct.
      *
      * @param first
      *   the first node
      * @param second
      *   the second node
      */
    def union(first: Int, second: Int): Unit =
      val firstRoot = find(first)
      val secondRoot = find(second)
      if firstRoot != secondRoot then
        val (largerRoot, smallerRoot) =
          if sizes(firstRoot) >= sizes(secondRoot) then (firstRoot, secondRoot)
          else (secondRoot, firstRoot)
        parents(smallerRoot) = largerRoot
        sizes(largerRoot) += sizes(smallerRoot)
    end union

    /** Returns sizes of all disjoint components. */
    def componentSizes: Seq[Int] =
      parents.indices.filter(node => find(node) == node).map(sizes).toVector
    end componentSizes
  end DisjointSet

  /** Part 2 is outside the scope of the current implementation.
    *
    * @param boxes
    *   the junction boxes, accepted to match the daily solution API
    * @return
    *   zero as the explicit part-2 placeholder
    */
  def part2(boxes: Seq[JunctionBox]): Int =
    require(boxes.nonEmpty, "boxes.nonEmpty")
    0
  end part2

end Day08
