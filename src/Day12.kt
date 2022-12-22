typealias Grid = MutableList<MutableList<Int>>

fun main() {
    class Position(val h: Int, val w: Int)

    operator fun Grid.get(pos: Position): Int {
        return this[pos.h][pos.w]
    }

    operator fun Grid.set(pos: Position, value: Int) {
        this[pos.h][pos.w] = value
    }

    // Same false positive for unused
    operator fun List<String>.get(pos: Position): Char {
        return this[pos.h][pos.w]
    }

    fun Grid.neigbours(pos: Position): List<Position> {
        val delta = listOf(Position(0, 1), Position(1, 0), Position(0, -1), Position(-1, 0))
        return delta.map { d -> Position(d.h + pos.h, d.w + pos.w) }
            .filter { p -> p.h >= 0 && p.w >= 0 && p.h < this.size && p.w < this[0].size }
    }

    fun findLetterPosition(input: List<String>, letter: Char): Position {
        val h = input.indexOfFirst { line -> line.contains(letter) }
        assert(h != -1)
        val w = input[h].indexOf(letter)
        return Position(h, w)
    }

    fun elevation(c: Char): Int {
        fun posInAlpnabet(x: Char): Int {
            return x.code - 'a'.code
        }
        assert((c in 'a'..'z') || c == 'S' || c == 'E')
        if (c == 'S') {
            return posInAlpnabet('a')
        }
        if (c == 'E') {
            return posInAlpnabet('z')
        }
        return posInAlpnabet(c)
    }

    fun initBFSPart1(input: List<String>, grid: Grid): ArrayDeque<Position> {
        val startPosition = findLetterPosition(input, 'S')
        grid[startPosition] = 0
        val queue = ArrayDeque(listOf(startPosition))
        queue.addLast(startPosition)
        return queue
    }

    fun initBFSPart2(input: List<String>, grid: Grid): ArrayDeque<Position> {
        val queue = ArrayDeque<Position>()
        for (h in input.indices) {
            for (w in input[h].indices) {
                val pos = Position(h, w)
                if (elevation(input[pos]) == 0) {
                    grid[pos] = 0
                    queue.addLast(pos)
                }
            }
        }
        return queue
    }

    fun solve(input: List<String>, initBFS: (List<String>, Grid) -> ArrayDeque<Position>): Int {
        val width = input[0].length
        val height = input.size
        val infinity = width * height + 5
        val grid: Grid = MutableList(height) { _ -> MutableList(width) { _ -> infinity } }
        val queue = initBFS(input, grid)
        while (queue.isNotEmpty()) {
            val position = queue.removeFirst()
            for (next in grid.neigbours(position)) {
                if (elevation(input[next]) - elevation(input[position]) <= 1 && grid[next] == infinity) {
                    grid[next] = grid[position] + 1
                    queue.addLast(next)
                }
            }
        }
        val endPosition = findLetterPosition(input, 'E')
        return grid[endPosition]
    }

    println("Day 12")
    val testInput = readInput("Day12-test")
    val input = readInput("Day12")
    println("1-test: ${solve(testInput, ::initBFSPart1)}")
    println("1-real: ${solve(input, ::initBFSPart1)}")
    println("2-test: ${solve(testInput, ::initBFSPart2)}")
    println("2-real: ${solve(input, ::initBFSPart2)}")
}