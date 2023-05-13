import java.io.File

fun File.existing(): File {
    check(exists())
    return this
}

fun File.file(): File {
    check(isFile)
    return this
}

fun File.filled(): File {
    check(length() > 0)
    return this
}

fun File.check(expected: Set<String>, report: File) {
    val issues = when {
        !exists() -> setOf("the file does not exist")
        isDirectory -> setOf("the file is a directory")
        else -> {
            val actual = readLines(Charsets.UTF_8)
            when {
                actual.isEmpty() -> setOf("the file does not contain text")
                else -> {
                    expected.mapNotNull { line ->
                        "the file does not contain \"$line\" line".takeIf { _ ->
                            actual.none { it.contains(line) }
                        }
                    }.toSet()
                }
            }
        }
    }
    when {
        report.exists() -> {
            check(report.isFile) { "report is not a file" }
            check(report.delete())
        }
        else -> report.parentFile?.mkdirs() ?: error("report has no parent")
    }
    if (issues.isEmpty()) {
        val message = "All checks of the file along the \"$name\" were successful."
        report.writeText(message)
        println(message)
        return
    }
    val text = """
        <html>
        <h3>The following problems were found while checking the <code>$name</code>:</h3>
        ${issues.joinToString(prefix = "<ul>", postfix = "</ul>", separator = "\n") { "<li>$it</li>" }}
        </html>
    """.trimIndent()
    report.writeText(text)
    error("Problems were found while checking the \"$name\". See the report ${report.absolutePath}")
}
