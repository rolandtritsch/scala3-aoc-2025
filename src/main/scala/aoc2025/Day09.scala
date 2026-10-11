package aoc2025

import com.typesafe.scalalogging.Logger
import scala.io.Source

/** Day09 - Movie Theater (red tiles and rectangles).
  *
  * The North Pole base movie theater has a tiled floor, and the Elves have marked some tiles red.
  * They want to find the largest axis-aligned rectangle that uses two red tiles as a pair of
  * opposite (diagonal) corners; the rectangle's edges run along the grid lines. Given the list of
  * red tile coordinates, part 1 returns the largest possible area (width times height) of any such
  * rectangle.
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
    * product. The span is `abs(a - b) + 1`, so a rectangle between two tiles sharing a row or column
    * is a valid (thin) rectangle with a height or width of one, exactly as in the problem example.
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
    best
  end part1

end Day09