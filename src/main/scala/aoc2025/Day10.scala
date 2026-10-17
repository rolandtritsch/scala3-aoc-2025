package aoc2025

import scala.io.Source

import com.typesafe.scalalogging.Logger

/** Day10 - Factory (indicator lights, button wiring, and joltage counters).
  *
  * A factory is full of offline machines. Each machine owns a row of indicator lights (all off
  * initially) plus a set of buttons, each of which names a fixed subset of those lights. Part 1
  * asks, for every machine, the fewest total button presses needed to toggle the lights into a
  * target diagram; the answer is the sum over all machines.
  *
  * Part 2 ignores the diagrams and repurposes the same button schematics: each machine also has one
  * numeric joltage counter per requirement (all initially zero), and each button now increments a
  * named subset of counters by one per press. The task is the fewest total presses to drive every
  * counter to its target joltage, again summed over all machines.
  *
  * Pressing a button toggles lights (part 1), so pressing it twice restores the prior state at
  * twice the cost; the optimum therefore presses each button at most once, reducing that part to a
  * minimum-weight solution of a linear system over GF(2).
  *
  * Counter increments (part 2) are additive rather than toggling, so every counter must be driven
  * to an exact non-negative total. That is an integer linear program: minimise `sum x_j` subject to
  * `A x = b`, `x >= 0`, where column `j` of `A` is the 0/1 incidence of button `j` over the
  * counters and `b` is the joltage target. This is solved with an exact branch-and-bound over the
  * continuous relaxation (a simplex over rational arithmetic picks the fractional variables to
  * branch on, and an upper bound per button of the smallest target among the counters it touches
  * bounds the search).
  */
object Day10:
  val logger: Logger = Logger(this.getClass.getName)

  /** One machine: its target light pattern, the toggle mask of each button, and its joltage
    * targets.
    *
    * A toggle mask is an `Int` whose bit `i` is set when the button toggles light `i`; the target
    * pattern is likewise an `Int` whose bit `i` is set when light `i` must end up on. Bit 0 is the
    * leftmost light of the diagram. The same bit `i` names counter `i` in part 2, so `buttons`
    * doubles as each button's counter-incidence mask.
    *
    * @param target
    *   the desired on-light pattern (bit `i` = light `i` on)
    * @param buttons
    *   the toggle (and counter) mask of each button, in input order
    * @param joltages
    *   the target value of each joltage counter, in order (empty when the caller only needs part 1,
    *   e.g. tests that build a machine by hand)
    */
  final case class Machine(target: Int, buttons: Vector[Int], joltages: Vector[Int] = Vector.empty)

  /** Parse the machines from a classpath resource such as `inputs/Day10.txt`.
    *
    * Each line describes one machine as `[####.##] (buttons) ... {joltages}`: the bracketed diagram
    * gives the target lights, each parenthesised group is one button's named lights/counters, and
    * the braced list is the joltage targets (used only by part 2). This is O(total input size) time
    * and space.
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
      val joltages = Vector.newBuilder[Int]
      var i = closeDiagram + 1
      while i < line.length do
        if line(i) == '(' then
          val close = line.indexOf(')', i)
          require(close > i, s"unterminated button in: $line")
          val ids = line.substring(i + 1, close).split(',').withFilter(_.nonEmpty).map(_.toInt)
          var mask = 0
          for id <- ids do mask ^= 1 << id
          buttons += mask
          i = close + 1
        else if line(i) == '{' then
          val close = line.indexOf('}', i)
          require(close > i, s"unterminated joltages in: $line")
          for tok <- line.substring(i + 1, close).split(',').withFilter(_.nonEmpty).map(_.toInt) do
            joltages += tok
          i = close + 1
        else i += 1
      end while

      Machine(target, buttons.result(), joltages.result())
  end readFile

  /** Fewest button presses needed to configure a single machine's indicator lights (part 1).
    *
    * Pressing a button twice restores the prior state at twice the cost, so the optimum presses
    * each button at most once: only a button's press parity matters. The task is therefore to find
    * the smallest subset of buttons whose XOR equals `target`; the subset size is the answer.
    *
    * The input machines have at most 13 buttons, so every subset (2¹³ = 8192 at most) is enumerated
    * and the lightest subset whose toggle-XOR equals the target is kept. O(b·2ᵇ) work per machine.
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

  /** Sum of the minimum press counts over all machines (part 1).
    *
    * Each machine is configured independently, so the answer is the total of [[part1]] over every
    * machine.
    *
    * @param machines
    *   the machines to configure
    * @return
    *   the fewest total presses to configure every machine's lights
    */
  def totalPresses(machines: Vector[Machine]): Int =
    require(machines.nonEmpty, "machines.nonEmpty")
    machines.map(part1).sum
  end totalPresses

  /** Fewest button presses needed to configure a single machine's joltage counters (part 2).
    *
    * Every button now increments its named counters by exactly one per press, so each counter must
    * receive a press count that sums to its target. Minimising the total presses is the integer
    * program `min sum x: A x = b, x >= 0` solved exactly by [[CounterSolver.minimalPresses]].
    *
    * @param machine
    *   the button incidence masks and joltage targets
    * @return
    *   the minimum number of presses to match every joltage counter
    */
  def part2(machine: Machine): Int =
    require(machine.joltages.nonEmpty, "machine.joltages.nonEmpty")
    CounterSolver.minimalPresses(machine.buttons, machine.joltages)
  end part2

  /** Sum of the minimum press counts over all machines' joltage counters (part 2).
    *
    * @param machines
    *   the machines to configure
    * @return
    *   the fewest total presses to configure every machine's joltage counters
    */
  def totalJoltagePresses(machines: Vector[Machine]): Long =
    require(machines.nonEmpty, "machines.nonEmpty")
    machines.map(m => part2(m).toLong).sum
  end totalJoltagePresses

