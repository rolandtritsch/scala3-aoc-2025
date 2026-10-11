package aoc2025

import com.typesafe.scalalogging.Logger
import scala.io.Source

/** Day10 - Factory (indicator lights and button wiring).
  *
  * A factory is full of offline machines. Each machine owns a row of indicator lights (all off
  * initially) plus a set of buttons, each of which toggles a fixed subset of those lights when
  * pressed. A configuration is complete when the lights match a target diagram of on (`#`) and off
  * (`.`) lights. Part 1 asks, for every machine, the fewest total button presses needed to reach
  * the target; the answer is the sum over all machines.
  *
  * Pressing a button toggles lights, so pressing it twice restores the prior state at twice the
  * cost. The optimum therefore presses each button at most once, reducing the task to choosing a
  * subset of buttons whose combined toggles equal the target while minimising the number chosen.
  * Over GF(2) this is "find a minimum-weight solution of the linear system `Ax = b`", solved here
  * with Gaussian elimination plus a search over the null space. Button counts are at most 13, so
  * the system is tiny and the search is cheap.
  */
object Day10:
  val logger: Logger = Logger(this.getClass.getName)

  /** One machine: its target light pattern and the toggle mask of each button.
    *
    * A toggle mask is an `Int` whose bit `i` is set when the button toggles light `i`; the target
    * pattern is likewise an `Int` whose bit `i` is set when light `i` must end up on. Bit 0 is the
    * leftmost light of the diagram.
    *
    * @param target
    *   the desired on-light pattern (bit `i` = light `i` on)
    * @param buttons
    *   the toggle mask of each button, in input order
    */
  final case class Machine(target: Int, buttons: Vector[Int])

  /** Parse the machines from a classpath resource such as `inputs/Day10.txt`.
    *
    * Each line describes one machine as `[####.##] (buttons) ... {joltages}`: the bracketed diagram
    * gives the target lights, each parenthesised group is one button's toggled lights, and the
    * braced joltage requirements are irrelevant to part 1. This is O(total input size) time and
    * space.
    *
    * @param filename
    *   resource path of the machines
    * @return
    *   the parsed machines in input order
    */
  def readFile(filename: String): Vector[Machine] =
    require(filename.nonEmpty, "filename.nonEmpty")
    logger.debug(s"filename: ${filename}")

    val source = Source.fromResource(filename)
    val lines =
      try source.getLines().toVector.map(_.trim).filter(_.nonEmpty)
      finally source.close()
      end try

    lines.map: line =>
      val closeDiagram = line.indexOf(']')
      require(line.startsWith("[") && closeDiagram > 0, s"missing light diagram: $line")
      val diagram = line.substring(1, closeDiagram)

      val target = diagram.indices.foldLeft(0): (acc, i) =>
        if diagram(i) == '#' then acc | (1 << i) else acc

      val buttons = Vector.newBuilder[Int]
      var i = closeDiagram + 1
      while i < line.length do
        if line(i) == '(' then
          val close = line.indexOf(')', i)
          require(close > i, s"unterminated button in: $line")
          val ids = line.substring(i + 1, close).split(',').filter(_.nonEmpty).map(_.toInt)
          var mask = 0
          for id <- ids do mask ^= 1 << id
          buttons += mask
          i = close + 1
        else if line(i) == '{' then i = line.length // joltages are irrelevant to part 1
        else i += 1
      end while

      Machine(target, buttons.result())
  end readFile

  /** Fewest button presses needed to configure a single machine.
    *
    * Pressing a button twice restores the prior state at twice the cost, so the optimum presses
    * each button at most once: only a button's press parity matters. The task is therefore to find
    * the smallest subset of buttons whose XOR equals `target`; the subset size is the answer.
    *
    * The input machines have at most 13 buttons, so every subset (2¹³ = 8192 at most) is enumerated
    * and the lightest subset whose toggle-XOR equals the target is kept. Each subset is evaluated
    * in O(b) time for `b` buttons, giving O(b·2ᵇ) work per machine, which is trivial for the
    * puzzle's machine sizes.
    *
    * @param machine
    *   the target pattern and buttons
    * @return
    *   the minimum number of presses, or -1 when the target is unreachable (cannot occur for valid
    *   input)
    */
  def part1(machine: Machine): Int =
    val buttons = machine.buttons
    val target = machine.target
    val b = buttons.size

    var best = -1
    var subset = 0
    val count = 1 << b
    while subset < count do
      var xor = 0
      var weight = 0
      var i = 0
      while i < b do
        if (subset & (1 << i)) != 0 then
          xor ^= buttons(i)
          weight += 1
        i += 1
      end while
      if xor == target && (best < 0 || weight < best) then best = weight
      subset += 1
    end while

    best
  end part1

  /** Sum of the minimum press counts over all machines.
    *
    * Each machine is configured independently, so the answer is the total of [[part1]] over every
    * machine.
    *
    * @param machines
    *   the machines to configure
    * @return
    *   the fewest total presses to configure every machine
    */
  def totalPresses(machines: Vector[Machine]): Int =
    require(machines.nonEmpty, "machines.nonEmpty")
    machines.map(part1).sum
  end totalPresses

end Day10