package aoc2025

import com.typesafe.scalalogging.Logger

/** Day10 - find the minimum button presses that configure each machine's lights.
  *
  * Pressing a button twice cancels its effect, so an optimal configuration presses each button at
  * most once. Enumerating button subsets therefore finds the minimum, and Gray-code ordering lets
  * each candidate configuration be derived by toggling only the button that changed.
  */
object Day10:
  val logger: Logger = Logger(this.getClass.getName)

  /** A machine's desired light pattern and the light-toggle masks of its buttons.
    *
    * @param target
    *   bit mask of the lights that should be on
    * @param buttons
    *   bit mask toggled by each button
    */
  final case class Machine(target: Int, buttons: Vector[Int])

  /** Parse one manual line into its target light mask and button masks.
    *
    * @param line
    *   one line containing a light diagram, button schematics, and joltage data
    * @return
    *   the parsed machine; joltage requirements are intentionally ignored
    */
  private def parseMachine(line: String): Machine =
    val tokens = line.trim.split("\\s+").toVector
    require(tokens.length >= 3, s"Expected a diagram, buttons, and joltage data: ${line}")
    val diagram = tokens.head
    require(diagram.startsWith("[") && diagram.endsWith("]"), s"Invalid light diagram: ${diagram}")
    val pattern = diagram.drop(1).dropRight(1)
    require(pattern.nonEmpty, "The light diagram must contain at least one light")
    require(pattern.length < 31, "The light diagram must fit in an Int bit mask")
    val target = pattern.zipWithIndex.foldLeft(0): (mask, light) =>
      val (state, index) = light
      require(state == '.' || state == '#', s"Invalid light state: ${state}")
      if state == '#' then mask | (1 << index) else mask

    val buttonTokens = tokens.drop(1).takeWhile(_.startsWith("("))
    require(buttonTokens.nonEmpty, s"Expected at least one button: ${line}")
    val buttons = buttonTokens.map: token =>
      require(token.endsWith(")"), s"Invalid button schematic: ${token}")
      val lights = token.drop(1).dropRight(1).split(",").toVector.map(_.toInt)
      lights.foldLeft(0): (mask, index) =>
        require(index >= 0 && index < pattern.length, s"Light index out of range: ${index}")
        mask | (1 << index)
    require(buttons.length < 31, "The button count must fit in an Int subset mask")
    Machine(target, buttons)
  end parseMachine

  /** Read and parse all machine descriptions from a classpath resource.
    *
    * @param filename
    *   resource path such as `inputs/Day10.txt`
    * @return
    *   the machines in input order
    */
  def readFile(filename: String): Seq[Machine] =
    import scala.io.Source

    require(filename.nonEmpty, "filename.nonEmpty")
    logger.debug(s"filename: ${filename}")

    val source = Source.fromResource(filename)
    try source.getLines().toSeq.map(parseMachine)
    finally source.close()
    end try
  end readFile

  /** Find the fewest button presses that produce each target, then sum those minima.
    *
    * @param machines
    *   the machines whose light patterns must be configured
    * @return
    *   the minimum total number of presses across all machines
    */
  def part1(machines: Seq[Machine]): Int =
    require(machines.nonEmpty, "machines.nonEmpty")

    machines.map: machine =>
      val combinations = 1 << machine.buttons.length
      var configuration = 0
      var previousGrayCode = 0
      var minimum = Int.MaxValue
      var subset = 0

      while subset < combinations do
        val grayCode = subset ^ (subset >>> 1)
        if subset > 0 then
          val changedButton = Integer.numberOfTrailingZeros(grayCode ^ previousGrayCode)
          configuration ^= machine.buttons(changedButton)
        if configuration == machine.target then
          minimum = math.min(minimum, Integer.bitCount(grayCode))
        previousGrayCode = grayCode
        subset += 1
      end while

      require(minimum != Int.MaxValue, s"Machine target is unreachable: ${machine}")
      minimum
    .sum
  end part1

  /** Part two is outside the requested part-one scope and remains an explicit stub.
    *
    * @param machines
    *   the parsed machine descriptions
    * @return
    *   the placeholder value until part two is implemented
    */
  def part2(machines: Seq[Machine]): Int =
    require(machines.nonEmpty, "machines.nonEmpty")
    0
  end part2

end Day10
