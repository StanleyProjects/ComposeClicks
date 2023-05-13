import java.net.URL

object BadgeUtil {
    fun url(
        label: String,
        message: String,
        labelColor: String = "212121",
        color: String,
        style: String = "flat",
    ): URL {
        val spec = "https://img.shields.io/static/v1?" + mapOf(
            "label" to label,
            "message" to message,
            "labelColor" to labelColor,
            "color" to color,
            "style" to style,
        ).toList().joinToString(separator = "&") { (key, value) ->
            "$key=$value"
        }
        return URL(spec)
    }
}
