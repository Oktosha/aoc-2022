fun main() {
    fun findMarker(data: String, length: Int): Int {
        for (i in length..data.length) {
            if (data.substring(i - length, i).toSet().size == length)
                return i
        }
        return -1
    }

    val input = readInput("Day06")
    println("Day 6")
    println(findMarker(input[0], 4))
    println(findMarker(input[0], 14))
}