fun main() {
    class Game(val player: Int, val opponent: Int)

    fun getMovesPart1(description: String): Game {
        val opponent = description[0].code - 'A'.code
        val player = description[2].code - 'X'.code
        return Game(player, opponent)
    }

    fun getGameScore(game: Game): Int {
        val moveScore = game.player + 1
        if ((game.opponent + 1) % 3 == game.player) {
            return moveScore + 6
        }
        if (game.opponent == game.player) {
            return moveScore + 3
        }
        return moveScore
    }

    fun part1EntryResolver(entry: String): Int {
        return getGameScore(getMovesPart1(entry))
    }

    fun part1(input: List<String>): Int {
        return input.map(::part1EntryResolver).sum()
    }

    fun getMovesPart2(description: String): Game {
        val opponent = description[0].code - 'A'.code
        val outcome = description[2].code - 'Y'.code
        val player = (3 + opponent + outcome) % 3
        return Game(player, opponent)
    }

    fun part2EntryResolver(entry: String): Int {
        return getGameScore(getMovesPart2(entry))
    }

    fun part2(input: List<String>): Int {
        return input.map(::part2EntryResolver).sum()
    }

    val input = readInput("Day02")
    println("Day 2")
    println(part1(input))
    println(part2(input))
}