fun main() {
    fun getPriorityOfAnItem(item: Char): Int {
        if (item.isLowerCase())
            return item.code - 'a'.code + 1
        return item.code - 'A'.code + 27
    }

    fun getPriorityOf1Rucksack(items: String): Int {
        val compartment1 = items.subSequence(0, items.length / 2).toSet()
        val compartment2 = items.subSequence(items.length / 2, items.length).toSet()
        return getPriorityOfAnItem(compartment1.intersect(compartment2).first())
    }

    fun part1(input: List<String>): Int {
        return input.map(::getPriorityOf1Rucksack).sum()
    }

    fun getCommonLetter(s1: String, s2: String, s3: String): Char {
        return s1.toSet().intersect(s2.toSet()).intersect(s3.toSet()).first()
    }

    fun part2(input: List<String>): Int {
        return input.chunked(3) { (a, b, c) -> getPriorityOfAnItem(getCommonLetter(a, b, c)) }.sum()
    }

    val input = readInput("Day03")
    println("Day 3")
    println(part1(input))
    println(part2(input))
}