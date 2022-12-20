import kotlin.math.abs
import kotlin.math.sign

typealias Message = MutableList<NumberWithMeta>

fun Message.valueAt(index: Int): Long {
    return this[index % this.size].value
}

data class NumberWithMeta(val value: Long, val originalPosition: Int)

fun main() {

    fun readInputDay20(filename: String): Message {
        return readInput(filename).mapIndexed { index, s -> NumberWithMeta(s.toLong(), index) }.toMutableList()
    }

    fun readInputDay20pt2(filename: String): Message {
        return readInput(filename).mapIndexed { index, s -> NumberWithMeta(s.toLong() * 811589153L, index) }
            .toMutableList()
    }

    fun swap(input: Message, position: Int, direction: Int): Int {
        val nextPosition = (position + input.size + direction) % input.size
        val temp = input[nextPosition]
        input[nextPosition] = input[position]
        input[position] = temp
        return nextPosition
    }

    fun mix(input: Message): Message {
        for (index in 0 until input.size) {
            var position = input.indexOfFirst { x -> x.originalPosition == index }
            val numberOfSteps = (abs(input[position].value) % (input.size - 1).toLong()).toInt()
            val direction = input[position].value.sign
            repeat(numberOfSteps) {
                position = swap(input, position, direction)
            }
//            print("step: $index")
//            for (x in input) {
//                print(" ${x.value}")
//            }
//            println()
        }
        return input
    }

    fun getGrooveCoordinatesSum(input: Message): Long {
        val start = input.indexOfFirst { x -> x.value == 0L }
        return input.valueAt(start + 1000) + input.valueAt(start + 2000) + input.valueAt(start + 3000)
    }

    fun part1(input: Message): Long {
        return getGrooveCoordinatesSum(mix(input))
    }

    fun part2(input: Message): Long {
        repeat(10) {
            mix(input)
        }
        return getGrooveCoordinatesSum(input)
    }

    val testInput = readInputDay20("Day20-test")
    val input = readInputDay20("Day20")
    val testInput2 = readInputDay20pt2("Day20-test")
    val input2 = readInputDay20pt2("Day20")
    println("Day 20")
    println("part 1 test: ${part1(testInput)}")
    println("part 1 real: ${part1(input)}")
    println("part 2 test: ${part2(testInput2)}")
    println("part 2 real: ${part2(input2)}")
}