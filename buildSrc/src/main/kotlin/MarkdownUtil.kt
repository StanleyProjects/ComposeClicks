import java.net.URL

object MarkdownUtil {
    fun url(
        text: String,
        value: URL,
    ): String {
        return "[$text]($value)"
    }

    fun image(
        text: String,
        url: URL,
    ): String {
        return "!" + url(text = text, value = url)
    }
}
