package aoc2025

import scala.collection.mutable
import scala.io.Source

import com.typesafe.scalalogging.Logger

/** Day12 - Christmas Tree Farm (packing presents into the regions under trees).
  *
  * The input lists a handful of small, odd present shapes (each drawn as `#` cells in a small
  * diagram) and then, for every tree, the width and length of the region beneath it together with
  * how many presents of each shape must fit inside. A present may be rotated and reflected but must
  * sit exactly on the unit grid, and the occupied (`#`) cells of two different presents may not
  * occupy the same grid cell; the unoccupied (`.`) cells of one present's diagram do not block
  * another present. Part one asks how many regions can contain every listed present at once.
  *
  * A region can only fit when the total number of occupied cells of all of its presents does not
  * exceed the region's area. Because the shapes are tiny this area bound is in practice the
  * deciding test, but the solver still runs a complete backtracking placement search (including the
  * freedom to leave a cell empty) so genuine geometric impossibilities are detected too.
  */
object Day12:
  val logger: Logger = Logger(this.getClass.getName)

  /** A present shape: its occupied (`#`) cells as (column, row) offsets from the shape's top-left
    * (minimum) corner.
    */
  final case class Shape(offsets: Vector[(Int, Int)]):
    require(offsets.nonEmpty, "a shape must have at least one occupied cell")

    /** Number of occupied cells; every present of this shape covers exactly this many cells. */
    val cellCount: Int = offsets.size

  end Shape

  /** A region under one tree together with how many presents of each shape it must hold. */
  final case class Region(width: Int, height: Int, quantities: Vector[Int]):
    require(width > 0 && height > 0, s"non-empty region: ${width}x$height")
    require(quantities.nonEmpty, "a region needs per-shape quantities")

    /** Number of unit cells the region provides. */
    def area: Int = width * height

  end Region

  /** Parsed puzzle input: the standard present shapes followed by the regions. */
  final case class Input(shapes: Vector[Shape], regions: Vector[Region])

  /** Read the shape definitions and the regions from a classpath resource such as
    * `inputs/Day12.txt`.
    *
    * Shape diagrams start with a line `N:` followed by the rendered `#`/`.` grid of the shape. Each
    * region line is `WxH: q0 q1 q2 q3 q4 q5`, where `W`/`H` are the region's width and length and
    * each `q` is how many presents of that shape index are required. This is O(input size) time and
    * O(input size) space.
    *
    * @param filename
    *   resource path of the puzzle input
    * @return
    *   the parsed present shapes and regions
    */
  def readFile(filename: String): Input =
    require(filename.nonEmpty, "filename.nonEmpty")
    logger.debug(s"filename: $filename")

    val source = Source.fromResource(filename)
    val lines =
      try source.getLines().toVector.map(_.trim)
      finally source.close()
      end try

    val shapes = lines.zipWithIndex.collect { case (line, i) if line.matches("[0-9]+:") => i }
      .map: i =>
        val diagram = lines.slice(i + 1, i + 4)
        require(diagram.size == 3, s"expected a 3-row diagram after '${lines(i)}'")
        parseShape(diagram)

    val regions = lines.withFilter(_.matches("[0-9]+x[0-9]+:.*")).map(parseRegion)
    Input(shapes, regions)
  end readFile

  private def parseShape(diagram: IndexedSeq[String]): Shape =
    val offsets =
      for
        (row, y) <- diagram.zipWithIndex
        (cell, x) <- row.zipWithIndex if cell == '#'
      yield (x, y)
    Shape(offsets.toVector)
  end parseShape

  private def parseRegion(line: String): Region =
    val dims = line.takeWhile(_ != ':').split('x')
    require(dims.length == 2, s"expected a 'WxH:' region: $line")
    val width = dims(0).toInt
    val height = dims(1).toInt
    val quantities = line.dropWhile(_ != ':').drop(1).trim.split("\\s+").withFilter(_.nonEmpty)
      .map(_.toInt).toVector
    Region(width, height, quantities)
  end parseRegion

  /** Count how many regions can hold every listed present.
    *
    * For each region the total number of occupied cells is compared against the region area; when
    * it exceeds the area the region is discarded immediately. Otherwise a complete backtracking
    * search places the presents one at a time, always anchoring the next present at the first
    * still-empty grid cell (which either hosts the top-left corner of some remaining present or is
    * left empty). Rotation and reflection are handled by generating every distinct orientation of
    * each shape once and translating those orientations across the region. The search is complete,
    * so a negative answer is only returned once every reachable placement has been tried.
    *
    * @param input
    *   the parsed shapes and regions produced by [[readFile]]
    * @return
    *   the number of regions that can fit all of their listed presents
    */
  def part1(input: Input): Int =
    require(input.shapes.nonEmpty, "at least one present shape")
    require(input.regions.nonEmpty, "at least one region")

    val orientationsByShape = input.shapes.map(orientations)
    val cellCounts = input.shapes.map(_.cellCount)
    input.regions.count(region => fits(region, orientationsByShape, cellCounts))
  end part1

  private def fits(
      region: Region,
      orientationsByShape: Vector[Vector[Shape]],
      cellCounts: Vector[Int],
  ): Boolean =
    val area = region.area
    val totalCells = cellCounts.zip(region.quantities).map((c, q) => c * q).sum
    if totalCells > area then false else canPack(region, orientationsByShape, totalCells)
  end fits

  /** The eight symmetries of the unit square applied to a shape, deduplicated and translated back
    * to the top-left corner. Rotating and reflecting a shape never changes which presents it can
    * form, so this precomputation captures every placement mode up to translation.
    */
  private def orientations(shape: Shape): Vector[Shape] =
    val result = mutable.Set.empty[Set[(Int, Int)]]
    var cells = shape.offsets.toSet
    for _ <- 0 until 4 do
      result += cells
      result += reflectX(cells)
      cells = rotate90(cells)
    result.toVector.map(normalizeShape).distinct
  end orientations

  private def reflectX(cells: Set[(Int, Int)]): Set[(Int, Int)] = cells.map((x, y) => (x, -y))

  private def rotate90(cells: Set[(Int, Int)]): Set[(Int, Int)] = cells.map((x, y) => (y, -x))

  private def normalizeShape(cells: Set[(Int, Int)]): Shape =
    val minX = cells.map(_._1).min
    val minY = cells.map(_._2).min
    Shape(cells.map((x, y) => (x - minX, y - minY)).toVector.sorted)
  end normalizeShape

  /** Build every placement (shape index plus the cell indices it occupies) of every still-needed
    * shape, keyed by the placement's first cell so the backtracking search can anchor on it.
    */
  private def placementsByAnchor(
      region: Region,
      orientationsByShape: Vector[Vector[Shape]],
  ): Map[Int, Vector[(Int, Vector[Int])]] =
    val w = region.width
    val h = region.height
    val placements =
      for
        si <- orientationsByShape.indices if region.quantities(si) > 0
        ori <- orientationsByShape(si)
        maxX = ori.offsets.map(_._1).max
        maxY = ori.offsets.map(_._2).max
        dy <- 0 to (h - maxY - 1)
        dx <- 0 to (w - maxX - 1)
      yield
        val cells = ori.offsets.map((x, y) => (y + dy) * w + (x + dx))
        (cells.min, si, cells)
    placements.groupMap(_._1)(p => (p._2, p._3)).view.mapValues(_.toVector).toMap
  end placementsByAnchor

  private def canPack(
      region: Region,
      orientationsByShape: Vector[Vector[Shape]],
      totalCells: Int,
  ): Boolean =
    val area = region.area
    val occupied = new Array[Boolean](area)
    val remaining = region.quantities.toArray
    val anchors = placementsByAnchor(region, orientationsByShape)

    def search(from: Int, emptyAllowed: Int): Boolean =
      if remaining.forall(_ == 0) then true
      else
        var cell = from
        while cell < area && occupied(cell) do cell += 1
        if cell >= area then false
        else
          var found = false
          val candidates = anchors.getOrElse(cell, Vector.empty)
          var i = 0
          while !found && i < candidates.length do
            val (shapeIndex, cells) = candidates(i)
            i += 1
            if remaining(shapeIndex) > 0 && cells.forall(c => !occupied(c)) then
              remaining(shapeIndex) -= 1
              cells.foreach(c => occupied(c) = true)
              found = search(cell + 1, emptyAllowed)
              cells.foreach(c => occupied(c) = false)
              remaining(shapeIndex) += 1
            end if
          end while
          if !found && emptyAllowed > 0 then
            occupied(cell) = true
            found = search(cell + 1, emptyAllowed - 1)
            occupied(cell) = false
          found
        end if
      end if
    end search

    search(0, area - totalCells)
  end canPack

end Day12
