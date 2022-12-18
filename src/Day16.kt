import java.lang.Integer.max

fun main() {
    class Valve(val rate: Int, val tunnels: List<String>)

    fun parseValve(input: String): Pair<String, Valve> {
        val regex =
            Regex("""Valve (\p{Upper}{2}) has flow rate=(\d+); tunnels? leads? to valves? (((\p{Upper}{2})(, )?)+)""")
        val match = regex.matchEntire(input)
        val name = match!!.groups[1]!!.value
        val rate = match.groups[2]!!.value.toInt()
        val tunnels = match.groups[3]!!.value.split(",").map { s -> s.trim() }
        return Pair(name, Valve(rate, tunnels))
    }

    fun parseValves(input: List<String>): Map<String, Valve> {
        return input.associate(::parseValve)
    }

    @Suppress("unused")
    fun printValves(valves: Map<String, Valve>) {
        for (valve in valves) {
            println("${valve.key} : ${valve.value.rate} ${valve.value.tunnels}")
        }
    }

    fun <K> updateData(dataset: MutableMap<K, Int>, key: K, value: Int) {
        dataset[key] = max(dataset[key] ?: -1, value)
    }

    fun <K> increaseData(dataset: MutableMap<K, Int>, key: K, value: Int) {
        dataset[key] = dataset[key]!! + value
    }

    fun getTotalRate(valves: Map<String, Valve>, openValves: Set<String>): Int {
        return openValves.sumOf { x -> valves[x]!!.rate }
    }

    fun part1(valves: Map<String, Valve>): Int {
        var currentStepData = mutableMapOf<Pair<Set<String>, String>, Int>()
        currentStepData[setOf<String>() to "AA"] = 0
        for (minute in 1..29) {
            val nextStepData = mutableMapOf<Pair<Set<String>, String>, Int>()
            for (option in currentStepData) {
                val currentValve = option.key.second
                val openValves = option.key.first
                val releasedPressure = option.value
                for (nextValve in valves[currentValve]!!.tunnels) {
                    updateData(nextStepData, openValves to nextValve, releasedPressure)
                }
                if (valves[currentValve]!!.rate > 0)
                    updateData(nextStepData, openValves.plusElement(currentValve) to currentValve, releasedPressure)
            }
            for (option in nextStepData) {
                increaseData(nextStepData, option.key, getTotalRate(valves, option.key.first))
            }
            currentStepData = nextStepData
        }
        return currentStepData.values.max()
    }

    fun part2(valves: Map<String, Valve>): Int {
        var currentStepData = mutableMapOf<Pair<Set<String>, Set<String>>, Int>()
        currentStepData[setOf<String>() to setOf("AA")] = 0
        for (minute in 1..25) {
            val nextStepData = mutableMapOf<Pair<Set<String>, Set<String>>, Int>()
            for (option in currentStepData) {
                val me = option.key.second.first()
                val elephant = option.key.second.last()
                val openValves = option.key.first
                val releasedPressure = option.value
                for (myNext in valves[me]!!.tunnels) {
                    for (elephantNext in valves[elephant]!!.tunnels) {
                        updateData(nextStepData, openValves to setOf(myNext,elephantNext), releasedPressure)
                    }
                    if (valves[elephant]!!.rate > 0) {
                        updateData(
                            nextStepData,
                            openValves.plusElement(elephant) to setOf(myNext, elephant),
                            releasedPressure
                        )
                    }
                }
                if (valves[me]!!.rate > 0) {
                    for (elephantNext in valves[elephant]!!.tunnels) {
                        updateData(nextStepData, openValves.plusElement(me) to setOf(me, elephantNext), releasedPressure)
                    }
                    if (valves[elephant]!!.rate > 0) {
                        updateData(
                            nextStepData,
                            openValves.plusElement(elephant).plusElement(me) to setOf(me, elephant),
                            releasedPressure
                        )
                    }
                }
            }
            for (option in nextStepData) {
                increaseData(nextStepData, option.key, getTotalRate(valves, option.key.first))
            }
            val bestOption = nextStepData.maxBy { e -> e.value }
            currentStepData = if (bestOption.key.first.size == valves.values.count { x -> x.rate > 0 }) {
                println("Discarding old options")
                mutableMapOf(bestOption.key to bestOption.value)
            } else {
                nextStepData
            }
            println("Step: $minute, len ${currentStepData.size}")
        }
        return currentStepData.values.max()
    }

    println("Day 16")
    val testInput = readInput("Day16-test")
    val testValves = parseValves(testInput)
    val valves = parseValves(readInput("Day16"))
    println("part 1 test: ${part1(testValves)}")
    println("part 1 real: ${part1(valves)}")
    val testPart2Result = part2(testValves)
    println("part 2 test: $testPart2Result")
    val realPart2Result = part2(valves)
    println("part 2 real: $realPart2Result")
}