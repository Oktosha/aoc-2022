fun main() {
    data class Cube(val x: Int, val y: Int, val z: Int) {
        operator fun plus(other: Cube): Cube {
            return Cube(x + other.x, y + other.y, z + other.z)
        }

        fun neigbours(): List<Cube> {
            val diffs = listOf(
                Cube(1, 0, 0), Cube(-1, 0, 0), Cube(0, 1, 0),
                Cube(0, -1, 0), Cube(0, 0, 1), Cube(0, 0, -1)
            )
            return diffs.map { x -> x + this }
        }
    }

    fun parseCube(input: String): Cube {
        val (x, y, z) = input.split(",").map { x -> x.toInt() }
        return Cube(x, y, z)
    }

    fun part1(input: List<String>): Int {
        val cubes = HashSet(input.map { s -> parseCube(s) })
        var ans = cubes.size * 6
        for (cube in cubes) {
            for (neigbour in cube.neigbours()) {
                if (neigbour in cubes) {
                    ans -= 1
                }
            }
        }
        return ans
    }

    val testInput = readInput("Day18-test")
    val input = readInput("Day18")
    println("Day18")
    println("test part1: ${part1(testInput)}")
    println("real part1: ${part1(input)}")
}