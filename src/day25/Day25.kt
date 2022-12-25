package day25

import kotlin.math.max
import readInput

const val BASE = 5

enum class SNAFUDigit(val value: Int, val symbol: Char) {
    DOUBLE_MINUS(-2, '='),
    MINUS(-1, '-'),
    ZERO(0, '0'),
    ONE(1, '1'),
    TWO(2, '2');

    companion object {
        private val charToDigit = SNAFUDigit.values().associateBy { it.symbol }
        fun fromValue(value: Int): SNAFUDigit {
            return SNAFUDigit.values()[value + 2]
        }
        fun fromChar(value: Char): SNAFUDigit {
            return charToDigit[value]!!
        }
    }
}

class SNAFUNumber(val value: List<SNAFUDigit>) {
    companion object {
        fun fromString(input: String): SNAFUNumber {
            return SNAFUNumber(input.map(SNAFUDigit.Companion::fromChar).reversed())
        }
    }
    operator fun plus(other: SNAFUNumber): SNAFUNumber {
        val ans = mutableListOf<SNAFUDigit>()
        var carry = 0
        for (i in 0..max(this.value.size, other.value.size) + 2) {
            val sum = carry + this.digitOrZero(i).value + other.digitOrZero(i).value
            val rawResult = (BASE + sum) % BASE
            val result = if (rawResult <= 2) {
                rawResult
            } else {
                rawResult - 5
            }
            ans.add(SNAFUDigit.fromValue(result))
            carry = if (sum >= 3) {
                1
            }else if (sum <= -3) {
                -1
            } else {
                0
            }
            print(":${SNAFUDigit.fromValue(result).symbol}")
        }
        println()
        return SNAFUNumber(ans.dropLastWhile { it == SNAFUDigit.ZERO })
    }

    private fun digitOrZero(pos: Int): SNAFUDigit {
        return value.elementAtOrNull(pos) ?: SNAFUDigit.ZERO
    }

    override fun toString(): String {
        return value.reversed().map { x -> x.symbol }.joinToString("", "", "")
    }
}

fun part1(input: List<String>): String {
    return input.map(SNAFUNumber.Companion::fromString).reduce{x, y -> x + y}.toString()
}

fun main() {
    println("Day 25")
    val input = readInput("Day25")
    var num = SNAFUNumber.fromString("1")
    for (i in 1 .. 1000) {
        num = SNAFUNumber.fromString("1") + num
        println(num)
    }
    println("part 1: ${part1(input)}")
}