package aoc2025

import com.typesafe.scalalogging.Logger
import scala.io.Source

/** Day11 - Reactor (counting directed paths through a device-output DAG).
  *
  * A toroidal reactor is wired to a server rack through a tangle of devices. Each input line names
  * a device followed by the devices its outputs are attached to, so `bbb: ddd eee` means data flows
  * from `bbb` forward to `ddd` and `eee`. Data only ever moves forward through outputs, so the
  * wiring forms a directed acyclic graph. The puzzle asks how many distinct directed paths lead
  * from the device labelled `you` to the device labelled `out` (the reactor's main output).
  *
  * Because the graph is finite and acyclic, the number of paths from any device to `out` is the sum
  * of the counts of its outputs (directly-connected devices), with `out` contributing a base count
  * of one. This is evaluated with memoised depth-first traversal and then read off at `you`.
  */
object Day11:
  val logger: Logger = Logger(this.getClass.getName)

  /** The device a path starts from (attached next to you). */
  private val SourceDevice = "you"

  /** The main output device the paths must end at. */
  private val TargetDevice = "out"

  /** Read the device wiring from a classpath resource such as `inputs/Day11.txt`.
    *
    * Each non-empty line is `<device>: <output>...`, where the outputs may be empty. The result
    * maps each device name to the ordered list of devices its outputs feed. This is O(total input
    * size) time and O(number of devices) space.
    *
    * @param filename
    *   resource path of the wiring
    * @return
    *   a map from each device to its output devices, in input order of appearance
    */
  def readFile(filename: String): Map[String, Vector[String]] =
    require(filename.nonEmpty, "filename.nonEmpty")
    logger.debug(s"filename: ${filename}")

    val source = Source.fromResource(filename)
    val lines =
      try source.getLines().toVector.map(_.trim).filter(_.nonEmpty)
      finally source.close()
      end try

    lines.map: line =>
      val colon = line.indexOf(':')
      require(colon > 0, s"missing ':' separator: $line")
      val device = line.substring(0, colon).trim
      val outputs = line.substring(colon + 1).trim.split("\\s+").toVector.filter(_.nonEmpty)
      device -> outputs
    .toMap
  end readFile

  /** Count the distinct directed paths from one device to `out`.
    *
    * Every path starts at [[SourceDevice]] and ends at `TargetDevice`. Because the wiring is a
    * directed acyclic graph, a device's path count is the sum of the counts of the devices its
    * outputs attach to, with `out` itself contributing exactly one (the empty remainder of a path
    * that has already arrived). Memoisation stores each device's count once it has been computed,
    * so the whole traversal is O(v + e) time (each device and each edge is visited once).
    *
    * @param wiring
    *   the device-to-outputs map produced by [[readFile]]
    * @return
    *   the number of distinct paths from `you` to `out`; `0` when `unreachable`
    */
  def part1(wiring: Map[String, Vector[String]]): Long =
    require(wiring.contains(SourceDevice), s"wiring contains '$SourceDevice'")

    val memo = scala.collection.mutable.Map.empty[String, Long]

    def countPaths(device: String): Long = memo.getOrElseUpdate(
      device,
      if device == TargetDevice then 1L
      else
        wiring.getOrElse(device, Vector.empty).foldLeft(0L)((acc, next) => acc + countPaths(next)),
    )

    countPaths(SourceDevice)
  end part1

end Day11
