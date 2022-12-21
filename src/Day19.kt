import kotlin.math.max
import kotlin.math.min

enum class ResourceType {
    ORE, CLAY, OBSIDIAN, GEODE
}

fun main() {
    data class ResourceSet(val ore: Int, val clay: Int, val obsidian: Int) {
        operator fun plus(other: ResourceSet): ResourceSet {
            return ResourceSet(ore + other.ore, clay + other.clay, obsidian + other.obsidian)
        }

        fun covers(other: ResourceSet, time: Int = 1): Boolean {
            return ore * time >= other.ore && clay * time >= other.clay && obsidian * time >= other.obsidian
        }

        operator fun get(type: ResourceType): Int {
            return when (type) {
                ResourceType.ORE -> ore
                ResourceType.CLAY -> clay
                ResourceType.OBSIDIAN -> obsidian
                ResourceType.GEODE -> throw IndexOutOfBoundsException()
            }
        }

        operator fun minus(other: ResourceSet): ResourceSet {
            return ResourceSet(ore - other.ore, clay - other.clay, obsidian - other.obsidian)
        }
    }

    fun robotProduction(type: ResourceType): ResourceSet {
        return when (type) {
            ResourceType.ORE -> ResourceSet(1, 0, 0)
            ResourceType.CLAY -> ResourceSet(0, 1, 0)
            ResourceType.OBSIDIAN -> ResourceSet(0, 0, 1)
            ResourceType.GEODE -> throw IndexOutOfBoundsException()
        }
    }

    data class RobotCosts(
        val ore: ResourceSet,
        val clay: ResourceSet,
        val obsidian: ResourceSet,
        val geode: ResourceSet
    ) {
        operator fun get(type: ResourceType): ResourceSet {
            return when (type) {
                ResourceType.ORE -> ore
                ResourceType.CLAY -> clay
                ResourceType.OBSIDIAN -> obsidian
                ResourceType.GEODE -> geode
            }
        }

        fun asList(): List<ResourceSet> {
            return listOf(ore, clay, obsidian, geode)
        }
    }

    operator fun <T> List<T>.component6(): T {
        return get(5)
    }

    operator fun <T> List<T>.component7(): T {
        return get(6)
    }

    data class State(val production: ResourceSet, var stock: ResourceSet, val geodes: Int)

    data class Blueprint(val id: Int, val robotCosts: RobotCosts) {
        fun bestGeodeProduction(time: Int): Int {
            var states = mutableSetOf(State(ResourceSet(1, 0, 0), ResourceSet(0, 0, 0), 0))
            var ans = 0
            for (minute in 1..time) {
                val timeLeft = time - minute
                println("time left: $timeLeft")
                val nextStates = mutableSetOf<State>()
                for (state in states) {
                    if (waitCanBeUseful(state, timeLeft)) {
                        val newState = State(state.production, state.stock + state.production, state.geodes)
                        nextStates.add(newState)
                    }
                    val usefulRobots = usefulRobotsPossibleNow(state)
                    for (robot in usefulRobots) {
                        val newState = if (robot == ResourceType.GEODE) {
                            State(
                                state.production,
                                state.stock - robotCosts[robot] + state.production,
                                state.geodes + timeLeft
                            )
                        } else {
                            State(
                                state.production + robotProduction(robot),
                                state.stock - robotCosts[robot] + state.production,
                                state.geodes,
                            )
                        }
                        nextStates.add(newState)
                    }
                }
                states = nextStates
                println("step $minute, number of states: ${states.size}")
                val bestResultOfTheStep = states.maxOf { x -> x.geodes }
                println("Geodes: $bestResultOfTheStep in ${states.count { x -> x.geodes == bestResultOfTheStep }} variants")
                ans = max(ans, bestResultOfTheStep)
                println()
                println()
            }
            return ans
        }

        fun robotsPossibleNow(state: State): List<ResourceType> {
            val answer = mutableListOf<ResourceType>()
            for (robotType in ResourceType.values()) {
                if (state.stock.covers(robotCosts[robotType])) {
                    answer.add(robotType)
                }
            }
            return answer
        }

        fun robotsPossibleWithWait(state: State, waitTime: Int = 40): List<ResourceType> {
            val answer = mutableListOf<ResourceType>()
            for (robotType in ResourceType.values()) {
                if (state.production.covers(robotCosts[robotType], waitTime)) {
                    answer.add(robotType)
                }
            }
            return answer
        }

        fun usefulRobotsPossibleNow(state: State): Set<ResourceType> {
            return usefulRobots(state).toSet().intersect(robotsPossibleNow(state).toSet())
        }

        fun waitCanBeUseful(state: State, waitTime: Int): Boolean {
            val usefulRobotsPossibleWithWait =
                usefulRobots(state).toSet().intersect(robotsPossibleWithWait(state, waitTime + 2).toSet())
            return usefulRobotsPossibleWithWait.size > usefulRobotsPossibleNow(state).size
        }

        fun usefulRobots(state: State): List<ResourceType> {
            val answer = mutableListOf<ResourceType>()
            for (robotType in ResourceType.values()) {
                if (robotType == ResourceType.GEODE) {
                    answer.add(ResourceType.GEODE)
                } else if (state.production[robotType] < robotCosts.asList().maxOf { r -> r[robotType] }) {
                    answer.add(robotType)
                }
            }

            return answer
        }

        fun qualityLevel(time: Int): Int {
            return id * bestGeodeProduction(time)
        }
    }

    fun parseBlueprint(input: String): Blueprint {
        val (id, oreOre, clayOre, obsidianOre, obsidianClay, geodeOre, geodeObsidian) = input.replace(':', ' ')
            .split(" ")
            .mapNotNull { x -> x.toIntOrNull() }
        val oreRobotCost = ResourceSet(oreOre, 0, 0)
        val clayRobotCost = ResourceSet(clayOre, 0, 0)
        val obsidianRobotCost = ResourceSet(obsidianOre, obsidianClay, 0)
        val geodeRobotCost = ResourceSet(geodeOre, 0, geodeObsidian)
        return Blueprint(id, RobotCosts(oreRobotCost, clayRobotCost, obsidianRobotCost, geodeRobotCost))
    }

    fun part1(input: List<Blueprint>) {
        var totalAns = 0
        for (blueprint in input) {
            println("Blueprint ${blueprint}:")
            val ans = blueprint.qualityLevel(24)
            println("Ans ${blueprint.id}: $ans")
            println()
            totalAns += ans
        }
        println("ANSWER: $totalAns")
    }

    @Suppress("unused")
    fun part2(input: List<Blueprint>): Int {
        var totalAns = 1
        val len = min(3, input.size)
        for (blueprint in input.subList(0, len)) {
            val ans = blueprint.bestGeodeProduction(32)
            println("Ans for blueprint ${blueprint.id}: $ans")
            totalAns *= ans
        }
        return totalAns
    }

    val input = readInput("Day19-test").map(::parseBlueprint)
    part1(input)
}