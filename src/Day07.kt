class FileTree(val root: Directory, private val position: MutableList<Directory>) {
    var answerPart1 = 0
    var answerPart2 = 70000000

    companion object Factory {
        fun create(input: List<String>): FileTree {
            val root = Directory("/")
            val fileTree = FileTree(root, mutableListOf(root))
            var cmd = mutableListOf<String>()
            for (line in input) {
                if (line[0] == '$' && cmd.isNotEmpty()) {
                    fileTree.execute(cmd)
                    cmd = mutableListOf()
                }
                cmd.add(line)
            }
            fileTree.execute(cmd)
            return fileTree
        }
    }

    class File(@Suppress("unused") val name: String, val size: Int)
    class Directory(
        val name: String,
        val files: MutableList<File> = mutableListOf(),
        val dirs: MutableList<Directory> = mutableListOf()
    )

    private fun execute(cmd: List<String>) {
        if (cmd[0][2] == 'l') {
            create(cmd.drop(1))
        } else {
            cd(cmd[0].split(" ")[2])
        }
    }

    private fun cd(direction: String) {
        when (direction) {
            "/" -> {
                while (position.size > 1) {
                    position.removeLast()
                }
            }

            ".." -> {
                assert(position.size > 1)
                position.removeLast()
            }

            else -> {
                position.add(position.last().dirs.find { x -> x.name == direction }!!)
            }
        }
    }

    private fun create(lsData: List<String>) {
        val cur = position.last()
        assert(cur.dirs.isEmpty() && cur.files.isEmpty())
        for (entry in lsData) {
            val (meta, name) = entry.split(" ")
            if (meta == "dir") {
                cur.dirs.add(Directory(name))
            } else {
                cur.files.add(File(name, meta.toInt()))
            }
        }
    }

    fun calculateSizes(dir: Directory, updateAnswer: (Int, FileTree) -> Unit): Int {
        var size = 0
        for (child in dir.dirs) {
            size += calculateSizes(child, updateAnswer)
        }
        size += dir.files.sumOf { f -> f.size }
        updateAnswer(size, this)
        return size
    }
}

fun updateAnswerPart1(size: Int, fileTree: FileTree) {
    if (size <= 100000) {
        fileTree.answerPart1 += size
    }
}

fun main() {
    println("Day 07")
    val input = readInput("Day07")
    val fileTree = FileTree.create(input)
    val occupiedSpace = fileTree.calculateSizes(fileTree.root, ::updateAnswerPart1)
    println(fileTree.answerPart1)
    val diskSpace = 70000000
    val requiredSpace = 30000000
    val freeSpace = diskSpace - occupiedSpace
    val spaceToClean = requiredSpace - freeSpace
    fileTree.calculateSizes(fileTree.root) { size, tree ->
        if (size >= spaceToClean && size < tree.answerPart2) tree.answerPart2 = size
    }
    println(fileTree.answerPart2)
}