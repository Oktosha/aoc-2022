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

enum class Face() {
    ONE, TWO, THREE, FOUR, FIVE, SIX
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

    fun turn(state: State, command: String): State {
        return when (command) {
            "R" -> State(state.pos, Direction.fromCode((state.dir.code + 1) % 4))
            "L" -> State(state.pos, Direction.fromCode((state.dir.code - 1 + 4) % 4))
            else -> throw Exception("Unknown turn command")
        }
    }

    fun positionInFrontOnCube(state: State, cubeSize: Int, face: Face): Pair<Face, State> {
        val dummyPosition = state.pos + state.dir.vec
        if (dummyPosition.row < cubeSize
            && dummyPosition.column < cubeSize
            && dummyPosition.row >= 0
            && dummyPosition.column >= 0
        ) {
            return face to State(dummyPosition, state.dir)
        }
        when (face) {
            Face.ONE -> when (state.dir) {
                Direction.RIGHT -> return Face.THREE to State(
                    Position(0, cubeSize - state.pos.row - 1),
                    Direction.DOWN
                )

                Direction.DOWN -> return Face.TWO to State(
                    Position(0, state.pos.column),
                    Direction.DOWN
                )

                Direction.LEFT -> return Face.FOUR to State(
                    Position(0, state.pos.row),
                    Direction.DOWN
                )

                Direction.UP -> return Face.FIVE to State(
                    Position(0, cubeSize - state.pos.column - 1),
                    Direction.DOWN
                )
            }

            Face.TWO -> when (state.dir) {
                Direction.RIGHT -> return Face.THREE to State(
                    Position(state.pos.row, 0),
                    Direction.RIGHT
                )

                Direction.DOWN -> return Face.SIX to State(
                    Position(0, state.pos.column),
                    Direction.DOWN
                )

                Direction.LEFT -> return Face.FOUR to State(
                    Position(state.pos.row, cubeSize - 1),
                    Direction.LEFT
                )

                Direction.UP -> return Face.ONE to State(
                    Position(cubeSize - 1, state.pos.column),
                    Direction.UP
                )
            }

            Face.THREE -> when (state.dir) {
                Direction.RIGHT -> return Face.FIVE to State(
                    Position(state.pos.row, 0),
                    Direction.RIGHT
                )

                Direction.DOWN -> return Face.SIX to State(
                    Position(state.pos.column, cubeSize - 1),
                    Direction.LEFT
                )

                Direction.LEFT -> return Face.TWO to State(
                    Position(state.pos.row, cubeSize - 1),
                    Direction.LEFT
                )

                Direction.UP -> return Face.ONE to State(
                    Position(cubeSize - 1 - state.pos.column, cubeSize - 1),
                    Direction.LEFT
                )
            }

            Face.FOUR -> when (state.dir) {
                Direction.RIGHT -> return Face.TWO to State(
                    Position(state.pos.row, 0),
                    Direction.RIGHT
                )

                Direction.DOWN -> return Face.SIX to State(
                    Position(cubeSize - 1 - state.pos.column, 0),
                    Direction.RIGHT
                )

                Direction.LEFT -> return Face.FIVE to State(
                    Position(state.pos.row, cubeSize - 1),
                    Direction.LEFT
                )

                Direction.UP -> return Face.ONE to State(
                    Position(state.pos.column, 0),
                    Direction.RIGHT
                )
            }

            Face.FIVE -> when (state.dir) {
                Direction.RIGHT -> return Face.FOUR to State(
                    Position(state.pos.row, 0),
                    Direction.RIGHT
                )

                Direction.DOWN -> return Face.SIX to State(
                    Position(cubeSize - 1, cubeSize - 1 - state.pos.column),
                    Direction.UP
                )

                Direction.LEFT -> return Face.THREE to State(
                    Position(state.pos.row, cubeSize - 1),
                    Direction.LEFT
                )

                Direction.UP -> return Face.ONE to State(
                    Position(0, cubeSize - 1 - state.pos.column),
                    Direction.DOWN
                )
            }

            Face.SIX -> when (state.dir) {
                Direction.RIGHT -> return Face.THREE to State(
                    Position(cubeSize - 1, state.pos.row),
                    Direction.UP
                )

                Direction.DOWN -> return Face.FIVE to State(
                    Position(cubeSize - 1, cubeSize - 1 - state.pos.column),
                    Direction.UP
                )

                Direction.LEFT -> return Face.FOUR to State(
                    Position(cubeSize - 1, cubeSize - 1 - state.pos.row),
                    Direction.UP
                )

                Direction.UP -> return Face.TWO to State(
                    Position(cubeSize - 1, state.pos.column),
                    Direction.UP
                )
            }
        }
    }

    fun testPositionInFrontOnCube() {
        val cubeSize = 7
        for (face in Face.values()) {
            for (row in 0 until cubeSize) {
                for (column in 0 until cubeSize) {
                    for (direction in Direction.values()) {
                        val startState = State(Position(row, column), direction)
                        val (nextFace, nextState) = positionInFrontOnCube(startState, cubeSize, face)
                        val turned = turn(turn(nextState, "L"), "L")
                        val (returnedFace, returnedState) = positionInFrontOnCube(turned, cubeSize, nextFace)
                        val turnedOnStart = turn(turn(returnedState, "L"), "L")
                        if (returnedFace != face || turnedOnStart != startState) {
                            println("State $startState on face $face doesn't return back")
                            println("It goes to $nextState on face $nextFace")
                            println("Than turns to be $turned")
                            println("Than goes to $returnedState on face $returnedFace")
                            println("And after reversing rotation is $turnedOnStart")
                            throw Exception()
                        }
                    }
                }
            }
        }
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
            state = if (command == "R" || command == "L") {
                turn(state, command)
            } else {
                moveForward(command.toInt(), state, field)
            }
        }
        println(state)
        return state.password()
    }

    testPositionInFrontOnCube()
    println("Day22")
    val testInput = readInput("Day22-test")
    val input = readInput("Day22")
    println("part1 test ${part1(testInput)}")
    println("part1 real ${part1(input)}")
}