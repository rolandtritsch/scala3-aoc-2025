package aoc2025

import com.typesafe.scalalogging.Logger

/** Day11 - count directed paths from the device labeled `you` to the device labeled `out`.
  *
  * The device network is a directed acyclic graph. Memoized depth-first search counts paths from
  * each device once, adding the path counts of its outputs; this avoids enumerating every complete
  * path separately and uses `BigInt` to accommodate large path counts.
  */
object Day11:
  val logger: Logger = Logger(this.getClass.getName)

  /** A device and the devices connected to its outputs.
    *
    * @param name
    *   the device label
    * @param outputs
    *   labels of devices receiving data from this device
    */
  final case class Device(name: String, outputs: Vector[String])

  /** Read and parse device connections from a classpath resource.
    *
    * @param filename
    *   resource path, such as `inputs/Day11.txt`
    * @return
    *   parsed devices in input order
    */
  def readFile(filename: String): Seq[Device] =
    import scala.io.Source

    require(filename.nonEmpty, "filename.nonEmpty")
    logger.debug(s"filename: ${filename}")

    val source = Source.fromResource(filename)
    try source.getLines().toSeq.map: line =>
        val parts = line.split(":", 2).map(_.trim)
        require(parts.length == 2 && parts(0).nonEmpty, s"Invalid device definition: ${line}")
        val outputs = parts(1).split("\\s+").filter(_.nonEmpty).toVector
        Device(parts(0), outputs)
    finally source.close()
    end try
  end readFile

  /** Count all directed paths from `you` to `out`.
    *
    * A memoized depth-first search assigns one path to `out` itself and sums the counts of all
    * outgoing neighbors for every other device. A recursion-stack set rejects cyclic inputs, since
    * a cycle on a route to the destination would create infinitely many directed walks.
    *
    * @param devices
    *   the directed device network
    * @return
    *   the number of distinct paths from `you` to `out`
    */
  def part1(devices: Seq[Device]): BigInt =
    require(devices.nonEmpty, "devices.nonEmpty")
    val connections = devices.map(device => device.name -> device.outputs).toMap
    require(connections.contains("you"), "The network must contain `you`")

    val counts = scala.collection.mutable.Map.empty[String, BigInt]
    val visiting = scala.collection.mutable.Set.empty[String]

    def countPaths(device: String): BigInt =
      if device == "out" then BigInt(1)
      else
        counts.getOrElseUpdate(
          device, {
            require(!visiting.contains(device), s"Cycle detected at device: ${device}")
            visiting.add(device)
            val count = connections.getOrElse(device, Vector.empty).map(countPaths).sum
            visiting.remove(device)
            count
          },
        )

    countPaths("you")
  end part1

  /** Part two is outside the requested part-one scope and remains an explicit stub.
    *
    * @param devices
    *   the parsed device network
    * @return
    *   the placeholder value until part two is implemented
    */
  def part2(devices: Seq[Device]): BigInt =
    require(devices.nonEmpty, "devices.nonEmpty")
    BigInt(0)
  end part2

end Day11
