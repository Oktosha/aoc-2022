package day23
import readInput

fun <E> Collection<E>.containsAny(elements: Collection<E>): Boolean {
    return elements.any { x -> this.contains(x) }
}

enum class Direction(val vec: Position) {
    N(Position(-1, 0)),
    NE(Position(-1, 1)),
    E(Position(0, 1)),
    SE(Position(1, 1)),
    S(Position(1, 0)),
    SW(Position(1, -1)),
    W(Position(0, -1)),
    NW(Position(-1, -1));

    companion object {
        // steps are counted from 1
        fun considerationSequence(step: Int): List<Direction> {
            return when (step % 4) {
                1 -> listOf(N, S, W, E)
                2 -> listOf(S, W, E, N)
                3 -> listOf(W, E, N, S)
                0 -> listOf(E, N, S, W)
                else -> {
                    throw Exception("Kek, step % 4 is ${step % 4}")
                }
            }
        }

        fun directionsToCheckWhenLookingAt(direction: Direction): List<Direction> {
            return when (direction) {
                N -> listOf(NW, N, NE)
                S -> listOf(SW, S, SE)
                W -> listOf(NW, W, SW)
                E -> listOf(NE, E, SE)
                else -> throw Exception("Requesting direction check for $direction")
            }
        }
    }
}

data class Position(val row: Int, val column: Int) {
    operator fun plus(other: Position): Position {
        return Position(row + other.row, column + other.column)
    }

    fun neigbours(): List<Position> {
        return Direction.values().map { x -> x.vec + this }
    }

    fun neigbours(direction: Direction): List<Position> {
        return Direction.directionsToCheckWhenLookingAt(direction).map { x -> x.vec + this }
    }
}

typealias State = Set<Position>

fun State.part1Answer(): Int {
    val startRow = this.minOf { p -> p.row }
    val endRow = this.maxOf { p -> p.row }
    val startColumn = this.minOf { p -> p.column }
    val endColumn = this.maxOf { p -> p.column }
    return (endRow - startRow + 1) * (endColumn - startColumn + 1) - this.size
}

fun State.proposition(elf: Position, considerationSequence: List<Direction>): Position? {
    if (!this.containsAny(elf.neigbours())) {
        return null
    }
    for (direction in considerationSequence) {
        if (!this.containsAny(elf.neigbours(direction))) {
            return elf + direction.vec
        }
    }
    return null
}

fun State.nextWith(considerationSequence: List<Direction>): State {
    val movingElves = this
        .asSequence()
        .map { x -> x to proposition(x, considerationSequence) }
        .filter { x -> x.second != null }
        .groupBy { x -> x.second }
        .filter { x -> x.value.size == 1 }
        .map { x -> x.value[0]}
        .toList().toMap()
    val nextState = mutableSetOf<Position>()
    for (elf in this) {
        if (elf in movingElves) {
            nextState.add(movingElves[elf]!!)
        } else {
            nextState.add(elf)
        }
    }
    return nextState
}

fun readState(input: List<String>): State {
    val state = mutableSetOf<Position>()
    for (row in input.indices) {
        for (column in input[row].indices) {
            if (input[row][column] == '#') {
                state.add(Position(row, column))
            }
        }
    }
    return state
}

fun simulate(startState: State, startStep: Int, numberOfSteps: Int): State {
    var state = startState
    for (step in startStep until startStep + numberOfSteps) {
        state = state.nextWith(Direction.considerationSequence(step))
    }
    return state
}

fun prettyPrintState(state: State) {
    for (row in 0..5) {
        for (column in 0..4) {
            if (state.contains(Position(row, column))) {
                print("#")
            } else {
                print(".")
            }
        }
        println()
    }
}

fun testSimulation(testData: List<Pair<Int, State>>) {
    for (i in 0 until testData.size - 1) {
        val (startStep, startState) = testData[i]
        val (endStep, endState) = testData[i + 1]
        val simulatedState = simulate(startState, startStep + 1, endStep - startStep)
        if (endState != simulatedState) {
            println("Simulating $startStep to $endStep starting with")
            prettyPrintState(startState)
            println("Expected state")
            prettyPrintState(endState)
            println("Simulated State")
            prettyPrintState(simulatedState)
            throw Exception("Simulation is wrong")
        }
    }
}

fun parseTestData(input: List<String>): List<Pair<Int, State>> {
    var stateMap = mutableListOf<String>()
    val testData = mutableListOf<Pair<Int, State>>()
    for (line in input) {
        if (line.toIntOrNull() == null) {
            stateMap.add(line)
        } else {
            testData.add(line.toInt() to readState(stateMap))
            stateMap = mutableListOf()
        }
    }
    return testData
}

fun main() {
    val smallTestData = parseTestData(readInput("Day23-test"))
    testSimulation(smallTestData)
    val bigTestData = parseTestData(readInput("Day23-test2"))
    testSimulation(bigTestData)
    println("Day 23")
    val input = readInput("Day23")
    val ans = simulate(readState(input), 1, 10).part1Answer()
    println("Answer part1: $ans")
}