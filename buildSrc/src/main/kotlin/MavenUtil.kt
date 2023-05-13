import java.net.URL
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

internal object MavenUtil {
    private const val MAVEN_APACHE_URL = "http://maven.apache.org"
    private val dateTimeFormatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmss")

    fun pom(
        modelVersion: String = "4.0.0",
        groupId: String,
        artifactId: String,
        version: String,
        packaging: String,
    ): String {
        val pomUrl = "$MAVEN_APACHE_URL/POM/$modelVersion"
        val project = setOf(
            "xsi:schemaLocation" to "$pomUrl $MAVEN_APACHE_URL/xsd/maven-$modelVersion.xsd",
            "xmlns" to pomUrl,
            "xmlns:xsi" to "http://www.w3.org/2001/XMLSchema-instance",
        ).joinToString(separator = " ") { (key, value) ->
            "$key=\"$value\""
        }
        return setOf(
            "modelVersion" to modelVersion,
            "groupId" to groupId,
            "artifactId" to artifactId,
            "version" to version,
            "packaging" to packaging,
        ).joinToString(
            prefix = "<project $project>",
            separator = "",
            postfix = "</project>",
        ) { (key, value) ->
            "<$key>$value</$key>"
        }
    }

    fun metadata(
        groupId: String,
        artifactId: String,
        version: String,
        dateTime: LocalDateTime = LocalDateTime.now(),
    ): String {
        return """
            <metadata>
                <groupId>$groupId</groupId>
                <artifactId>$artifactId</artifactId>
                <versioning>
                    <versions>
                        <version>$version</version>
                    </versions>
                    <lastUpdated>${dateTimeFormatter.format(dateTime)}</lastUpdated>
                </versioning>
            </metadata>
            """.trimIndent()
    }

    object Snapshot {
        fun url(
            maven: Maven,
            version: String,
        ): URL {
            val host = "https://s01.oss.sonatype.org"
            val path = "$host/content/repositories/snapshots"
            val spec = "$path/${maven.groupId.replace('.', '/')}/${maven.artifactId}/$version"
            return URL(spec)
        }
    }
}
