package day23
import containsAny
import readInput

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
    operator fun minus(other: Position): Position {
        return Position(row - other.row, column - other.column)
    }
    fun area(): Int {
        return row * column
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
    return (bottomRight() - topLeft() + Position(1, 1)).area() - this.size
}

fun State.topLeft(): Position {
    return Position(this.minOf { x -> x.row }, this.minOf { x -> x.column })
}

fun State.bottomRight(): Position {
    return Position(this.maxOf { x -> x.row }, this.maxOf { x -> x.column })
}

fun State.normalized(): State {
    return this.map { x -> x - this.topLeft() }.toSet()
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

fun State.nextWith(considerationSequence: List<Direction>): State? {
    val movingElves = this
        .asSequence()
        .map { x -> x to proposition(x, considerationSequence) }
        .filter { x -> x.second != null }
        .groupBy { x -> x.second }
        .filter { x -> x.value.size == 1 }
        .map { x -> x.value[0]}
        .toList().toMap()
    // println("Number of moving elves: ${movingElves.size}")
    if (movingElves.isEmpty()) {
        return null
    }
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
        // print("step $step: ")
        state = state.nextWith(Direction.considerationSequence(step)) ?: state
    }
    return state
}

fun simulateUntilEnd(startState: State): Int {
    var current = startState
    var stepOfNextState = 1
    var next = current.nextWith(Direction.considerationSequence(stepOfNextState))
    while (next != null) {
        current = next
        stepOfNextState += 1
        next = current.nextWith(Direction.considerationSequence(stepOfNextState))
    }
    return stepOfNextState
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

fun testSimulationToEnd(startState: State, steps: Int) {
    val simulatedSteps = simulateUntilEnd(startState)
    if (simulatedSteps != steps) {
        throw Exception("Simulation took $simulatedSteps instead of $steps")
    }
}

fun countStepsBetween(startState: State, endState: State, threshold: Int): Int {
    var state = startState
    var step = 1
    while (step < threshold && state.normalized() != endState.normalized()) {
        state = state.nextWith(Direction.considerationSequence(step))!!
        step += 1
    }
    return step - 1
}

fun testCountSteps(startState: State, endState: State, expectedSteps: Int, threshold: Int) {
    val steps = countStepsBetween(startState, endState, threshold)
    if (steps != expectedSteps) {
        throw Exception("Steps is $steps instead of $expectedSteps")
    }
}

fun main() {
    val smallTestData = parseTestData(readInput("Day23-test"))
    val bigTestData = parseTestData(readInput("Day23-test2"))
    val extraTestState = readState(readInput("Day23-extratest"))
    // println("test small simulation")
    testSimulation(smallTestData)
    // println("test big simulation")
    testSimulation(bigTestData)
    // println("Simulationg small test data")
    simulate(smallTestData[0].second, 1, 10)
    // println("Simulating big test Data")
    simulate(bigTestData[0].second, 1, 25)
    testCountSteps(smallTestData[0].second, smallTestData[3].second, 3, 100)
    testCountSteps(bigTestData[0].second, extraTestState, 19, 100)
    testSimulationToEnd(smallTestData[0].second, 4)
    testSimulationToEnd(bigTestData[0].second, 20)
    println("Day 23")
    val input = readInput("Day23")
    val ans = simulate(readState(input), 1, 10).part1Answer()
    println("Answer part1: $ans")
    val ans2 = simulateUntilEnd(readState(input))
    println("Answer part2: $ans2")
}