typealias Field = List<String>

data class Position(val row: Int, val column: Int) {
    operator fun plus(other: Position): Position {
        return Position(row + other.row, column + other.column)
    }
}

enum class Direction(val code: Int, val vec: Position) {
    RIGHT(0, Position(0, 1)),
    DOWN(1, Position(1, 0)),
    LEFT(2, Position(0, -1)),
    UP(3, Position(-1, 0));
    companion object {
        private val codeMapping = Direction.values().associateBy(Direction::code)
        fun fromCode(code: Int): Direction {
            return codeMapping[code]!!
        }
    }
}

fun main() {
    data class State(val pos: Position, val dir: Direction) {
        fun password(): Int {
            return 1000 * (pos.row + 1) + 4 * (pos.column + 1) + dir.code
        }
    }

    operator fun Field.get(pos: Position): Char {
        return this[pos.row][pos.column]
    }

    fun Field.rows(): Int {
        return this.size
    }

    fun Field.columns(): Int {
        return this[0].length
    }

    fun simpleWrap(pos: Position, field: Field): Position {
        return Position(
            (pos.row + field.rows()) % field.rows(),
            (pos.column + field.columns()) % field.columns()
        )
    }

    fun positionInFront(state: State, field: Field): Position {
        var current = state.pos
        do {
            current = simpleWrap(current + state.dir.vec, field)
        } while (field[current] == ' ')
        return current
    }

    fun moveForward(steps: Int, state: State, field: Field): State {
        var current = state
        repeat(steps) {
            if (field[positionInFront(current, field)] == '.') {
                current = State(positionInFront(current, field), current.dir)
            }
        }
        return current
    }

    fun getStartPosition(field: Field): Position {
        return Position(0, field[0].indexOf('.'))
    }

    fun normalizedField(input: List<String>): Field {
        val desiredLengh = input.maxOf { s -> s.length }
        return input.map { s -> s.padEnd(desiredLengh, ' ') }
    }
    fun part1(input: List<String>): Int {
        val field = normalizedField(input.dropLast(2))
        val commandString = input.last()
        val commandList = commandString
            .replace(Regex("""\p{Upper}""")) { x: MatchResult -> " ${x.value} " }
            .split(" ").filter { s -> s != "" }
        var state = State(getStartPosition(field), Direction.RIGHT)
        for (command in commandList) {
            state = when(command) {
                "R" -> State(state.pos, Direction.fromCode((state.dir.code + 1) % 4))
                "L" -> State(state.pos, Direction.fromCode((state.dir.code - 1 + 4) % 4))
                else -> moveForward(command.toInt(), state, field)
            }
        }
        println(state)
        return state.password()
    }

    println("Day22")
    val testInput = readInput("Day22-test")
    val input = readInput("Day22")
    println("part1 test ${part1(testInput)}")
    println("part1 real ${part1(input)}")
}