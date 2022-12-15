import kotlin.math.abs

fun main() {
    class Position(val x: Long, val y: Long) {
        fun distance(other: Position): Long {
            return abs(x - other.x) + abs(y - other.y)
        }
    }

    class Sensor(val pos: Position, val beacon: Position) {
        fun radius(): Long {
            return pos.distance(beacon)
        }
    }

    fun readSensor(input: String): Sensor {
        val regex = Regex("""Sensor at x=(.\d*), y=(.\d*): closest beacon is at x=(.\d*), y=(.\d*)""")
        val match = regex.matchEntire(input)
        val (xs, ys, xb, yb) = match!!.groups.drop(1).map { x -> x!!.value.toLong() }
        return Sensor(Position(xs, ys), Position(xb, yb))
    }

    class CoverageSegment(val start: Long, val end: Long)

    class Event(val x: Long, val type: Int)

    fun getCoverage(sensor: Position, distance: Long, y: Long): CoverageSegment? {
        if (abs(sensor.y - y) > distance) {
            return null
        }
        return CoverageSegment(
            sensor.x - (distance - abs(sensor.y - y)), sensor.x + (distance - abs(sensor.y - y)) + 1
        )
    }

    fun part1(input: List<String>, y: Long): Long {
        val sensors = input.map(::readSensor)
        val coverage = sensors.map { sensor -> getCoverage(sensor.pos, sensor.radius(), y) }
        val left = coverage.minOf { segment -> segment?.start ?: 0 }
        val right = coverage.maxOf { segment -> segment?.end ?: 0 }
        var ans = 0L
        for (x in left..right) {
            if (sensors.any { sensor -> sensor.beacon.distance(Position(x, y)) == 0L }) {
                continue
            }
            if (sensors.any { sensor -> sensor.pos.distance(Position(x, y)) <= sensor.radius() }) {
                ans += 1
            }
        }
        return ans
    }

    fun part2(input: List<String>, maxCoordinate: Long): Position {
        val sensors = input.map(::readSensor)
        for (y in 0..maxCoordinate) {
            val coverage = sensors.map { sensor -> getCoverage(sensor.pos, sensor.radius(), y) }
            val events = coverage.flatMap { s ->
                if (s != null) {
                    listOf(Event(s.start, 1), Event(s.end, -1))
                } else {
                    listOf()
                }
            }
            val sortedEvents = events.sortedWith(compareBy<Event> { e -> e.x }.thenBy { e -> -e.type })
            var balance = 0
            for (e in sortedEvents) {
                balance += e.type
                if ((balance == 0) && (e.x <= maxCoordinate) && sensors.all { sensor ->
                        sensor.beacon.distance(
                            Position(
                                e.x,
                                y
                            )
                        ) != 0L
                    }) {
                    return Position(e.x, y)
                }
            }
        }
        return Position(-1, -1)
    }

    val testInput = readInput("Day15-test")
    val input = readInput("Day15")
    println("Day 15")
    println("test part1 ${part1(testInput, 10)}")
    println("real part1 ${part1(input, 2000000)}")
    val testHiddenBeacon = part2(testInput, 20)
    println("test part2 ${testHiddenBeacon.x} ${testHiddenBeacon.y} -> ${testHiddenBeacon.x * 4000000 + testHiddenBeacon.y}")
    val hiddenBeacon = part2(input, 4000000)
    println("test part2 ${hiddenBeacon.x} ${hiddenBeacon.y} -> ${hiddenBeacon.x * 4000000 + hiddenBeacon.y}")
}