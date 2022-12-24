package day14

import kotlin.math.sign
import readInput

enum class Direction(val vec: Position) {
    DOWN(Position(1, 0)),
    DOWNLEFT(Position(1, -1)),
    DOWNRIGHT(Position(1, 1)),
    STAY(Position(0, 0))
}

data class Position(val row: Int, val column: Int) {
    operator fun plus(other: Position): Position {
        return Position(row + other.row, column + other.column)
    }
    operator fun minus(other: Position): Position {
        return Position(row - other.row, column - other.column)
    }
    fun sign(): Position {
        return Position(row.sign, column.sign)
    }
}

fun next(sandUnit: Position, walls: Set<Position>, fallenSand: Set<Position>): Position? {
    for (dir in Direction.values()) {
        val possibleNext = sandUnit + dir.vec
        if (!walls.contains(possibleNext) && !fallenSand.contains(possibleNext)) {
            return possibleNext
        }
    }
    return null
}

fun parsePosition(input: String): Position {
    val (column, row) = input.split(",").map{ it.trim().toInt() }
    return Position(row, column)
}
fun parseLine(input: String): List<Position> {
    return input.split("->").map(::parsePosition)
}

fun getPostionsInBetween(start: Position, end: Position): List<Position> {
    val ans = mutableListOf<Position>()
    val vec = (end - start).sign()
    var current = start + vec
    while (current != end) {
        ans.add(current)
        current += vec
    }
    return ans
}
fun getLinePositions(line: List<Position>): List<Position> {
    val ans = mutableListOf<Position>()
    for (i in 0 .. line.size - 2) {
        ans.add(line[i])
        ans.addAll(getPostionsInBetween(line[i], line[i + 1]))
    }
    ans.add(line.last())
    return ans
}

fun createWalls(lines: List<List<Position>>): Set<Position> {
    return lines.flatMap { getLinePositions(it) }.toSet()
}

fun simulate(walls: Set<Position>): Int {
    val startPosition = Position(0, 500)
    val fallenSand = mutableSetOf<Position>()
    val bottom = walls.maxOf { it.row }
    while (true) {
        var sandUnit = startPosition
        var nextPos = next(sandUnit, walls, fallenSand)
        while (nextPos != null && sandUnit != nextPos && nextPos.row <= bottom) {
            sandUnit = nextPos
            nextPos = next(sandUnit, walls, fallenSand)
        }
        if (nextPos == null || sandUnit.row == bottom) {
            return fallenSand.size
        }
        fallenSand.add(sandUnit)
    }
}

fun part1(input: List<String>): Int {
    val walls = createWalls(input.map(::parseLine))
    return simulate(walls)
}

fun part2(input: List<String>): Int {
    val walls = createWalls(input.map(::parseLine))
    val bottomRow = walls.maxOf { it.row + 2 }
    val bottomLine = listOf(Position(bottomRow, -10000), Position(bottomRow, 10000))
    return simulate(walls + createWalls(listOf(bottomLine)))

}

fun main() {
    println("Day 14")
    val testInput = readInput("Day14-test")
    val input = readInput("Day14")
    println("part 1 test: ${part1(testInput)}")
    println("part 1 real: ${part1(input)}")
    println("part 2 test: ${part2(testInput)}")
    println("part 2 real: ${part2(input)}")
    println()
}