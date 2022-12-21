enum class Operation(val symbol: String) {
    CONSTANT("C"),
    ADD("+"),
    SUBSTRACT("-"),
    MULTIPLY("*"),
    DIVIDE("/"),
    COMPARE("="),
    HUMAN("H");
    companion object {
        private val mapping = values().associateBy(Operation::symbol)
        fun fromSymbol(symbol: String): Operation {
            return mapping[symbol]!!
        }
    }
}

fun main() {

    fun nullableAdd(a: Long?, b: Long?): Long? {
        return b?.let { a?.plus(it) }
    }
    fun nullableSubstract(a: Long?, b: Long?): Long? {
        return b?.let { a?.minus(it) }
    }
    fun nullableDivide(a: Long?, b: Long?): Long? {
        if (a != null && b != null && a % b != 0L) {
            throw Exception("Not divisible")
        }
        return b?.let { a?.div(it) }
    }
    fun nullableMultiply(a: Long?, b: Long?): Long? {
        return b?.let { a?.times(it) }
    }
    class Monkey(val operation: Operation, val children: List<String>, var value: Long?) {
        fun calculateFromChildren(tree: Map<String, Monkey>) {
            children.forEach { ch -> tree[ch]!!.calculateFromChildren(tree) }
            when (operation) {
                Operation.CONSTANT -> return
                Operation.HUMAN -> return
                Operation.COMPARE -> return
                else -> {
                    val left = tree[children[0]]!!.value
                    val right = tree[children[1]]!!.value
                    value = when(operation) {
                        Operation.ADD -> nullableAdd(left, right)
                        Operation.SUBSTRACT -> nullableSubstract(left, right)
                        Operation.MULTIPLY -> nullableMultiply(left, right)
                        Operation.DIVIDE -> nullableDivide(left, right)
                        else -> {
                            throw Exception("Somehow operation list not exastive")
                        }
                    }
                }
            }
        }

        fun calculateFromParent(expectedValue: Long?, tree: Map<String, Monkey>) {
            value = expectedValue
            if (children.isEmpty()) {
                return
            }
            val left = tree[children[0]]!!
            val right = tree[children[1]]!!
            if (left.value == null) {
                when(operation) {
                    Operation.COMPARE -> left.calculateFromParent(right.value!!, tree)
                    Operation.ADD -> left.calculateFromParent(value!! - right.value!!, tree)
                    Operation.MULTIPLY -> {
                        if (value!! % right.value!! != 0L) {
                            throw Exception("Can't reverse multiplication")
                        }
                        left.calculateFromParent(value!! / right.value!!, tree)
                    }
                    Operation.DIVIDE -> left.calculateFromParent(value!! * right.value!!, tree)
                    Operation.SUBSTRACT -> left.calculateFromParent(value!! + right.value!!, tree)
                    else -> {
                        throw Exception("Constant/human have children??? oO")
                    }
                }
                return
            }
            when(operation) {
                Operation.COMPARE -> right.calculateFromParent(left.value!!, tree)
                Operation.ADD -> right.calculateFromParent(value!! - left.value!!, tree)
                Operation.MULTIPLY -> {
                    if (value!! % left.value!! != 0L) {
                        throw Exception("Can't reverse multiplication")
                    }
                    right.calculateFromParent(value!! / left.value!!, tree)
                }
                Operation.DIVIDE -> {
                    if (left.value!! % value!! != 0L) {
                        throw Exception("Can't reverse division")
                    }
                    right.calculateFromParent(left.value!! / value!!, tree)
                }
                Operation.SUBSTRACT -> right.calculateFromParent(left.value!! - value!!, tree)
                else -> {
                    throw Exception("Constant/human have children??? oO")
                }
            }
        }
    }
    fun parseMonkey(input: String): Pair<String, Monkey> {
        val (name, formula) = input.split(":")
        if (name == "humn") {
            return (name to Monkey(Operation.HUMAN, listOf(), null))
        }
        if (formula.trim().split(" ").size == 1) {
            return (name to Monkey(Operation.CONSTANT, listOf(), formula.trim().toLong()))
        }
        val (left, operation, right) = formula.trim().split(" ")
        if (name == "root") {
            return name to Monkey(Operation.COMPARE, listOf(left, right), null)
        }
        return name to Monkey(Operation.fromSymbol(operation), listOf(left, right),null)
    }
    fun part2(input: List<String>): Long {
        val monkeys = input.associate(::parseMonkey)
        monkeys["root"]!!.calculateFromChildren(monkeys)
        monkeys["root"]!!.calculateFromParent(null, monkeys)
        return monkeys["humn"]!!.value!!
    }
    println("Day21 part2")
    val testInput = readInput("Day21-test")
    val realInput = readInput("Day21")
    println("test: ${part2(testInput)}")
    println("real: ${part2(realInput)}")
}