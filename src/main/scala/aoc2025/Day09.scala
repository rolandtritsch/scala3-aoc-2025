package aoc2025

import scala.io.Source

import com.typesafe.scalalogging.Logger

/** Day09 - Movie Theater (red tiles and rectangles).
  *
  * The North Pole base movie theater has a tiled floor, and the Elves have marked some tiles red.
  * They want to find the largest axis-aligned rectangle that uses two red tiles as a pair of
  * opposite (diagonal) corners; the rectangle's edges run along the grid lines. Given the list of
  * red tile coordinates, part 1 returns the largest possible area (width times height) of any such
  * rectangle.
  *
  * Part 2 restricts the rectangle to tiles that are red or green. In the input, every red tile is
  * connected to the red tile before and after it (the list wraps, so the last also connects to the
  * first) by a straight line of green tiles, and every tile strictly inside that closed loop is
  * also green. The listed red tiles are therefore the vertices of a simple rectilinear polygon; the
  * usable region is exactly the polygon's filled interior including its boundary. Part 2 returns
  * the largest rectangle using two red tiles as opposite corners whose entire cell span lies inside
  * that region.
  */
object Day09:
  val logger: Logger = Logger(this.getClass.getName)

  /** A red tile position on the grid. */
  final case class Tile(x: Long, y: Long)

  /** Read red tile coordinates from a classpath resource such as `inputs/Day09.txt`.
    *
    * Each non-empty line is a single `X,Y` pair. This is O(n) time and O(n) space in the number of
    * lines.
    *
    * @param filename
    *   resource path of the coordinates
    * @return
    *   the red tile coordinates in input order
    */
  def readFile(filename: String): Vector[Tile] =
    require(filename.nonEmpty, "filename.nonEmpty")
    logger.debug(s"filename: ${filename}")

    val source = Source.fromResource(filename)
    try source.getLines().toVector.map: line =>
        val parts = line.trim.split(',')
        require(parts.length == 2, s"expected an X,Y pair: $line")
        Tile(parts(0).toLong, parts(1).toLong)
    finally source.close()
    end try
  end readFile

  /** The area of the axis-aligned rectangle whose opposite corners are two red tiles.
    *
    * Because the rectangle's edges run along grid lines, its width is the inclusive span of the two
    * X coordinates and its height is the inclusive span of the two Y coordinates; the area is their
    * product. The span is `abs(a - b) + 1`, so a rectangle between two tiles sharing a row or
    * column is a valid (thin) rectangle with a height or width of one, exactly as in the problem
    * example.
    *
    * @param a
    *   one corner tile
    * @param b
    *   the opposite corner tile
    * @return
    *   the area `(|ax - bx| + 1) * (|ay - by| + 1)`
    */
  def area(a: Tile, b: Tile): Long =
    val width = math.abs(a.x - b.x) + 1
    val height = math.abs(a.y - b.y) + 1
    width * height
  end area

  /** Largest area of any rectangle using two red tiles as opposite corners.
    *
    * Every unordered pair of red tiles is considered as a pair of opposite corners, and the maximum
    * [[area]] is returned. This is O(n²) time and O(1) additional space for n tiles.
    *
    * @param tiles
    *   the red tile coordinates
    * @return
    *   the largest achievable area
    */
  def part1(tiles: Vector[Tile]): Long =
    require(tiles.size >= 2, "need at least two red tiles")
    logger.debug(s"tiles: ${tiles.size}")

    var best = 0L
    for
      i <- 0 until tiles.size
      j <- (i + 1) until tiles.size
    do
      val a = area(tiles(i), tiles(j))
      if a > best then best = a
    end for
    best
  end part1

  /** An axis-aligned segment of the red-tile loop.
    *
    * The loop connects each red tile to the next (wrapping around), and consecutive tiles always
    * share a row or a column, so every segment is either horizontal or vertical. Horizontal
    * segments are identified by `vertical == false`, `fixed == y` and an inclusive x range
    * `[lo, hi]`; vertical segments by `vertical == true`, `fixed == x` and an inclusive y range
    * `[lo, hi]`.
    *
    * @param vertical
    *   whether the segment runs along a column (`true`) or a row (`false`)
    * @param fixed
    *   the constant coordinate (x for vertical, y for horizontal)
    * @param lo
    *   the lower inclusive bound of the varying coordinate
    * @param hi
    *   the upper inclusive bound of the varying coordinate
    */
  private final case class Segment(vertical: Boolean, fixed: Long, lo: Long, hi: Long)

  /** Build the closed loop of segments between consecutive red tiles.
    *
    * Each red tile is connected to the next in input order by a straight segment, and the last tile
    * connects back to the first. The result has one segment per red tile. This is O(n) time and
    * O(n) space for n tiles.
    *
    * @param tiles
    *   the red tile coordinates in input order
    * @return
    *   the axis-aligned segments closing the loop
    */
  private def segments(tiles: Vector[Tile]): Vector[Segment] =
    val count = tiles.size
    (0 until count).map: i =>
      val a = tiles(i)
      val b = tiles((i + 1) % count)
      if a.x == b.x then
        Segment(vertical = true, fixed = a.x, lo = math.min(a.y, b.y), hi = math.max(a.y, b.y))
      else Segment(vertical = false, fixed = a.y, lo = math.min(a.x, b.x), hi = math.max(a.x, b.x))
    .toVector
  end segments

  /** Whether the tile at `(x, y)` lies inside (or on the boundary of) the loop.
    *
    * The loop is treated as the closed rectilinear polygon whose edges are `segs` and whose red
    * vertices sit at tile centers. A tile is usable when its center is on an edge (a red or green
    * boundary tile) or strictly inside the polygon (a green interior tile). "On edge" is checked
    * directly for both horizontal and vertical segments; strict inclusion uses the even-odd rule,
    * casting a horizontal ray to the right and counting only vertical segments that strictly
    * straddle the tile's row (`y` in `[lo, hi)`), which scores a segment's bottom endpoint but not
    * its top endpoint so shared vertices are counted once. This is O(s) time and O(1) space for s
    * segments.
    *
    * @param x
    *   the tile column
    * @param y
    *   the tile row
    * @param segs
    *   the loop segments
    * @return
    *   true when the tile is usable (red or green)
    */
  private def inRegion(x: Long, y: Long, segs: Vector[Segment]): Boolean =
    var crossings = 0
    var onEdge = false
    var i = 0
    while i < segs.length && !onEdge do
      val s = segs(i)
      if s.vertical then
        if x == s.fixed && y >= s.lo && y <= s.hi then onEdge = true
        else if x < s.fixed && y >= s.lo && y < s.hi then crossings += 1
      else if y == s.fixed && x >= s.lo && x <= s.hi then onEdge = true
      i += 1
    end while
    onEdge || (crossings % 2 == 1)
  end inRegion

  /** Split one axis into contiguous blocks, one per distinct coordinate and one per interior gap.
    *
    * Because every loop segment lies along a red-tile row or column, the usable/interior state of a
    * tile can only change when crossing a distinct red x (column) or red y (row). Splitting at all
    * distinct coordinates, plus one block per gap of whole cells between consecutive distinct
    * coordinates, therefore makes `inRegion` constant within any block. Each block is represented
    * by its lowest tile coordinate. Returns `(representatives, representativeToIndex)`.
    *
    * @param sorted
    *   the distinct coordinate values in ascending order
    * @return
    *   one representative per block, and the block index of each distinct value
    */
  private def axisBlocks(sorted: Vector[Long]): (Vector[Long], Map[Long, Int]) =
    val reps = Vector.newBuilder[Long]
    val index = Map.newBuilder[Long, Int]
    var block = 0
    for i <- sorted.indices do
      val value = sorted(i)
      reps += value
      index += (value -> block)
      block += 1
      if i + 1 < sorted.length then
        val next = sorted(i + 1)
        if next > value + 1 then
          reps += (value + 1)
          block += 1
      end if
    end for
    (reps.result(), index.result())
  end axisBlocks

  /** Largest area of any red-cornered rectangle made only of red or green tiles.
    *
    * The usable range is the interior plus boundary of the loop through the red tiles. The axes are
    * split into blocks using [[axisBlocks]] and a 2D prefix sum of unusable blocks is built, after
    * which every unordered pair of red tiles can be tested for full containment in O(1). The
    * overall cost is O(n²) for the pair scan plus O(bx·by) to fill (bx/by are the block counts,
    * each at most 2·d − 1 for d distinct coordinates).
    *
    * @param tiles
    *   the red tile coordinates in input order
    * @return
    *   the largest enclosed area
    */
  def part2(tiles: Vector[Tile]): Long =
    require(tiles.size >= 4, "need at least four red tiles to close a loop")
    logger.debug(s"tiles: ${tiles.size}")

    val segs = segments(tiles)
    val xs = tiles.map(_.x).distinct.sorted.toVector
    val ys = tiles.map(_.y).distinct.sorted.toVector
    val (xBlocks, xIndex) = axisBlocks(xs)
    val (yBlocks, yIndex) = axisBlocks(ys)
    val nx = xBlocks.size
    val ny = yBlocks.size

    val prefix = Array.ofDim[Int](nx + 1, ny + 1)
    for
      bx <- 0 until nx
      by <- 0 until ny
    do
      val outOfRegion = if inRegion(xBlocks(bx), yBlocks(by), segs) then 0 else 1
      prefix(bx + 1)(by + 1) = outOfRegion + prefix(bx)(by + 1) + prefix(bx + 1)(by) -
        prefix(bx)(by)
    end for

    def fullyInside(a: Tile, b: Tile): Boolean =
      val bx1 = xIndex(math.min(a.x, b.x))
      val bx2 = xIndex(math.max(a.x, b.x))
      val by1 = yIndex(math.min(a.y, b.y))
      val by2 = yIndex(math.max(a.y, b.y))
      val unusable = prefix(bx2 + 1)(by2 + 1) - prefix(bx1)(by2 + 1) - prefix(bx2 + 1)(by1) +
        prefix(bx1)(by1)
      unusable == 0
    end fullyInside

    var best = 0L
    val count = tiles.size
    for
      i <- 0 until count
      j <- (i + 1) until count
    do
      val a = tiles(i)
      val b = tiles(j)
      if fullyInside(a, b) then
        val ar = area(a, b)
        if ar > best then best = ar
    end for
    best
  end part2

end Day09
