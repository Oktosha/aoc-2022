import kotlin.math.abs

fun main() {
    fun getAnsIncrement(x: Int, op: Int): Int {
        assert(op < 260)
        if (op >= 20 && (op - 20) % 40 == 0) {
            println("$op * $x = ${op * x}")
            return op * x
        }
        return 0
    }

    fun part1(input: List<String>): Int {
        var x = 1
        var op = 1
        var ans = 0
        for (line in input) {
            val cmd = line.split(" ")
            op += 1
            ans += getAnsIncrement(x, op)
            if (cmd[0] == "addx") {
                op += 1
                x += cmd[1].toInt()
                ans += getAnsIncrement(x, op)
            }
        }
        return ans
    }

    class State(var x: Int, var op: Int)

    fun printState(state: State) {
        val pos = state.op % 40
        if (pos == 0) {
            println()
        }
        print(if (abs(state.x - pos) <= 1) "#" else ".")
    }

    fun part2(input: List<String>) {
        val state = State(1, 0)
        printState(state)
        for (line in input) {
            val cmd = line.split(" ")
            state.op += 1
            printState(state)
            if (cmd[0] == "addx") {
                state.op += 1
                state.x += cmd[1].toInt()
                printState(state)
            }
        }
    }

    println("Day 10")
    val input = readInput("Day10")
    println(part1(input))
    part2(input)

}