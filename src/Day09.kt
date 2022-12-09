import kotlin.math.abs
import kotlin.math.sign

class Knot(var x: Int, var y: Int)

fun solve(input: List<String>, ropeLength: Int): Int {
    val totalSteps = mutableMapOf("L" to 0, "R" to 0, "U" to 0, "D" to 0)
    input.forEach { x ->
        run {
            val (direction, count) = x.split(" ")
            totalSteps[direction] = totalSteps[direction]!! + count.toInt()
        }
    }
    val grid =
        MutableList(totalSteps["U"]!! + totalSteps["D"]!! + 1) { _ -> MutableList(totalSteps["R"]!! + totalSteps["L"]!! + 1) { _ -> 0 } }
    val rope = List(ropeLength) { _ -> Knot(totalSteps["L"]!!, totalSteps["U"]!!) }
    grid[rope.last().y][rope.last().x] = 1
    for (instruction in input) {
        val direction = instruction.split(" ")[0]
        val count = instruction.split(" ")[1].toInt()
        repeat (count) {
            when (direction) {
                "L" -> rope[0].x -= 1
                "R" -> rope[0].x += 1
                "U" -> rope[0].y -= 1
                "D" -> rope[0].y += 1
            }
            for (i in 1 until rope.size) {
                if (abs(rope[i - 1].x - rope[i].x) > 1 || abs(rope[i - 1].y - rope[i].y) > 1) {
                    rope[i].x += (rope[i - 1].x - rope[i].x).sign
                    rope[i].y += (rope[i - 1].y - rope[i].y).sign
                }
            }
            grid[rope.last().y][rope.last().x] = 1
        }
    }
    // grid.forEach{line -> run { println(line.joinToString("")) }}
    return grid.sumOf { line -> line.sum() }
}

fun main() {
    println("Day 09")
    val input = readInput("Day09")
    println(solve(input, 2))
    println(solve(input, 10))
}