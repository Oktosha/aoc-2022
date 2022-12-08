class MathVector(val x: Int, val y: Int) {
    operator fun plus(other: MathVector): MathVector {
        return MathVector(x + other.x, y + other.y)
    }
}

val DIRECTION_OF_VIEW = listOf(MathVector(0, 1), MathVector(1, 0), MathVector(0, -1), MathVector(-1, 0))

@Suppress("BooleanMethodIsAlwaysInverted")
fun isWithinBorders(position: MathVector, borders: MathVector): Boolean {
    return position.x >= 0 && position.y >= 0 && position.x < borders.x && position.y < borders.y
}

fun part1(input: List<String>, borders: MathVector): Int {
    val marks = MutableList(input.size) { _ -> MutableList(input[0].length) { _ -> 0 } }
    val startPosition =
        listOf(MathVector(0, 0), MathVector(0, 0), MathVector(0, borders.y - 1), MathVector(borders.x - 1, 0))
    val directionOfStartPositionChange = listOf(MathVector(1, 0), MathVector(0, 1), MathVector(1, 0), MathVector(0, 1))
    for (i in 0 until 4) {
        var currentStartPosition = startPosition[i]
        while (isWithinBorders(currentStartPosition, borders)) {
            var position = currentStartPosition
            var maxHeight = -1
            while (isWithinBorders(position, borders)) {
                if (input[position.x][position.y].digitToInt() > maxHeight) {
                    marks[position.x][position.y] = 1
                    maxHeight = input[position.x][position.y].digitToInt()
                }
                position += DIRECTION_OF_VIEW[i]
            }
            currentStartPosition += directionOfStartPositionChange[i]
        }
        // println(marks.joinToString("\n"))
        // println("====")
        // marks = MutableList(input.size) { _ -> MutableList(input[0].length) { _ -> 0 } }
    }
    return marks.sumOf { x -> x.sum() }
}

fun part2(input: List<String>, borders: MathVector): Int {
    var ans = 0
    for (x in 0 until borders.x) {
        for (y in 0 until borders.y) {
            var currentTotal = 1
            for (direction in DIRECTION_OF_VIEW) {
                var currentVisibleTrees = 0
                var currentPosition = MathVector(x, y) + direction
                while (isWithinBorders(currentPosition, borders)) {
                    currentVisibleTrees += 1
                    if (input[currentPosition.x][currentPosition.y].digitToInt() >= input[x][y].digitToInt()) {
                        break
                    }
                    currentPosition += direction
                }
                currentTotal *= currentVisibleTrees
            }
            if (currentTotal > ans) {
                ans = currentTotal
            }
            // print(currentTotal)
        }
        // println()
    }
    return ans
}

fun main() {
    val input = readInput("Day08")
    val borders = MathVector(input.size, input[0].length)

    println("Day 08")
    println(part1(input, borders))
    println(part2(input, borders))
}