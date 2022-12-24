package day13

import pass
import java.io.File
import kotlin.math.min
import kotlin.math.sign

sealed class PacketElement {
    class Num(val value: Int) : PacketElement() {
        override fun toString(): String {
            return value.toString()
        }
    }
    class Lst(val elements: MutableList<PacketElement>) : PacketElement() {
        override fun toString(): String {
            return elements.joinToString(prefix = "[", postfix = "]", separator = ",")
        }
    }
}

fun cmpSignForList(l1: PacketElement.Lst, l2: PacketElement.Lst): Int {
    val len = min(l1.elements.size, l2.elements.size)
    for (i in 0 until len) {
        val currentSign = cmpSign(l1.elements[i], l2.elements[i])
        if (currentSign != 0) {
            return currentSign
        }
    }
    return (l1.elements.size - l2.elements.size).sign
}

fun cmpSign(p1: PacketElement, p2: PacketElement): Int {
    return when (p1) {
        is PacketElement.Num -> when (p2) {
            is PacketElement.Num -> (p1.value - p2.value).sign
            is PacketElement.Lst -> cmpSign(PacketElement.Lst(mutableListOf(p1)), p2)
        }

        is PacketElement.Lst -> when (p2) {
            is PacketElement.Num -> cmpSign(p1, PacketElement.Lst(mutableListOf(p2)))
            is PacketElement.Lst -> cmpSignForList(p1, p2)
        }
    }
}

sealed class Token {
    class Num(val value: Int) : Token() {
        override fun toString(): String {
            return value.toString()
        }
    }
    // object Comma : Token()
    object OpenBracket : Token() {
        override fun toString(): String {
            return "["
        }
    }
    object ClosingBracket : Token() {
        override fun toString(): String {
            return "]"
        }
    }
}

fun tokenize(input: String): List<Token> {
    val ans = mutableListOf<Token>()
    var currentNum = -1
    for (ch in input) {
        if (ch.isDigit()) {
            currentNum = if (currentNum == -1) {
                ch.digitToInt()
            } else {
                currentNum * 10 + ch.digitToInt()
            }
        } else {
            if (currentNum != -1) {
                ans.add(Token.Num(currentNum))
                currentNum = -1
            }
            when (ch) {
                '[' -> ans.add(Token.OpenBracket)
                ']' -> ans.add(Token.ClosingBracket)
                ',' -> pass // ans.add(Token.Comma)
                else -> throw Exception("Invalid symbol $ch in input")
            }
        }
    }
    return ans
}

fun parsePacket(input: List<Token>): PacketElement {
    // println(input)
    val resultParent = PacketElement.Lst(mutableListOf())
    val stack = ArrayDeque(listOf(resultParent))
    for (token in input) {
        when (token) {
            Token.OpenBracket -> {
                val listElment = PacketElement.Lst(mutableListOf())
                stack.last().elements.add(listElment)
                stack.addLast(listElment)
            }

            Token.ClosingBracket -> {
                stack.removeLast()
            }

            is Token.Num -> {
                stack.last().elements.add(PacketElement.Num(token.value))
            }
        }
    }
    return resultParent.elements[0]
}

fun parsePacket(input: String): PacketElement {
    val answer = parsePacket(tokenize(input))
    if (answer.toString() != input) {
        throw Exception("Broken parsing:\n$input\n$answer")
    }
    return answer
}

fun part1(input: String): List<Int> {
    val objectPairs = input
        .trim()
        .split("\n\n")
        .map { it.split("\n").map(::parsePacket) }
    val ans = mutableListOf<Int>()
    for (i in objectPairs.indices) {
        val (p1, p2) = objectPairs[i]
        val cmpResult = cmpSign(p1, p2)
        if (cmpResult == -1) {
            ans.add(i + 1)
        } else if (cmpResult == 0) {
            throw Exception("Found equals at ${i + 1}!")
        }
    }
    return ans
}

fun part2(input: String): List<Int> {
    val packetStrings = input.split("\n").map{it.trim()}.filter { it != "" }
    val dividerPacketStrings = listOf("[[2]]", "[[6]]")
    val packets = (packetStrings + dividerPacketStrings).map(::parsePacket)
    val sortedPackets = packets.sortedWith { p1, p2 -> cmpSign(p1, p2) }
    return dividerPacketStrings.map { divider -> sortedPackets.indexOfFirst { it.toString() == divider } + 1}
}

fun main() {
    println("Day 13")
    val testInput = File("src","Day13-test.txt").readText()
    val input = File("src","Day13.txt").readText()
    val testAns1 = part1(testInput)
    println("test part 1: ${testAns1.sum()} <- $testAns1")
    val realAns1 = part1(input)
    println("real part 1: ${realAns1.sum()} <- $realAns1")
    val testAns2 = part2(testInput)
    println("test part 2: ${testAns2[0] * testAns2[1]} <- $testAns2")
    val realAns2 = part2(input)
    println("test part 2: ${realAns2[0] * realAns2[1]} <- $realAns2")
}