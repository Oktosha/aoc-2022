import kotlin.math.max
import java.util.PriorityQueue

fun main() {
    fun part1(input: List<String>): Int {
        var answer = 0
        var current = 0
        for (line in input) {
            if (line.trim() == "") {
                answer = max(current, answer)
                current = 0
            } else {
                current += line.toInt()
            }
        }
        answer = max(current, answer)
        return answer
    }

    fun part2(input: List<String>): Int {
        val highestValues = PriorityQueue<Int>()
        var current = 0
        for (line in input) {
            if (line.trim() == "") {
                highestValues.add(current)
                if (highestValues.size > 3) {
                    highestValues.remove()
                }
                current = 0
            } else {
                current += line.toInt()
            }
        }
        highestValues.add(current)
        if (highestValues.size > 3) {
            highestValues.remove()
        }
        return highestValues.fold(0) { x, y -> x + y }
    }

    val input = readInput("Day01")
    println(part1(input))
    println(part2(input))
}
