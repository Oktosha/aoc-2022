enum class Material {
    WATER, LAVA
}

fun main() {
    data class Cube(val x: Int, val y: Int, val z: Int) {
        operator fun plus(other: Cube): Cube {
            return Cube(x + other.x, y + other.y, z + other.z)
        }

        operator fun minus(other: Cube): Cube {
            return Cube(x - other.x, y - other.y, z - other.z)
        }

        fun neigbours(): List<Cube> {
            val diffs = listOf(
                Cube(1, 0, 0), Cube(-1, 0, 0), Cube(0, 1, 0),
                Cube(0, -1, 0), Cube(0, 0, 1), Cube(0, 0, -1)
            )
            return diffs.map { x -> x + this }
        }

        fun volume(): Int {
            return x * y * z
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

    fun dfs(cube: Cube, world: MutableMap<Cube, Material>, start: Cube, end: Cube) {
        for (neigbour in cube.neigbours()) {
            if (!world.contains(neigbour)
                && neigbour.x >= start.x && neigbour.y >= start.y && neigbour.z >= start.z
                && neigbour.x <= end.x && neigbour.y <= end.y && neigbour.z <= end.z) {
                world[neigbour] = Material.WATER
                dfs(neigbour, world, start, end)
            }
        }
    }

    fun part2(input: List<String>): Int {
        val cubes = HashSet(input.map { s -> parseCube(s) })
        val start = Cube(cubes.minOf { cube -> cube.x } - 3,
            cubes.minOf { cube -> cube.y } - 3,
            cubes.minOf { cube -> cube.z } - 3)
        val end = Cube(cubes.maxOf { cube -> cube.x } + 3,
            cubes.maxOf { cube -> cube.y } + 3,
            cubes.maxOf { cube -> cube.z } + 3)
        val world = cubes.associateWith { _ -> Material.LAVA }.toMutableMap()
        world[start] = Material.WATER
        println("World Size: ${(end - start).volume()}")
        dfs(start, world, start, end)
        return cubes.sumOf { cube -> cube.neigbours().count { neigbour -> world[neigbour] == Material.WATER } }
    }

    val testInput = readInput("Day18-test")
    val input = readInput("Day18")
    println("Day18")
    println("test part1: ${part1(testInput)}")
    println("real part1: ${part1(input)}")
    println("test part2: ${part2(testInput)}")
    println("real part2: ${part2(input)}")
}