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
