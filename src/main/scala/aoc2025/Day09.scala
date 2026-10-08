package aoc2025

import com.typesafe.scalalogging.Logger

/** Day09 - find the largest axis-aligned rectangle with red tiles at opposite corners.
  *
  * Every pair of red-tile coordinates defines a candidate. Since grid coordinates identify tiles,
  * each rectangle dimension includes both endpoint tiles, so a coordinate difference of d yields a
  * side length of d + 1. Checking every pair takes O(n²) time.
  */
object Day09:
  val logger: Logger = Logger(this.getClass.getName)

  /** Parse each input line as an (x, y) red-tile coordinate.
    *
    * @param filename
    *   resource path, such as `inputs/Day09.txt`
    * @return
    *   the red-tile coordinates in input order
    */
  def readFile(filename: String): Seq[(Long, Long)] =
    import scala.io.Source

    require(filename.nonEmpty, "filename.nonEmpty")
    logger.debug(s"filename: ${filename}")

    val source = Source.fromResource(filename)
    try source.getLines().toSeq.map: line =>
        val coordinates = line.trim.split(",")
        require(coordinates.length == 2, s"Expected an x,y coordinate: ${line}")
        (coordinates(0).toLong, coordinates(1).toLong)
    finally source.close()
    end try
  end readFile

  /** Find the maximum inclusive area over all pairs of red-tile coordinates.
    *
    * @param coordinates
    *   at least two red-tile coordinates
    * @return
    *   the largest rectangle area, counting both endpoint tiles in each dimension
    */
  def part1(coordinates: Seq[(Long, Long)]): Long =
    require(coordinates.length >= 2, "coordinates.length >= 2")

    coordinates.indices.flatMap: firstIndex =>
      ((firstIndex + 1) until coordinates.length).map: secondIndex =>
        val (x1, y1) = coordinates(firstIndex)
        val (x2, y2) = coordinates(secondIndex)
        (math.abs(x1 - x2) + 1L) * (math.abs(y1 - y2) + 1L)
    .max
  end part1

  /** Part two is outside the requested part-one scope and remains an explicit stub.
    *
    * @param coordinates
    *   red-tile coordinates
    * @return
    *   the placeholder value until part two is implemented
    */
  def part2(coordinates: Seq[(Long, Long)]): Long =
    require(coordinates.nonEmpty, "coordinates.nonEmpty")
    0L
  end part2

end Day09
