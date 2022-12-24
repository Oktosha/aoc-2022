package day16

import readInput
import kotlin.math.max
import kotlin.math.pow

class StepData(val data: ShortArray, val totalValves: Int, val usefulValves: Int, val players: Int) {
    companion object {
        fun numberOfValveConfigurations(usefulValves: Int): Int {
            return (2).toDouble().pow(usefulValves).toInt()
        }

        fun create(totalValves: Int, usefulValves: Int, players: Int): StepData {

            val numberOfPositions = totalValves.toDouble().pow(players).toInt()
            return StepData(
                ShortArray(numberOfPositions * numberOfValveConfigurations(usefulValves)) { _ -> -1 },
                totalValves,
                usefulValves,
                players
            )
        }
    }

    private fun numberOfValveConfigurations(): Int {
        return Companion.numberOfValveConfigurations(usefulValves)
    }

    fun unpack(state: Int): State {
        val openValvesMask = state % numberOfValveConfigurations()
        var encodedPositions = state / numberOfValveConfigurations()
        val positions = mutableListOf<Int>()
        for (i in 0 until players) {
            positions.add(encodedPositions % totalValves)
            encodedPositions /= totalValves
        }
        val openValves = mutableSetOf<Int>()
        for (i in 0 until usefulValves) {
            if (openValvesMask and (1 shl i) != 0) {
                openValves.add(i)
            }
        }
        return State(positions, openValves)
    }

    fun pack(state: State): Int {
        var openValvesMask = 0
        for (i in 0 until usefulValves) {
            if (i in state.openValves) {
                openValvesMask = openValvesMask or (1 shl i)
            }
        }
        var encodedPositions = 0
        for (player in state.positions.reversed()) {
            encodedPositions += player
            encodedPositions *= totalValves
        }
        encodedPositions /= totalValves
        return encodedPositions * numberOfValveConfigurations() + openValvesMask
    }

    operator fun set(state: State, value: Short) {
        data[pack(state)] = value
    }

    operator fun get(state: State): Short {
        return data[pack(state)]
    }
}

data class EncodedValve(val rate: Int, val tunnels: List<Int>)

sealed class Transition {
    class Move(val direction: Int) : Transition() {
        override fun toString(): String {
            return "move to $direction"
        }
    }
    class OpenValve(val valve: Int) : Transition() {
        override fun toString(): String {
            return "open valve $valve"
        }
    }
}

