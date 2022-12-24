package day17

import containsAny
import readInput

enum class Direction(val symbol: Char, val vec: Point) {
    UP('^', Point(0, 1)),
    RIGHT('>', Point(1, 0)),
    DOWN('v', Point(0, -1)),
    LEFT('<', Point(-1, 0));

    companion object {
        private val symbolMap = Direction.values().associateBy { it.symbol }
        fun fromSymbol(symbol: Char): Direction {
            return symbolMap[symbol]!!
        }
    }
}

/*
y
5|..#.#..|
4|#####..|
3|..###..|
2|...#...|
1|..####.|
0+-------+
 012345678x
 */
data class Point(val x: Int, val y: Int) {
    operator fun plus(other: Point): Point {
        return Point(x + other.x, y + other.y)
    }
}

data class Figure(val points: List<Point>) {
    fun moved(dir: Direction): Figure {
        return Figure(points.map { it + dir.vec })
    }

    fun moved(vec: Point): Figure {
        return Figure(points.map { it + vec })
    }

    fun top(): Int {
        return points.maxOf { it.y }
    }

    fun left(): Int {
        return points.minOf { it.x }
    }

    fun right(): Int {
        return points.maxOf { it.x }
    }
}

fun spawn(figure: Figure, towerHeight: Int): Figure {
    return figure.moved(Point(3, towerHeight + 4))
}

fun printState(tower: Tower, fallingFigure: Figure?) {
    val pictureHeight = fallingFigure?.top() ?: tower.height
    for (y in pictureHeight downTo 0) {
        for (x in 0..8) {
            val p = Point(x, y)
            if (fallingFigure?.points?.contains(p) == true) {
                print("@")
            } else if (x == 0 && y == 0) {
                print("+")
            } else if (x == 8 && y == 0) {
                print("+")
            } else if ((x == 0) || (x == 8)) {
                print("|")
            } else if (y == 0) {
                print("-")
            } else if (tower.points.contains(p)) {
                print("#")
            } else {
                print('.')
            }
        }
        println()
    }
}

data class Tower(val points: MutableSet<Point>, var height: Int)

fun simulate(
    jetPattern: List<Direction>,
    figureSequence: List<Figure>, steps: Int, verbose: Boolean = false
): List<Int> {
    val tower = Tower(List(9) { Point(it, 0) }.toMutableSet(), 0)
    var jetCount = 0
    val heightIncrease = mutableListOf<Int>()
    var height = 0
    repeat(steps) {
        val figureIndex = it % figureSequence.size
        var figure = spawn(figureSequence[figureIndex], tower.height)
        while (true) {
            val jetDirection = jetPattern[jetCount % jetPattern.size]
            val jetMoved = figure.moved(jetDirection)
            if (jetMoved.right() <= 7 &&
                jetMoved.left() >= 1 &&
                !tower.points.containsAny(jetMoved.points)
            ) {
                figure = jetMoved
            }
            if (verbose) {
                println("jet ${jetDirection.symbol} $it")
                printState(tower, figure)
                println()
            }
            ++jetCount
            val movedDown = figure.moved(Direction.DOWN)
            if (tower.points.containsAny(movedDown.points)) {
                tower.points.addAll(figure.points)
                tower.height = figure.points.maxOf { f -> f.y }.coerceAtLeast(tower.height)
                break
            }
            figure = movedDown
            if (verbose) {
                println("down $it")
                printState(tower, figure)
                println()
            }
        }
        if (verbose) {
            println("finish $it")
            printState(tower, null)
            println()
        }
        val currentHeightIncrease = tower.height - height
        heightIncrease.add(currentHeightIncrease)
        height = tower.height
    }
    return heightIncrease
}

fun countWithCycles(totalSteps: Long, cycleInit: List<Long>, cycle: List<Long>): Long {
    val amountOfCycles = (totalSteps - cycleInit.size) / cycle.size
    val remainder = ((totalSteps - cycleInit.size) % cycle.size).toInt()
    return cycleInit.sum() + amountOfCycles * cycle.sum() + cycle.take(remainder).sum()
}

data class Cycle(val offset: Int, val length: Int)

fun findCycle(data: List<Int>, maxOffset: Int, maxLength: Int): Cycle? {
    for (length in 5..maxLength step 5) {
        for (offset in 0..maxOffset) {
            var isGoodCycle = true
            for (i in offset until data.size - length) {
                if (data[i] != data[i + length]) {
                    isGoodCycle = false
                    break
                }
            }
            if (isGoodCycle) {
                return Cycle(offset, length)
            }
        }
    }
    return null
}

fun predictHeightFromSimulationResult(heightIncrease: List<Int>, numberOfSteps: Long): Long {
    val cycle = findCycle(heightIncrease, 5000, 5000)
        ?: throw Exception("Couldn't find cycle")
    val cycleInit = heightIncrease.take(cycle.offset)
    val cycleElements = heightIncrease.drop(cycle.offset).take(cycle.length)
    return countWithCycles(numberOfSteps,
        cycleInit.map { x -> x.toLong() },
        cycleElements.map { x -> x.toLong() })
}

fun main() {
    println("Day 17")

    val testInput = readInput("Day17-test")[0].map { Direction.fromSymbol(it) }
    val input = readInput("Day17")[0].map { Direction.fromSymbol(it) }

    val horizontalLine = Figure(listOf(Point(0, 0), Point(1, 0), Point(2, 0), Point(3, 0)))
    val plus = Figure(listOf(Point(1, 0), Point(0, 1), Point(1, 1), Point(2, 1), Point(1, 2)))
    val corner = Figure(listOf(Point(0, 0), Point(1, 0), Point(2, 0), Point(2, 1), Point(2, 2)))
    val verticalLine = Figure(listOf(Point(0, 0), Point(0, 1), Point(0, 2), Point(0, 3)))
    val square = Figure(listOf(Point(0, 0), Point(0, 1), Point(1, 0), Point(1, 1)))
    val figureSequence = listOf(horizontalLine, plus, corner, verticalLine, square)

    val totalSteps = 1000000000000

    val testHeightIncrease = simulate(testInput, figureSequence, 10000)
    val realHeightIncrease = simulate(input, figureSequence, 50000)

    println("part 1 test ${testHeightIncrease.take(2022).sum()}")
    println("part 1 real: ${realHeightIncrease.take(2022).sum()}")
    println("part 2 test: ${predictHeightFromSimulationResult(testHeightIncrease, totalSteps)}")
    println("part 2 real: ${predictHeightFromSimulationResult(realHeightIncrease, totalSteps)}")

}