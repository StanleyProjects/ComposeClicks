import java.net.URL

object GitHubUtil {
    fun pages(
        owner: String,
        name: String,
        postfix: String,
    ): URL {
        check(postfix.isNotEmpty())
        return URL("https://$owner.github.io/$name/$postfix")
    }

    fun url(
        owner: String,
        name: String,
    ): URL {
        return URL("https://github.com/$owner/$name")
    }
}
