fun main() {
    class Monkey(val children: List<String>, val operation: (List<Long>) -> Long) {
        fun getValue(tree: Map<String, Monkey>): Long {
            return operation(children.map { x -> tree[x]!!.getValue(tree) })
        }
    }

    val operations: Map<String, (List<Long>) -> Long> = mapOf(
        "+" to { x -> x[0] + x[1] },
        "-" to { x -> x[0] - x[1] },
        "*" to { x -> x[0] * x[1] },
        "/" to { x ->
            if (x[0] % x[1] == 0L) {
                x[0] / x[1]
            } else {
                throw Exception()
            }
        })

    fun parseMonkey(input: String): Pair<String, Monkey> {
        val (name, formula) = input.split(":")
        if (formula.trim().split(" ").size == 1) {
            val value = formula.trim().toLong()
            val operationFunction =
                if (name == "humn") {
                    fun(_: List<Long>): Long {
                        println("humn called")
                        return value
                    }
                } else {
                    fun(_: List<Long>): Long {
                        return value
                    }
                }
            return (name to Monkey(listOf(), operationFunction))
        }
        val (left, operation, right) = formula.trim().split(" ")
        return name to Monkey(listOf(left, right), operations[operation]!!)
    }

    fun parseInput(input: List<String>): Map<String, Monkey> {
        return input.associate { s -> parseMonkey(s) }
    }

    fun part1(monkeys: Map<String, Monkey>): Long {
        return monkeys["root"]!!.getValue(monkeys)
    }

    println("Day 21")
    val testInput = readInput("Day21-test")
    val realInput = readInput("Day21")
    val testMonkeys = parseInput(testInput)
    val realMonkeys = parseInput(realInput)
    println("Part1 test: ${part1(testMonkeys)}")
    println("Part1 real: ${part1(realMonkeys)}")
}