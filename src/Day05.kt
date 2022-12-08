import java.util.*

class Cargo(private val stacks: List<Deque<Char>>) {

    companion object Factory {
        fun create(description: List<String>): Cargo {
            val cargo = Cargo(List<Deque<Char>>(9) { _ -> ArrayDeque() }) // yep, hardcoded amount of stacks
            description.reversed().forEach { s -> cargo.addLayer(s) }
            return cargo
        }
    }

    fun addLayer(layer: String) {
        layer.chunked(4).forEachIndexed { i, box ->
            if (box[1] != ' ') {
                stacks[i].addLast(box[1])
            }
        }
    }
    /*
    fun print() {
        for (stack in stacks) {
            println(stack.joinToString("") { x -> x.toString() })
        }
    }
    */

    class Move(val from: Int, val to: Int, val amount: Int) {
        companion object Factory {
            fun create(description: String): Move {
                val regex = Regex("""move (\d+) from (\d+) to (\d+)""")
                val match = regex.matchEntire(description)!!
                val groups = match.groups.drop(1)
                val ints = groups.map { x -> x!!.value.toInt() }
                val (amount, from, to) = ints
                return Move(from - 1, to - 1, amount)
            }
        }
    }

    fun moveOneByOne(description: Move) {
        for (i in 1..description.amount) {
            stacks[description.to].addLast(stacks[description.from].removeLast())
        }
    }

    fun moveTogether(description: Move) {
        val tmp = ArrayDeque<Char>()
        for (i in 1..description.amount) {
            tmp.addLast(stacks[description.from].removeLast())
        }
        for (i in 1..description.amount) {
            stacks[description.to].addLast(tmp.removeLast())
        }
    }

    fun getTops(): String {
        return stacks.joinToString("") { stack -> stack.last().toString() }
    }
}

fun main() {

    fun part1(input: List<String>): String {
        val cargoDescription = input.takeWhile { s -> s[1] != '1' }
        val cargo = Cargo.create(cargoDescription)
        // println("Cargo vv")
        // cargo.print()
        // println("Cargo ^^")
        for (i in cargoDescription.size + 2 until input.size) {
            cargo.moveOneByOne(Cargo.Move.create(input[i]))
        }
        return cargo.getTops()
    }

    fun part2(input: List<String>): String {
        val cargoDescription = input.takeWhile { s -> s[1] != '1' }
        val cargo = Cargo.create(cargoDescription)
        for (i in cargoDescription.size + 2 until input.size) {
            cargo.moveTogether(Cargo.Move.create(input[i]))
        }
        return cargo.getTops()
    }

    val input = readInput("Day05")
    println("Day 5")
    println(part1(input))
    println(part2(input))
}