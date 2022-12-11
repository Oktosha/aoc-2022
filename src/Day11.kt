import java.io.File
import java.math.BigInteger

interface Item {
    fun multiplyWorryLevel(by: Int)
    fun increaseWorryLevel(by: Int)
    fun squareWorryLevel()
    fun postprocess()

    @Suppress("BooleanMethodIsAlwaysInverted")
    fun isWorryLevelDivisible(by: Int): Boolean
}

class BigIntegerItem(private var worryLevel: BigInteger) : Item {

    companion object Factory {
        fun create(worryLevel: Int): BigIntegerItem {
            return BigIntegerItem(worryLevel.toBigInteger())
        }
    }

    override fun multiplyWorryLevel(by: Int) {
        worryLevel *= by.toBigInteger()
    }

    override fun increaseWorryLevel(by: Int) {
        worryLevel += by.toBigInteger()
    }

    override fun squareWorryLevel() {
        worryLevel *= worryLevel
    }

    override fun postprocess() {
        worryLevel /= BigInteger.valueOf(3)
    }

    override fun isWorryLevelDivisible(by: Int): Boolean {
        return worryLevel % by.toBigInteger() == BigInteger.ZERO
    }

}

class RemaindersItem(private val worryLevelRemainders: MutableMap<Int, Int>) : Item {

    companion object Factory {
        fun create(worryLevel: Int, trackedDivisors: List<Int>): RemaindersItem {
            val item = RemaindersItem(mutableMapOf())
            for (divisor in trackedDivisors) {
                item.worryLevelRemainders[divisor] = worryLevel % divisor
            }
            return item
        }
    }

    override fun multiplyWorryLevel(by: Int) {
        for ((key, value) in worryLevelRemainders) {
            worryLevelRemainders[key] = (value * by) % key
        }
    }

    override fun increaseWorryLevel(by: Int) {
        for ((key, value) in worryLevelRemainders) {
            worryLevelRemainders[key] = (value + by) % key
        }
    }

    override fun squareWorryLevel() {
        for ((key, value) in worryLevelRemainders) {
            worryLevelRemainders[key] = (value * value) % key
        }
    }

    override fun postprocess() {
        // Do nothing
    }

    @Suppress("BooleanMethodIsAlwaysInverted")
    override fun isWorryLevelDivisible(by: Int): Boolean {
        return worryLevelRemainders[by] == 0
    }
}

class Monkey(
    val rawItems: String,
    val items: ArrayDeque<Item>,
    val inspect: (Item) -> Unit,
    val decisionConstant: Int,
    private val decisionIfDivisible: Int,
    private val decisionIfNotDivisible: Int,
    var inspectCount: Long = 0
) {
    fun decide(item: Item): Int {
        return if (item.isWorryLevelDivisible(decisionConstant)) {
            decisionIfDivisible
        } else {
            decisionIfNotDivisible
        }
    }
}

fun pushItemsToMonkeys(monkeys: List<Monkey>, createItem: (Int) -> Item) {
    monkeys.forEach { monkey ->
        monkey.items.addAll(monkey.rawItems.split(",").map { i -> createItem(i.trim().toInt()) })
    }
}

fun populateBigIntegerItems(monkeys: List<Monkey>): List<Monkey> {
    pushItemsToMonkeys(monkeys, BigIntegerItem.Factory::create)
    return monkeys
}

fun populateRemaindersItems(monkeys: List<Monkey>): List<Monkey> {
    val decisionConstants = monkeys.map { monkey -> monkey.decisionConstant }
    val createItem = { x: Int -> RemaindersItem.create(x, decisionConstants) }
    pushItemsToMonkeys(monkeys, createItem)
    return monkeys
}

fun readOperation(input: String): (Item) -> Unit {
    if (input.count { x -> x == 'o' } == 3) {
        return { item -> run { item.squareWorryLevel() } }
    }
    val operationValue = input.split(" ").last().toInt()
    if (input.contains('*'))
        return { item -> run { item.multiplyWorryLevel(operationValue) } }
    return { item -> run { item.increaseWorryLevel(operationValue) } }
}

fun readMonkey(input: String): Monkey {
    val lines = input.split("\n")
    val rawItems = lines[1].split(":")[1]
    val operation = readOperation(lines[2])
    val decisionConstant = lines[3].split(" ").last().toInt()
    val decisionIfDivisible = lines[4].split(" ").last().toInt()
    val decisionIfNotDivisible = lines[5].split(" ").last().toInt()
    return Monkey(rawItems, ArrayDeque(), operation, decisionConstant, decisionIfDivisible, decisionIfNotDivisible)
}

fun readMonkeys(input: String): List<Monkey> {
    return input.trim().split("\n\n").map(::readMonkey)
}

fun prepareMonkeys(input: String, initItems: (List<Monkey>) -> List<Monkey>): List<Monkey> {
    return initItems(readMonkeys(input))
}

fun modelMonkeys(monkeys: List<Monkey>, rounds: Int): Long {
    repeat(rounds) {
        for (monkey in monkeys) {
            while (monkey.items.isNotEmpty()) {
                val item = monkey.items.removeFirst()
                monkey.inspect(item)
                monkey.inspectCount += 1
                item.postprocess()
                monkeys[monkey.decide(item)].items.addLast(item)
            }
        }
    }
    val sorted = monkeys.sortedBy { x -> -x.inspectCount }
    return sorted[0].inspectCount * sorted[1].inspectCount
}

fun main() {
    val testInput = File("src", "Day11-test.txt").readText()
    val input = File("src", "Day11.txt").readText()
    println("Day 11")
    println("test part1: ${modelMonkeys(prepareMonkeys(testInput, ::populateBigIntegerItems), 20)}")
    println("ans part1: ${modelMonkeys(prepareMonkeys(input, ::populateBigIntegerItems), 20)}")
    println("test part2: ${modelMonkeys(prepareMonkeys(testInput, ::populateRemaindersItems), 10000)}")
    println("ans part2: ${modelMonkeys(prepareMonkeys(input, ::populateRemaindersItems), 10000)}")
}