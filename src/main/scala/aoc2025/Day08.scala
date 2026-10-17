package aoc2025

import scala.io.Source

import com.typesafe.scalalogging.Logger

/** Day08 - Playground (junction boxes and string lights).
  *
  * The Elves have suspended a large number of junction boxes in 3D space and want to connect them
  * with strings of lights so electricity can reach every box. Connecting two boxes joins their
  * circuits. To save on string lights they connect the closest pairs first: repeatedly take the
  * closest pair of boxes that are not yet directly connected and join them (single-linkage
  * clustering). A pair whose two boxes are already in the same circuit does nothing.
  *
  * Part 1 connects the `connections` closest pairs and multiplies the sizes of the three largest
  * resulting circuits.
  */
object Day08:
  val logger: Logger = Logger(this.getClass.getName)

  /** A junction box position in 3D space. */
  final case class Point3D(x: Int, y: Int, z: Int)

  /** Read junction box positions from a classpath resource such as `inputs/Day08.txt`.
    *
    * Each non-empty line is a single `X,Y,Z` triple. This is O(n) time and O(n) space in the number
    * of lines.
    *
    * @param filename
    *   resource path of the positions
    * @return
    *   the junction box positions in input order
    */
  def readFile(filename: String): Vector[Point3D] =
    require(filename.nonEmpty, "filename.nonEmpty")
    logger.debug(s"filename: ${filename}")

    val source = Source.fromResource(filename)
    try source.getLines().toVector.map: line =>
        val parts = line.trim.split(',')
        require(parts.length == 3, s"expected an X,Y,Z triple: $line")
        Point3D(parts(0).toInt, parts(1).toInt, parts(2).toInt)
    finally source.close()
    end try
  end readFile

  /** The squared straight-line distance between two boxes.
    *
    * Squared distance preserves the ordering of straight-line distance, so it is sufficient for
    * selecting the closest pairs while avoiding floating-point error. Coordinates are at most
    * 99977, so the result fits comfortably in a Long.
    *
    * @param a
    *   one junction box
    * @param b
    *   another junction box
    * @return
    *   the squared Euclidean distance between them
    */
  def squaredDistance(a: Point3D, b: Point3D): Long =
    val dx = (a.x - b.x).toLong
    val dy = (a.y - b.y).toLong
    val dz = (a.z - b.z).toLong
    dx * dx + dy * dy + dz * dz
  end squaredDistance

  /** Connect the `connections` closest pairs and multiply the sizes of the three largest circuits.
    *
    * All n*(n-1)/2 unordered pairs are formed and sorted by squared distance. The `connections`
    * closest pairs are then processed in order with a union-find: a pair whose boxes are already in
    * the same circuit does nothing, otherwise the two circuits are merged. Finally the component
    * sizes are collected, sorted descending, and the three largest are multiplied. This is O(n^2
    * log n) time (dominated by sorting the pairs) and O(n^2) space.
    *
    * @param points
    *   the junction box positions
    * @param connections
    *   the number of closest pairs to connect
    * @return
    *   the product of the sizes of the three largest circuits
    */
  def part1(points: Vector[Point3D], connections: Int): Long =
    require(points.nonEmpty, "points.nonEmpty")
    require(connections >= 0, "connections >= 0")
    logger.debug(s"points: ${points.size}, connections: $connections")

    val n = points.size
    val parent = Array.tabulate(n)(identity)
    val size = Array.fill(n)(1)

    def find(x: Int): Int =
      var root = x
      while parent(root) != root do root = parent(root)
      var cur = x
      while parent(cur) != root do
        val next = parent(cur)
        parent(cur) = root
        cur = next
      root
    end find

    def union(a: Int, b: Int): Unit =
      val ra = find(a)
      val rb = find(b)
      if ra != rb then
        if size(ra) < size(rb) then
          parent(ra) = rb
          size(rb) += size(ra)
        else
          parent(rb) = ra
          size(ra) += size(rb)
      end if
    end union

    val pairs =
      for
        i <- 0 until n
        j <- (i + 1) until n
      yield (squaredDistance(points(i), points(j)), i, j)
    val sorted = pairs.sortBy(_._1)

    for k <- 0 until math.min(connections, sorted.length) do
      val (_, i, j) = sorted(k)
      union(i, j)

    val componentSizes = Array.fill(n)(0)
    for i <- 0 until n do componentSizes(find(i)) += 1
    val sizes = componentSizes.filter(_ > 0).sorted.reverse
    sizes.take(3).map(_.toLong).product
  end part1

  /** Connect closest pairs until every box is in one circuit; product of the last two boxes' X.
    *
    * The same sorted all-pairs edge list as [[part1]] is consumed in ascending distance order by a
    * union-find, but instead of stopping after a fixed number of connections, processing continues
    * until a single merge unites every box into one circuit. That (final) merge is the edge the
    * Elves connect last, so its two endpoints are the pair whose X coordinates are multiplied. Ties
    * are broken by the stable index order of the pair list, matching [[part1]]. This is O(n^2 log
    * n) time (sorting dominates) and O(n^2) space; the loop body runs at most n - 1 successful
    * merges.
    *
    * @param points
    *   the junction box positions
    * @return
    *   the product of the X coordinates of the last two boxes connected
    */
  def part2(points: Vector[Point3D]): Long =
    require(points.nonEmpty, "points.nonEmpty")
    logger.debug(s"points: ${points.size}")

    val n = points.size
    val parent = Array.tabulate(n)(identity)
    val size = Array.fill(n)(1)

    def find(x: Int): Int =
      var root = x
      while parent(root) != root do root = parent(root)
      var cur = x
      while parent(cur) != root do
        val next = parent(cur)
        parent(cur) = root
        cur = next
      root
    end find

    var lastX = 0L

    val pairs =
      for
        i <- 0 until n
        j <- (i + 1) until n
      yield (squaredDistance(points(i), points(j)), i, j)
    val sorted = pairs.sortBy(_._1)

    var connected = false
    var idx = 0
    while !connected && idx < sorted.length do
      val (_, i, j) = sorted(idx)
      val ra = find(i)
      val rb = find(j)
      if ra != rb then
        val newRoot =
          if size(ra) < size(rb) then
            parent(ra) = rb
            size(rb) += size(ra)
            rb
          else
            parent(rb) = ra
            size(ra) += size(rb)
            ra
        lastX = points(i).x.toLong * points(j).x.toLong
        connected = size(newRoot) == n
      end if
      idx += 1
    end while

    lastX
  end part2

end Day08
