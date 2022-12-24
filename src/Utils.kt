import java.io.File

/**
 * Reads lines from the given input txt file.
 */
fun readInput(name: String) = File("src", "$name.txt")
    .readLines()

val pass: Unit = Unit

fun <E> Collection<E>.containsAny(elements: Collection<E>): Boolean {
    return elements.any { x -> this.contains(x) }
}