end Day10

/** Exact solver for the additive button-press minimum (Day 10 part 2).
  *
  * Each button is a 0/1 column over the counters: pressing button `j` once adds one to every
  * counter whose bit is set in its mask. The target is the joltage vector `b`, all counters start
  * at zero, and we minimise the total number of presses. This is the integer program
  *
  * {{{
  *   minimise   sum_j x_j
  *   subject to for every counter i: sum_{j: bit i in mask j} x_j == b_i,   x_j >= 0 integer
  * }}}
  *
  * solved with branch-and-bound over its linear-programming relaxation. A tableau simplex on exact
  * rational numbers yields the fractional lower bound and the variable to branch on; each button is
  * additionally capped by the smallest target among the counters it touches (pressing it more would
  * overshoot that counter). The button matrices are tiny (at most 13 buttons and 10 counters), so
  * the search converges immediately.
  */
private object CounterSolver:

  /** Immutable exact rational, kept in lowest terms with a positive denominator. */
  private final class R private (val n: BigInt, val d: BigInt):
    def +(o: R): R = R(n * o.d + o.n * d, d * o.d)
    def -(o: R): R = R(n * o.d - o.n * d, d * o.d)
    def *(o: R): R = R(n * o.n, d * o.d)
    def unary_- : R = R(-n, d)
    def <(o: R): Boolean = n * o.d < o.n * d
    def <=(o: R): Boolean = n * o.d <= o.n * d
    def >(o: R): Boolean = n * o.d > o.n * d
    def floor: BigInt = n / d
    def isInteger: Boolean = d == 1
    def toInt: Int = n.toInt

    override def equals(o: Any): Boolean = o match
      case r: R => n == r.n && d == r.d
      case _    => false

    override def hashCode: Int = 31 * n.hashCode + d.hashCode
    override def toString: String = if d == 1 then n.toString else s"$n/$d"
  end R

  private object R:

    def apply(n: BigInt, d: BigInt): R =
      if d == 0 then throw new ArithmeticException("division by zero")
      val g = n.gcd(d)
      val (nn, dd) = if d < 0 then (-n / g, -d / g) else (n / g, d / g)
      new R(nn, dd)

    end apply

    def apply(n: BigInt): R = new R(n, 1)
    val zero: R = new R(0, 1)
    val one: R = new R(1, 1)
  end R

  /** Exact tableau simplex: minimise `c·x` subject to `A x = b`, `x >= 0` (all `b_i >= 0`).
    *
    * Two-phase method with artificial variables to establish feasibility, then the original
    * objective. Returns `None` when infeasible, otherwise the optimal value and a primal solution.
    *
    * @param a
    *   the constraint matrix (rows x columns)
    * @param b
    *   the right-hand side
    * @param colCost
    *   the objective coefficients
    * @return
    *   the optimal value and a primal `x` (fractional), or `None` if infeasible
    */
  private def simplex(a: Array[Array[R]], b: Array[R], colCost: Array[R]): Option[(R, Array[R])] =
    val m = b.length
    val n = colCost.length
    val N = n + m

    val rows: Array[Array[R]] = Array.tabulate(m): r =>
      val row = Array.fill(N + 1)(R.zero)
      var j = 0
      while j < n do
        row(j) = a(r)(j)
        j += 1
      row(n + r) = R.one
      row(N) = b(r)
      row

    val basis: Array[Int] = Array.tabulate(m)(r => n + r)
    val red: Array[R] = Array.fill(N + 1)(R.zero)
    var r = 0
    while r < m do
      red(n + r) = R.one
      r += 1
    r = 0
    while r < m do
      var k = 0
      while k <= N do
        red(k) -= rows(r)(k)
        k += 1
      r += 1
    end while

    def pivot(e: Int, l: Int): Unit =
      val pv = rows(l)(e)
      var k = 0
      while k <= N do
        rows(l)(k) = R(rows(l)(k).n * pv.d, rows(l)(k).d * pv.n)
        k += 1
      var rr = 0
      while rr < m do
        if rr != l && (rows(rr) != null) then
          val f = rows(rr)(e)
          if !(f.n == 0) then
            k = 0
            while k <= N do
              rows(rr)(k) -= f * rows(l)(k)
              k += 1
          end if
        end if
        rr += 1
      end while
      val fr = red(e)
      k = 0
      while k <= N do
        red(k) -= fr * rows(l)(k)
        k += 1
      basis(l) = e
    end pivot

    def enter(): Int =
      var k = 0
      while k < N do
        if red(k).n < 0 then return k
        k += 1
      -1
    end enter

    def leave(e: Int): Int =
      var best: R = null
      var l = -1
      var rr = 0
      while rr < m do
        val ac = rows(rr)(e)
        if ac.n > 0 then
          val ratio = R(rows(rr)(N).n * ac.d, rows(rr)(N).d * ac.n)
          if best == null || ratio < best || (ratio == best && basis(rr) < basis(l)) then
            best = ratio
            l = rr
        end if
        rr += 1
      end while
      l
    end leave

    var e = enter()
    while e >= 0 do
      val l = leave(e)
      if l < 0 then return None
      pivot(e, l)
      e = enter()
    end while

    if red(N).n != 0 then return None // infeasible

    // drive residual artificials out of the basis
    var rr = 0
    while rr < m do
      if basis(rr) >= n then
        var j = 0
        while j < n && rows(rr)(j).n == 0 do j += 1
        if j < n then pivot(j, rr)
        else
          rows(rr) = null
          basis(rr) = -2
      end if
      rr += 1
    end while

    // phase two over the surviving rows and first n columns
    val live = rows.zipWithIndex.collect { case (row, i) if row != null => i }
    val m2 = live.length
    val tabled: Array[Array[R]] = live.toArray.map: i =>
      Array.tabulate(n + 1)(k => if k < n then rows(i)(k) else rows(i)(N))
    val basis2: Array[Int] = live.toArray.map(basis)

    val red2: Array[R] = Array.fill(n + 1)(R.zero)
    var j = 0
    while j < n do
      red2(j) = colCost(j)
      j += 1
    rr = 0
    while rr < m2 do
      val bi = basis2(rr)
      val cb = if bi >= 0 && bi < n then colCost(bi) else R.zero
      if !(cb.n == 0) then
        var k = 0
        while k <= n do
          red2(k) -= cb * tabled(rr)(k)
          k += 1
      end if
      rr += 1
    end while

    def enter2(): Int =
      var k = 0
      while k < n do
        if red2(k).n < 0 then return k
        k += 1
      -1
    end enter2

    def leave2(e: Int): Int =
      var best: R = null
      var l = -1
      var rr2 = 0
      while rr2 < m2 do
        val ac = tabled(rr2)(e)
        if ac.n > 0 then
          val ratio = R(tabled(rr2)(n).n * ac.d, tabled(rr2)(n).d * ac.n)
          if best == null || ratio < best || (ratio == best && basis2(rr2) < basis2(l)) then
            best = ratio
            l = rr2
        end if
        rr2 += 1
      end while
      l
    end leave2

    def pivot2(e: Int, l: Int): Unit =
      val pv = tabled(l)(e)
      var k = 0
      while k <= n do
        tabled(l)(k) = R(tabled(l)(k).n * pv.d, tabled(l)(k).d * pv.n)
        k += 1
      var r2 = 0
      while r2 < m2 do
        if r2 != l then
          val f = tabled(r2)(e)
          if !(f.n == 0) then
            k = 0
            while k <= n do
              tabled(r2)(k) -= f * tabled(l)(k)
              k += 1
          end if
        end if
        r2 += 1
      end while
      val fr = red2(e)
      k = 0
      while k <= n do
        red2(k) -= fr * tabled(l)(k)
        k += 1
      basis2(l) = e
    end pivot2

    var e2 = enter2()
    while e2 >= 0 do
      val l2 = leave2(e2)
      if l2 < 0 then return None
      pivot2(e2, l2)
      e2 = enter2()
    end while

    val value = R(-red2(n).n, red2(n).d)
    val x = Array.fill(n)(R.zero)
    rr = 0
    while rr < m2 do
      if basis2(rr) >= 0 && basis2(rr) < n then x(basis2(rr)) = tabled(rr)(n)
      rr += 1
    Some((value, x))
  end simplex

  /** Solve the continuous relaxation with per-variable lower/upper bounds.
    *
    * Bounds are folded into an equivalent unconstrained-nonnegativity problem: lower bounds shift
    * the right-hand side and upper bounds become slack inequalities. Returns `None` when
    * infeasible.
    */
  private def solveBounded(
      a: Array[Array[R]],
      b: Array[R],
      lb: Array[R],
      ub: Array[Int],
  ): Option[(R, Array[R])] =
    val m = b.length
    val n = lb.length

    val beq = b.clone()
    var j = 0
    while j < n do
      val lo = lb(j)
      if !(lo.n == 0) then
        var i = 0
        while i < m do
          val aij = a(i)(j)
          if !(aij.n == 0) then beq(i) -= lo * aij
          i += 1
      end if
      j += 1
    end while

    // collect <= rows: one per finite upper bound (all are finite here)
    val upperRows = Array.newBuilder[(Array[R], BigInt)]
    j = 0
    while j < n do
      val lo = lb(j)
      val hi = R(ub(j)) - lo
      val coeff = Array.fill(n)(R.zero)
      coeff(j) = R.one
      upperRows += ((coeff, hi.n))
      j += 1
    end while
    val ups = upperRows.result()
    val nle = ups.length

    val total = n + nle
    val neq = m + nle
    val big = Array.fill(neq)(Array.fill(total)(R.zero))
    val rhs = Array.fill(neq)(R.zero)
    var i = 0
    while i < m do
      j = 0
      while j < n do
        big(i)(j) = a(i)(j)
        j += 1
      rhs(i) = beq(i)
      i += 1
    end while
    var u = 0
    while u < nle do
      val coeff = ups(u)._1
      j = 0
      while j < n do
        big(m + u)(j) = coeff(j)
        j += 1
      big(m + u)(n + u) = R.one
      rhs(m + u) = R(ups(u)._2)
      u += 1
    end while

    val colCost = Array.fill(total)(R.zero)
    j = 0
    while j < n do
      colCost(j) = R.one
      j += 1

    // guard against a negative right-hand side (infeasible <= in this normalisation)
    i = 0
    while i < neq do
      if rhs(i).n < 0 then return None
      i += 1

    simplex(big, rhs, colCost).map: (value, xs) =>
      val x = Array.tabulate(n)(k => xs(k) + lb(k))
      // The normalised objective minimises the shifted variables x' = x - lb; the original
      // objective adds the constant sum of the lower bounds back in.
      val offset = (0 until n).foldLeft(R.zero)((acc, k) => acc + lb(k))
      (value + offset, x)
  end solveBounded

  /** Minimum total presses for one machine's joltage counters via exact branch-and-bound.
    *
    * Branch on the first fractional variable of the rational LP optimum: one child caps it at its
    * floor, the other raises it to its ceiling. A branch is pruned when its lower bound meets the
    * incumbent integer solution.
    *
    * @param buttons
    *   counter-incidence masks (bit `i` = counter `i`)
    * @param joltages
    *   target value of each counter
    * @return
    *   the minimum total presses; input is guaranteed reachable
    */
  def minimalPresses(buttons: Vector[Int], joltages: Vector[Int]): Int =
    val m = joltages.length
    val n = buttons.length
    val a: Array[Array[R]] = Array.tabulate(m)(i =>
      Array.tabulate(n)(j => if ((buttons(j) >> i) & 1) == 1 then R.one else R.zero)
    )
    val b: Array[R] = joltages.toArray.map(v => R(v))

    val ub: Array[Int] = Array.tabulate(n): j =>
      var lo = Int.MaxValue
      var i = 0
      while i < m do
        if ((buttons(j) >> i) & 1) == 1 then lo = math.min(lo, joltages(i))
        i += 1
      lo

    // Lower bound `value >= best` (integer best) exact for rationals.
    def atLeast(value: R, bound: Int): Boolean = value.n >= BigInt(bound) * value.d

    var best = Int.MaxValue

    def node(lb: Array[R], hi: Array[Int]): Unit = solveBounded(a, b, lb, hi) match
      case None             => ()
      case Some((value, x)) =>
        if atLeast(value, best) then ()
        else
          val fracIdx = x.indices.find(k => !x(k).isInteger)
          fracIdx match
            case None    => best = math.min(best, value.toInt)
            case Some(k) =>
              val z = x(k)
              val flo = z.floor.toInt
              val hi1 = hi.clone()
              hi1(k) = flo
              node(lb, hi1)
              val lb2 = lb.clone()
              lb2(k) = R(flo + 1)
              node(lb2, hi)
          end match
    end node

    node(Array.fill(n)(R.zero), ub)
    best
  end minimalPresses

end CounterSolver