class ValveInfo(
    private val valves: List<EncodedValve>,
    val usefulValvesCount: Int,
    val startValve: Int,
    val numbersToNames: Map<Int, String>
) {
    companion object {
        fun create(input: List<String>): ValveInfo {
            val parsedValves = parseValves(input)
            val usefulValves = parsedValves.filter { e -> e.value.rate > 0 }.toList()
            val uselessValves = parsedValves.filter { e -> e.value.rate == 0 }.toList()
            val orderedValves = usefulValves + uselessValves
            val namesToNumbers = mutableMapOf<String, Int>()
            orderedValves.forEachIndexed { index, valve -> namesToNumbers[valve.first] = index }
            val valves = orderedValves
                .map { valve ->
                    EncodedValve(valve.second.rate,
                        valve.second.tunnels.map { v -> namesToNumbers[v]!! })
                }
            return ValveInfo(valves,
                usefulValves.size,
                namesToNumbers["AA"]!!,
                namesToNumbers.entries.associateBy({ it.value }) { it.key })
        }
    }

    fun production(state: State): Short {
        return state.openValves.sumOf { v -> valves[v].rate }.toShort()
    }

    private fun isUseful(valve: Int): Boolean {
        return valve < usefulValvesCount
    }

    fun totalValves(): Int {
        return valves.size
    }

    private fun transitions(position: Int, openValves: Set<Int>): List<Transition> {
        val options = valves[position].tunnels.map { v -> Transition.Move(v) }
        if (isUseful(position) && !openValves.contains(position)) {
            return options + listOf(Transition.OpenValve(position))
        }
        return options
    }

    fun adjacentStates(state: State): List<State> {
        val stepComponents = List(state.positions.size) { i -> transitions(state.positions[i], state.openValves) }
        // println("Step components: $stepComponents")
        val firstPlayerOptions = stepComponents[0]
        var stepDescriptions = firstPlayerOptions.map{ listOf(it)}
        for (playerOptions in stepComponents.drop(1)) {
            val newStepDescriptions = mutableListOf<List<Transition>>()
            for (option in playerOptions) {
                for (stepDescription in stepDescriptions) {
                    newStepDescriptions.add(stepDescription + option)
                }
            }
            stepDescriptions = newStepDescriptions
        }
        // println("Step descriptions: $stepDescriptions")
        val answer = mutableListOf<State>()
        for (description in stepDescriptions) {
            val newPositions = description.mapIndexed { i, d -> when(d) {
                is Transition.OpenValve -> state.positions[i]
                is Transition.Move -> d.direction
            } }
            val newOpenValves = state.openValves + description
                .filterIsInstance<Transition.OpenValve>().map { d -> d.valve }.toSet()
            answer.add(State(newPositions, newOpenValves))
        }
        return answer
    }

    @Suppress("Unused")
    fun stateToString(state: State): String {
        return state.positions.map { it to numbersToNames[it] }.toString() +
                "-" + state.openValves.map { it to numbersToNames[it] }
    }
}

data class State(val positions: List<Int>, val openValves: Set<Int>)

fun nextStep(stepData: StepData, valveInfo: ValveInfo): StepData {
    val next = StepData.create(stepData.totalValves, stepData.usefulValves, stepData.players)
    for (i in stepData.data.indices) {
        if (stepData.data[i] > (-1).toShort()) {
            val state = stepData.unpack(i)
            // println("Current state: ${valveInfo.stateToString(state)}")
            val adjacentStates = valveInfo.adjacentStates(state)
            // println("Adjacent states: ${adjacentStates.map{valveInfo.stateToString(it)}}")
            for (nextState in adjacentStates) {
                next[nextState] = max(
                    next[nextState].toInt(),
                    stepData.data[i] + valveInfo.production(state)
                ).toShort()
            }
        }
    }
    return next
}

fun solve(input: List<String>, players: Int, stepCount: Int): Short {
    val valveInfo = ValveInfo.create(input)
    val stepZero = StepData.create(valveInfo.totalValves(), valveInfo.usefulValvesCount, players)
    val startingPositions = List(players) { _ -> valveInfo.startValve }
    stepZero[State(startingPositions, setOf())] = 0
    var currentStep = stepZero
    repeat(stepCount) {
        currentStep = nextStep(currentStep, valveInfo)
        println("$it: ${currentStep.data.max()}")
    }
    println()
    return currentStep.data.max()
}

fun testEncoding(totalValves: Int, usefulValves: Int, players: Int) {
    val stepData = StepData.create(totalValves, usefulValves, players)
    for (i in stepData.data.indices) {
        val state = stepData.unpack(i)
        val packedBack = stepData.pack(state)
        if (packedBack != i) {
            throw Exception("Encoding problems $i -> $state -> $packedBack")
        }
    }
}

fun main() {
    println("Day 16 rewritten")
    val testInput = readInput("Day16-test")
    val input = readInput("Day16")
    testEncoding(10, 5, 1)
    testEncoding(10, 5, 2)
    // testEncoding(57, 14, 1)
    // testEncoding(57, 14, 2)
    println("part 1 test: ${solve(testInput, 1, 30)}")
    println("part 1 real: ${solve(input, 1, 30)}")
    println("part 2 test: ${solve(testInput, 2, 26)}")
    println("part 2 real: ${solve(input, 2, 26)}")
}