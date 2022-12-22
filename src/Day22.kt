typealias FlatRepresentation = List<String>

data class Position(val row: Int, val column: Int) {
    operator fun plus(other: Position): Position {
        return Position(row + other.row, column + other.column)
    }
    operator fun minus(other: Position): Position {
        return Position(row - other.row, column - other.column)
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

enum class Face {
    ONE, TWO, THREE, FOUR, FIVE, SIX
}

data class State22(val pos: Position, val dir: Direction) {
    fun password(): Int {
        return 1000 * (pos.row + 1) + 4 * (pos.column + 1) + dir.code
    }
}

interface Field {
    val flatMap: FlatRepresentation
    fun moveForward(state: State22, steps: Int):State22
}

fun main() {


    operator fun FlatRepresentation.get(pos: Position): Char {
        return this[pos.row][pos.column]
    }

    fun FlatRepresentation.rows(): Int {
        return this.size
    }

    fun FlatRepresentation.columns(): Int {
        return this[0].length
    }

    fun simpleWrap(pos: Position, field: FlatRepresentation): Position {
        return Position(
            (pos.row + field.rows()) % field.rows(),
            (pos.column + field.columns()) % field.columns()
        )
    }

    fun flatPositionInFront(state: State22, field: FlatRepresentation): State22 {
        var current = state.pos
        do {
            current = simpleWrap(current + state.dir.vec, field)
        } while (field[current] == ' ')
        return State22(current, state.dir)
    }

    fun turnDir(dir: Direction, command: String): Direction {
        return when (command) {
            "R" -> Direction.fromCode((dir.code + 1) % 4)
            "L" -> Direction.fromCode((dir.code - 1 + 4) % 4)
            else -> throw Exception("Unknown turn command")
        }
    }
    fun turn(state: State22, command: String): State22 {
        return State22(state.pos, turnDir(state.dir, command))
    }

    fun positionInFrontOnCube(cubeState: Pair<Face,State22>, cubeSize: Int): Pair<Face, State22> {
        val (face, state) = cubeState
        val dummyPosition = state.pos + state.dir.vec
        if (dummyPosition.row < cubeSize
            && dummyPosition.column < cubeSize
            && dummyPosition.row >= 0
            && dummyPosition.column >= 0
        ) {
            return face to State22(dummyPosition, state.dir)
        }
        when (face) {
            Face.ONE -> when (state.dir) {
                Direction.RIGHT -> return Face.THREE to State22(
                    Position(0, cubeSize - state.pos.row - 1),
                    Direction.DOWN
                )

                Direction.DOWN -> return Face.TWO to State22(
                    Position(0, state.pos.column),
                    Direction.DOWN
                )

                Direction.LEFT -> return Face.FOUR to State22(
                    Position(0, state.pos.row),
                    Direction.DOWN
                )

                Direction.UP -> return Face.FIVE to State22(
                    Position(0, cubeSize - state.pos.column - 1),
                    Direction.DOWN
                )
            }

            Face.TWO -> when (state.dir) {
                Direction.RIGHT -> return Face.THREE to State22(
                    Position(state.pos.row, 0),
                    Direction.RIGHT
                )

                Direction.DOWN -> return Face.SIX to State22(
                    Position(0, state.pos.column),
                    Direction.DOWN
                )

                Direction.LEFT -> return Face.FOUR to State22(
                    Position(state.pos.row, cubeSize - 1),
                    Direction.LEFT
                )

                Direction.UP -> return Face.ONE to State22(
                    Position(cubeSize - 1, state.pos.column),
                    Direction.UP
                )
            }

            Face.THREE -> when (state.dir) {
                Direction.RIGHT -> return Face.FIVE to State22(
                    Position(state.pos.row, 0),
                    Direction.RIGHT
                )

                Direction.DOWN -> return Face.SIX to State22(
                    Position(state.pos.column, cubeSize - 1),
                    Direction.LEFT
                )

                Direction.LEFT -> return Face.TWO to State22(
                    Position(state.pos.row, cubeSize - 1),
                    Direction.LEFT
                )

                Direction.UP -> return Face.ONE to State22(
                    Position(cubeSize - 1 - state.pos.column, cubeSize - 1),
                    Direction.LEFT
                )
            }

            Face.FOUR -> when (state.dir) {
                Direction.RIGHT -> return Face.TWO to State22(
                    Position(state.pos.row, 0),
                    Direction.RIGHT
                )

                Direction.DOWN -> return Face.SIX to State22(
                    Position(cubeSize - 1 - state.pos.column, 0),
                    Direction.RIGHT
                )

                Direction.LEFT -> return Face.FIVE to State22(
                    Position(state.pos.row, cubeSize - 1),
                    Direction.LEFT
                )

                Direction.UP -> return Face.ONE to State22(
                    Position(state.pos.column, 0),
                    Direction.RIGHT
                )
            }

            Face.FIVE -> when (state.dir) {
                Direction.RIGHT -> return Face.FOUR to State22(
                    Position(state.pos.row, 0),
                    Direction.RIGHT
                )

                Direction.DOWN -> return Face.SIX to State22(
                    Position(cubeSize - 1, cubeSize - 1 - state.pos.column),
                    Direction.UP
                )

                Direction.LEFT -> return Face.THREE to State22(
                    Position(state.pos.row, cubeSize - 1),
                    Direction.LEFT
                )

                Direction.UP -> return Face.ONE to State22(
                    Position(0, cubeSize - 1 - state.pos.column),
                    Direction.DOWN
                )
            }

            Face.SIX -> when (state.dir) {
                Direction.RIGHT -> return Face.THREE to State22(
                    Position(cubeSize - 1, state.pos.row),
                    Direction.UP
                )

                Direction.DOWN -> return Face.FIVE to State22(
                    Position(cubeSize - 1, cubeSize - 1 - state.pos.column),
                    Direction.UP
                )

                Direction.LEFT -> return Face.FOUR to State22(
                    Position(cubeSize - 1, cubeSize - 1 - state.pos.row),
                    Direction.UP
                )

                Direction.UP -> return Face.TWO to State22(
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
                        val startState = State22(Position(row, column), direction)
                        val (nextFace, nextState) = positionInFrontOnCube(face to startState, cubeSize)
                        val turned = turn(turn(nextState, "L"), "L")
                        val (returnedFace, returnedState) = positionInFrontOnCube(nextFace to turned, cubeSize)
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

    fun simpleMoveForward(steps: Int, state: State22,
                        field: FlatRepresentation, positionInFront: (State22) -> State22): State22 {
        var current = state
        repeat(steps) {
            if (field[positionInFront(current).pos] == '.') {
                current = positionInFront(current)
            }
        }
        return current
    }

    fun getStartPosition(field: FlatRepresentation): Position {
        return Position(0, field[0].indexOf('.'))
    }

    fun normalizedField(input: List<String>): FlatRepresentation {
        val desiredLengh = input.maxOf { s -> s.length }
        return input.map { s -> s.padEnd(desiredLengh, ' ') }
    }

    class DummyField(override val flatMap: FlatRepresentation) : Field {
        override fun moveForward(state: State22, steps: Int): State22 {
            return simpleMoveForward(steps, state, flatMap) {x -> flatPositionInFront(x, flatMap)}
        }
    }

    // pos - top left corner of face on map
    // dir where top looks according to ideal cube
    data class Alignment(val pos: Position, val dir: Direction)

    fun coordinateIsInsideFace(pos: Position, alignment: Alignment, cubeSize: Int): Boolean {
        return (pos.column >= alignment.pos.column)
                && (pos.row >= alignment.pos.row)
                && (pos.column < alignment.pos.column + cubeSize)
                && (pos.row < alignment.pos.row + cubeSize)
    }

    fun cubeState(flatRelativePos: Position, flatDir: Direction, faceDir: Direction, cubeSize: Int): State22 {
        var cubeDir = flatDir
        var tmpFaceDir = faceDir
        while (tmpFaceDir != Direction.UP) {
            cubeDir = turnDir(cubeDir, "L")
            tmpFaceDir = turnDir(tmpFaceDir, "L")
        }
        val position = when(faceDir) {
            Direction.RIGHT -> Position(cubeSize - flatRelativePos.column - 1, flatRelativePos.row)
            Direction.DOWN -> Position(cubeSize - flatRelativePos.row - 1,
                cubeSize - flatRelativePos.column - 1)
            Direction.LEFT -> Position(flatRelativePos.column, cubeSize - flatRelativePos.row - 1)
            Direction.UP -> flatRelativePos
        }
        return State22(position, cubeDir)
    }

    class CubeField(val cubeSize: Int,
                    override val flatMap: FlatRepresentation,
                    val alignments: Map<Face, Alignment>) : Field {
        override fun moveForward(state: State22, steps: Int): State22 {
            return simpleMoveForward(steps, state, flatMap) {x ->
                convertCubeToFlat(positionInFrontOnCube(convertFlatToCube(x), cubeSize))
            }
        }
        fun convertFlatToCube(state: State22): Pair<Face, State22> {
            val face = Face.values().find { x -> coordinateIsInsideFace(state.pos, alignments[x]!!, cubeSize) }!!
            val relativePos = state.pos - alignments[face]!!.pos
            return face to cubeState(relativePos, state.dir, alignments[face]!!.dir, cubeSize)
        }
        fun convertCubeToFlat(cubeState: Pair<Face,State22>): State22 {
            val (face, state) = cubeState
            var faceDir = Direction.UP
            var flatDir = state.dir
            while(faceDir != alignments[face]!!.dir) {
                faceDir = turnDir(faceDir, "R")
                flatDir = turnDir(flatDir, "R")
            }
            val relativePos = when(faceDir) {
                Direction.RIGHT -> Position(state.pos.column, cubeSize - state.pos.row - 1)
                Direction.DOWN -> Position(cubeSize - state.pos.row - 1, cubeSize - state.pos.column - 1)
                Direction.LEFT -> Position(cubeSize - state.pos.column -1, state.pos.row)
                Direction.UP -> state.pos
            }
            return State22(alignments[face]!!.pos + relativePos, flatDir)
        }
    }

    fun createDummyField(input: List<String>): DummyField {
        return DummyField(normalizedField(input))
    }

    fun solve(input: List<String>, createField: (List<String>)->Field): Int {
        val field = createField(input.dropLast(2))
        val commandString = input.last()
        val commandList = commandString
            .replace(Regex("""\p{Upper}""")) { x: MatchResult -> " ${x.value} " }
            .split(" ").filter { s -> s != "" }
        var state = State22(getStartPosition(field.flatMap), Direction.RIGHT)
        for (command in commandList) {
            state = if (command == "R" || command == "L") {
                turn(state, command)
            } else {
                field.moveForward(state, command.toInt())
            }
        }
        println(state)
        return state.password()
    }

    fun createTestCubeField(input: List<String>): Field {
        return CubeField(4, normalizedField(input), mapOf(
            Face.ONE to Alignment(Position(0, 8), Direction.UP),
            Face.TWO to Alignment(Position(4, 8), Direction.UP),
            Face.THREE to Alignment(Position(8, 12), Direction.RIGHT),
            Face.FOUR to Alignment(Position(4, 4), Direction.UP),
            Face.FIVE to Alignment(Position(4, 0), Direction.UP),
            Face.SIX to Alignment(Position(8, 8), Direction.UP)
        ))
    }

    fun createRealCubeField(input: List<String>): CubeField {
        return CubeField(50, normalizedField(input), mapOf(
            Face.ONE to Alignment(Position(0, 50), Direction.UP),
            Face.TWO to Alignment(Position(50, 50), Direction.UP),
            Face.THREE to Alignment(Position(0, 100), Direction.LEFT),
            Face.FOUR to Alignment(Position(100, 0), Direction.LEFT),
            Face.FIVE to Alignment(Position(150, 0), Direction.LEFT),
            Face.SIX to Alignment(Position(100, 50), Direction.UP)
        ))
    }

    fun testRealTransformation(input: List<String>) {
        val field = createRealCubeField(input)
        for (row in 0 until field.flatMap.rows()) {
            for (column in 0 until field.flatMap.columns()) {
                val pos = Position(row, column)
                if (field.flatMap[pos] != ' ') {
                    for (dir in Direction.values()) {
                        val state = State22(pos, dir)
                        val cubeState = field.convertFlatToCube(state)
                        val reversedState = field.convertCubeToFlat(cubeState)
                        if (state != reversedState) {
                            println("Non consistent conversion")
                            println("$state != $reversedState")
                            println("interim cube state $cubeState")
                            throw Exception()
                        }
                    }
                }
            }
        }
    }

    println("Day22")
    val testInput = readInput("Day22-test")
    val input = readInput("Day22")

    testPositionInFrontOnCube()
    testRealTransformation(input.dropLast(2))
    println("part1 test ${solve(testInput, ::createDummyField)}")
    println("part1 real ${solve(input, ::createDummyField)}")
    println("part2 test ${solve(testInput, ::createTestCubeField)}")
    println("part2 real ${solve(input, ::createRealCubeField)}")
}