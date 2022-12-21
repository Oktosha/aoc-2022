import kotlin.math.max
import kotlin.math.min

enum class ResourceType {
    ORE, CLAY, OBSIDIAN, GEODE;

    companion object {
        fun buildingResourses(): List<ResourceType> {
            return listOf(ORE, CLAY, OBSIDIAN)
        }
    }
}

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

data class State(val production: ResourceSet, var stock: ResourceSet, val geodes: Int) {
    fun asList(): List<Int> {
        return listOf(
            production.ore,
            production.clay,
            production.obsidian,
            stock.ore,
            stock.clay,
            stock.obsidian,
            geodes
        )
    }
}

fun stateFromList(input: List<Int>): State {
    return State(ResourceSet(input[0], input[1], input[2]), ResourceSet(input[3], input[4], input[5]), input[6])
}

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
            println("total number of states: ${nextStates.size}")
            val reachedMaximums = nextStates.reduce { s1, s2 ->
                stateFromList(s1.asList().zip(s2.asList()) { x1, x2 -> max(x1, x2) })
            }
            val maximalStates = nextStates.filter { x ->
                x.asList().zip(reachedMaximums.asList())
                { y, ymax -> (y >= ymax) }.any { b -> b }
            }
            println("number of maximal states: ${maximalStates.size}")
            if (nextStates.size < 7000) {
                clearUselessStates(nextStates)
            } else {
                nextStates.removeIf { x -> maximalStates.any { y -> y.isBetterThan(x, this) } }
            }
            states = nextStates
            println("step $minute, useful number of states: ${states.size}")
            println("Geodes: ${reachedMaximums.geodes}")
            ans = max(ans, reachedMaximums.geodes)
            println()
            println()
        }
        return ans
    }

    private fun clearUselessStates(states: MutableSet<State>): MutableSet<State> {
        val useless = states.filter { x ->
            states.any(fun(y: State): Boolean {
                if (y.isBetterThan(x, this)) {
                    // println("$y is better than $x")
                    return true
                }
                return false
            })
        }
        states.removeAll(useless.toSet())
        return states
    }

    private fun robotsPossibleNow(state: State): List<ResourceType> {
        val answer = mutableListOf<ResourceType>()
        for (robotType in ResourceType.values()) {
            if (state.stock.covers(robotCosts[robotType])) {
                answer.add(robotType)
            }
        }
        return answer
    }

    private fun robotsPossibleWithWait(state: State, waitTime: Int = 40): List<ResourceType> {
        val answer = mutableListOf<ResourceType>()
        for (robotType in ResourceType.values()) {
            if (state.production.covers(robotCosts[robotType], waitTime)) {
                answer.add(robotType)
            }
        }
        return answer
    }

    private fun usefulRobotsPossibleNow(state: State): Set<ResourceType> {
        return usefulRobots(state).toSet().intersect(robotsPossibleNow(state).toSet())
    }

    private fun waitCanBeUseful(state: State, waitTime: Int): Boolean {
        val usefulRobotsPossibleWithWait =
            usefulRobots(state).toSet().intersect(robotsPossibleWithWait(state, waitTime + 2).toSet())
        return usefulRobotsPossibleWithWait.size > usefulRobotsPossibleNow(state).size
    }

    private fun usefulRobots(state: State): List<ResourceType> {
        val answer = mutableListOf<ResourceType>()
        for (robotType in ResourceType.values()) {
            if (robotType == ResourceType.GEODE) {
                answer.add(ResourceType.GEODE)
            } else if (state.production[robotType] < maxNeededProduction(robotType)) {
                answer.add(robotType)
            }
        }

        return answer
    }

    fun maxNeededProduction(resourceType: ResourceType): Int {
        if (resourceType == ResourceType.GEODE) {
            throw Exception("Asking for max geode production, lol")
        }
        return robotCosts.asList().maxOf { r -> r[resourceType] }
    }

    fun qualityLevel(time: Int): Int {
        return id * bestGeodeProduction(time)
    }
}

fun State.isBetterThan(other: State, blueprint: Blueprint): Boolean {
    if (this == other) {
        return false
    }
    if (this.production.covers(blueprint.robotCosts.geode)
        && this.stock.covers(blueprint.robotCosts.geode)
        && geodes >= other.geodes
    ) {
        //println("Ultimate!")
        return true
    }
    for (resourseType in ResourceType.buildingResourses())
        if (!resourseIsGoodEnough(blueprint, resourseType)
            && (production[resourseType] < other.production[resourseType]
                    || stock[resourseType] < other.stock[resourseType])
        ) {
            return false
        }
    return geodes >= other.geodes
}

fun State.resourseIsGoodEnough(blueprint: Blueprint, resource: ResourceType): Boolean {
    return production[resource] >= blueprint.maxNeededProduction(resource)
            && stock[resource] >= blueprint.maxNeededProduction(resource)
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

fun part1(input: List<Blueprint>): Int {
    var totalAns = 0
    for (blueprint in input) {
        println("Blueprint ${blueprint}:")
        val ans = blueprint.qualityLevel(24)
        println("Ans ${blueprint.id}: $ans")
        println()
        totalAns += ans
    }
    println("ANSWER: $totalAns")
    return totalAns
}

fun part2(input: List<Blueprint>): Int {
    var totalAns = 1
    val len = min(3, input.size)
    for (blueprint in input.subList(0, len)) {
        val ans = blueprint.bestGeodeProduction(32)
        println("Ans for blueprint ${blueprint.id}: $ans")
        totalAns *= ans
    }
    println("TOTAL ANS pt 2: $totalAns")
    return totalAns
}


fun main() {
    val input = readInput("Day19").map(::parseBlueprint)
    val testInput = readInput("Day19-test").map(::parseBlueprint)
    val testAnsPart1 = part1(testInput)
    val ansPart1 = part1(input)
    val testAnsPart2 = part2(testInput)
    val ansPart2 = part2(input)
    println()
    println("ANSWERS")
    println("test part1: $testAnsPart1")
    println("real part1: $ansPart1")
    println("test part2: $testAnsPart2")
    println("real part2: $ansPart2")
}