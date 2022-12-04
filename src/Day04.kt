import kotlin.math.max
import kotlin.math.min

fun main() {
    class Range(val begin: Int, val end: Int) {
        override operator fun equals(other: Any?): Boolean {
            if (other !is Range) {
                return false
            }
            return begin == other.begin && end == other.end
        }

        override fun hashCode(): Int {
            return Pair(begin, end).hashCode()
        }
    }

    fun parseRange(description: String): Range {
        val data = description.split('-').map { x -> x.toInt() }
        return Range(data[0], data[1])
    }

    fun parsePair(description: String): List<Range> {
        return description.split(',').map(::parseRange)
    }

    fun oneIsInsideOther(range1: Range, range2: Range): Boolean {
        val totalRange = Range(min(range1.begin, range2.begin), max(range1.end, range2.end))
        return totalRange == range1 || totalRange == range2
    }

    fun part1scorePair(description: String): Int {
        val data = parsePair(description)
        return if (oneIsInsideOther(data[0], data[1])) 1 else 0
    }

    fun part1(input: List<String>): Int {
        return input.map(::part1scorePair).sum()
    }

    fun isOverlap(range1: Range, range2: Range): Boolean {
        val intersectRange = Range(max(range1.begin, range2.begin), min(range1.end, range2.end))
        return intersectRange.begin <= intersectRange.end
    }

    fun part2scorePair(description: String): Int {
        val data = parsePair(description)
        return if (isOverlap(data[0], data[1])) 1 else 0
    }

    fun part2(input: List<String>): Int {
        return input.map(::part2scorePair).sum()
    }

    val input = readInput("Day04")
    println("Day 4")
    println(part1(input))
    println(part2(input))
}