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

  /** The device a part-two path starts from (the server rack). */
  private val Part2SourceDevice = "svr"

  /** The devices every part-two path must visit (in any order). */
  private val RequiredDevices = Vector("dac", "fft")

  /** Count the distinct directed paths from `svr` to `out` that visit both `dac` and `fft`.
    *
    * Part two moves the source to `svr` and additionally requires each counted path to pass through
    * both of the devices `dac` and `fft`, in any order. Data still flows only forward, so the
    * wiring remains a directed acyclic graph. Each required device is assigned a bit, and a path's
    * state is the set of required devices visited so far; at `out` a path counts only when every
    * required bit is set. The recursion is memoised on `(device, visitedMask)`, giving O(v * 2^k +
    * e * 2^k) time and space where `k` is the number of required devices (two here) and `v`/`e` the
    * device/edge counts.
    *
    * @param wiring
    *   the device-to-outputs map produced by [[readFile]]
    * @return
    *   the number of distinct paths from `svr` to `out` visiting both `dac` and `fft`; `0` when no
    *   such path exists or `svr` has none
    */
  def part2(wiring: Map[String, Vector[String]]): Long =
    require(wiring.contains(Part2SourceDevice), s"wiring contains '$Part2SourceDevice'")
    require(RequiredDevices.nonEmpty, "at least one required device")

    val requiredMask = RequiredDevices.zipWithIndex.map((name, i) => name -> (1 << i)).toMap
    val requiredFull = (1 << RequiredDevices.size) - 1

    val memo = scala.collection.mutable.Map.empty[(String, Int), Long]

    def countPaths(device: String, visited: Int): Long = memo.getOrElseUpdate(
      (device, visited),
      if device == TargetDevice then (if visited == requiredFull then 1L else 0L)
      else
        val nextVisited = requiredMask.get(device) match
          case Some(bit) => visited | bit
          case None      => visited
        wiring.getOrElse(device, Vector.empty)
          .foldLeft(0L)((acc, next) => acc + countPaths(next, nextVisited)),
    )

    countPaths(Part2SourceDevice, 0)
  end part2

end Day11
