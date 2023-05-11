fun camelCase(segment: String, vararg other: String): String {
    check(segment.isNotEmpty())
    val builder = StringBuilder(segment)
    for (it in other) {
        if (it.isNotEmpty()) {
            builder.append(it.capitalize())
        }
    }
    return builder.toString()
}

fun kebabCase(segment: String, vararg other: String): String {
    check(segment.isNotEmpty())
    val builder = StringBuilder(segment)
    for (it in other) {
        if (it.isNotEmpty()) {
            builder.append("-")
                .append(it)
        }
    }
    return builder.toString()
}