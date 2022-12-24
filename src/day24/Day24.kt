package day24

import readInput

enum class Direction(val symbol: Char, val vec: Position) {
    UP('^', Position(-1, 0)),
    RIGHT('>', Position(0, 1)),
    DOWN('v', Position(1, 0)),
    LEFT('<', Position(0, -1));

    companion object {
        private val symbolMap = Direction.values().associateBy { it.symbol }
        fun fromSymbol(symbol: Char): Direction {
            return symbolMap[symbol]!!
        }
    }
}

data class Position(val row: Int, val column: Int) {
    operator fun plus(other: Position): Position {
        return Position(row + other.row, column + other.column)
    }

    operator fun minus(other: Position): Position {
        return Position(row - other.row, column - other.column)
    }
}

data class Blizzard(val position: Position, val direction: Direction)

fun wrapSingleCoordinate(coordinate: Int, restriction: Int): Int {
    return (restriction + (coordinate - 1)) % restriction + 1
}

fun moveWrapped(blizzard: Blizzard, valleyHeight: Int, valleyWidth: Int): Blizzard {
    val dummyNextPosition = blizzard.position + blizzard.direction.vec
    val row = wrapSingleCoordinate(dummyNextPosition.row, valleyHeight)
    val column = wrapSingleCoordinate(dummyNextPosition.column, valleyWidth)
    return Blizzard(Position(row, column), blizzard.direction)
}

@Suppress("Unused")
fun printState(
    valleyHeight: Int,
    valleyWidth: Int,
    blizzards: Set<Blizzard>,
    walls: Set<Position>,
    ourPositions: Set<Position>
) {
    val blizzardCounts = blizzards.groupBy { it.position }
        .map {
            it.key to if (it.value.size == 1) {
                it.value[0].direction.symbol
            } else {
                it.value.size.toString()[0]
            }
        }.toMap()
    for (row in 0..valleyHeight + 1) {
        for (column in 0..valleyWidth + 1) {
            when (val pos = Position(row, column)) {
                in walls -> print("#")
                in blizzardCounts -> print(blizzardCounts[pos])
                in ourPositions -> print("E")
                else -> print('.')
            }
        }
        println()
    }
}

fun findPath(
    start: Position,
    finish: Position,
    walls: Set<Position>,
    valleyHeight: Int,
    valleyWidth: Int,
    startBlizzards: Set<Blizzard>
): Pair<Int, Set<Blizzard>> {
    var possiblePositions = setOf(start)
    var requiredSteps = 0
    var blizzards = startBlizzards
    while (!possiblePositions.contains(finish)) {
        ++requiredSteps
        val nextBlizzards = mutableSetOf<Blizzard>()
        for (blizzard in blizzards) {
            nextBlizzards.add(moveWrapped(blizzard, valleyHeight, valleyWidth))
        }
        val nextBlizzardPositions = nextBlizzards.map { it.position }.toSet()
        val nextPossiblePositions = mutableSetOf<Position>()
        for (position in possiblePositions) {

            val neigbours = Direction.values().map { it.vec + position }
            val options = neigbours + listOf(position)
            val filteredOptions = options.filter { !walls.contains(it) && !nextBlizzardPositions.contains(it) }
            /*
            if (requiredSteps < 3) {
                println("Considering $position")
                println("neigbours $neigbours")
                println("options $options")
                println("filtered $filteredOptions")
                println()
            }
             */
            nextPossiblePositions.addAll(filteredOptions)
        }
        possiblePositions = nextPossiblePositions
        blizzards = nextBlizzards
        // println("Step $requiredSteps, total positions: ${nextPossiblePositions.size}")
        // println("$possiblePositions")
        //printState(valleyHeigh, valleyWidth, blizzards, walls, possiblePositions)
    }
    return requiredSteps to blizzards
}

fun solve(input: List<String>): Pair<Int, Int> {
    val valleyHeigh = input.size - 2
    val valleyWidth = input[0].length - 2
    val walls = mutableSetOf<Position>()
    val startPosition = Position(0, input.first().indexOf('.'))
    val finishPosition = Position(input.size - 1, input.last().indexOf('.'))
    walls.add(startPosition + Direction.UP.vec)
    walls.add(finishPosition + Direction.DOWN.vec)
    val blizzards = mutableSetOf<Blizzard>()
    for (row in input.indices) {
        for (column in input[row].indices) {
            val symbol = input[row][column]
            if (symbol in Direction.values().map { it.symbol }) {
                blizzards.add(Blizzard(Position(row, column), Direction.fromSymbol(symbol)))
            }
            if (symbol == '#') {
                walls.add(Position(row, column))
            }
        }
    }
    val (stepsToFinish, blizzards1) = findPath(startPosition, finishPosition, walls, valleyHeigh, valleyWidth, blizzards)
    val (stepsBack, blizzards2) = findPath(finishPosition, startPosition, walls, valleyHeigh, valleyWidth, blizzards1)
    val (stepsToFinishAgain, _) = findPath(startPosition, finishPosition, walls, valleyHeigh, valleyWidth, blizzards2)
    return stepsToFinish to stepsToFinish + stepsBack + stepsToFinishAgain
}

fun main() {
    println("Day 24")
    val testInput = readInput("Day24-test")
    val input = readInput("Day24")
    val (testAns1, testAns2) = solve(testInput)
    println("test part1: $testAns1")
    println("test part2: $testAns2")
    val (ans1, ans2) = solve(input)
    println("real part1: $ans1")
    println("real part2: $ans2")
}