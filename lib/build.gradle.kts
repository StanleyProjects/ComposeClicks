import com.android.build.gradle.api.BaseVariant
import com.android.build.gradle.api.LibraryVariant
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import sp.gx.core.Badge
import sp.gx.core.GitHub
import sp.gx.core.Markdown
import sp.gx.core.Maven
import sp.gx.core.asFile
import sp.gx.core.assemble
import sp.gx.core.buildDir
import sp.gx.core.camelCase
import sp.gx.core.check
import sp.gx.core.colonCase
import sp.gx.core.create
import sp.gx.core.existing
import sp.gx.core.file
import sp.gx.core.filled
import sp.gx.core.kebabCase
import sp.gx.core.resolve
import sp.gx.core.task

version = "0.3.0"

val maven = Maven.Artifact(
    group = "com.github.kepocnhh",
    id = rootProject.name,
)

val gh = GitHub.Repository(
    owner = "StanleyProjects",
    name = rootProject.name,
)

repositories {
    google()
    mavenCentral()
}

plugins {
    id("com.android.library")
    id("kotlin-android")
    id("org.gradle.jacoco")
    id("io.gitlab.arturbosch.detekt") version Version.detekt
    id("org.jetbrains.dokka") version Version.dokka
}

fun BaseVariant.getVersion(): String {
    return when (flavorName) {
        "unstable" -> {
            when (buildType.name) {
                "debug" -> kebabCase("${version}u", "SNAPSHOT")
                else -> error("Build type \"${buildType.name}\" is not supported for flavor \"$flavorName\"!")
            }
        }
        else -> error("Flavor name \"$flavorName\" is not supported!")
    }
}

fun BaseVariant.getOutputFileName(extension: String): String {
    check(extension.isNotEmpty())
    return "${kebabCase(rootProject.name, getVersion())}.$extension"
}

jacoco.toolVersion = Version.jacoco

fun checkCoverage(variant: BaseVariant) {
    val taskUnitTest = camelCase("test", variant.name, "UnitTest")
    val executionData = layout.buildDirectory.get()
        .dir("outputs/unit_test_code_coverage/${variant.name}UnitTest")
        .file("$taskUnitTest.exec")
    tasks.getByName<Test>(taskUnitTest) {
        doLast {
            executionData.existing().file().filled()
        }
    }
    val taskCoverageReport = task<JacocoReport>(camelCase("assemble", variant.name, "CoverageReport")) {
        dependsOn(taskUnitTest)
        reports {
            csv.required = false
            html.required = true
            xml.required = false
        }
        sourceDirectories.setFrom(file("src/main/kotlin"))
        val dirs = layout.buildDirectory.get()
            .dir("tmp/kotlin-classes")
            .dir(variant.name)
            .let(::fileTree)
        classDirectories.setFrom(dirs)
        executionData(executionData)
        doLast {
            val report = layout.buildDirectory.get()
                .dir("reports/jacoco/$name/html")
                .file("index.html")
                .asFile
            if (report.exists()) {
                println("Coverage report: ${report.absolutePath}")
            }
        }
    }
    task<JacocoCoverageVerification>(camelCase("check", variant.name, "Coverage")) {
        dependsOn(taskCoverageReport)
        violationRules {
            rule {
                limit {
                    minimum = BigDecimal(0.9)
                }
            }
        }
        classDirectories.setFrom(taskCoverageReport.classDirectories)
        executionData(taskCoverageReport.executionData)
    }
}

fun checkCodeQuality(variant: BaseVariant) {
    val configs = setOf(
        "comments",
        "common",
        "complexity",
        "coroutines",
        "empty-blocks",
        "exceptions",
        "naming",
        "performance",
        "potential-bugs",
        "style",
    ).map { config ->
        rootDir.resolve("buildSrc/src/main/resources/detekt/config/$config.yml")
            .existing()
            .file()
            .filled()
    }
    setOf(
        Triple("main", variant.sourceSets.flatMap { it.kotlinDirectories }.distinctBy { it.absolutePath }, ""),
        Triple("test", files("src/test/kotlin"), "UnitTest"),
    ).forEach { (type, sources, postfix) ->
        task<io.gitlab.arturbosch.detekt.Detekt>(camelCase("check", variant.name, "CodeQuality", postfix)) {
            jvmTarget = Version.jvmTarget
            setSource(sources)
            when (type) {
                "main" -> config.setFrom(configs)
                "test" -> {
                    val tests = setOf(
                        "test",
                        "android/test",
                    ).map { config ->
                        rootDir.resolve("buildSrc/src/main/resources/detekt/config/$config.yml")
                            .existing()
                            .file()
                            .filled()
                    }
                    config.setFrom(configs + tests)
                }
                else -> error("Type \"$type\" is not supported!")
            }
            val report = layout.buildDirectory.get()
                .dir("reports/analysis/code/quality")
                .dir("${variant.name}/$type/html")
                .file("index.html")
                .asFile
            reports {
                html {
                    required = true
                    outputLocation = report
                }
                md.required = false
                sarif.required = false
                txt.required = false
                xml.required = false
            }
            val detektTask = tasks.getByName<io.gitlab.arturbosch.detekt.Detekt>(camelCase("detekt", variant.name, postfix))
            classpath.setFrom(detektTask.classpath)
            doFirst {
                println("Analysis report: ${report.absolutePath}")
            }
        }
    }
}

fun checkDocumentation(variant: BaseVariant) {
    val configs = setOf(
        "common",
        "documentation",
    ).map { config ->
        rootDir.resolve("buildSrc/src/main/resources/detekt/config/$config.yml")
            .existing()
            .file()
            .filled()
    }
    task<io.gitlab.arturbosch.detekt.Detekt>(camelCase("check", variant.name, "Documentation")) {
        jvmTarget = Version.jvmTarget
        setSource(files("src/main/kotlin"))
        config.setFrom(configs)
        val report = layout.buildDirectory.get()
            .dir("reports/analysis/documentation")
            .dir("${variant.name}/html")
            .file("index.html")
            .asFile
        reports {
            html {
                required = true
                outputLocation = report
            }
            md.required = false
            sarif.required = false
            txt.required = false
            xml.required = false
        }
        val detektTask = tasks.getByName<io.gitlab.arturbosch.detekt.Detekt>(camelCase("detekt", variant.name))
        classpath.setFrom(detektTask.classpath)
        doFirst {
            println("Analysis report: ${report.absolutePath}")
        }
    }
}

fun assembleDocumentation(variant: BaseVariant) {
    task<org.jetbrains.dokka.gradle.DokkaTask>(camelCase("assemble", variant.name, "Documentation")) {
        outputDirectory = layout.buildDirectory.dir("documentation/${variant.name}")
        moduleName = gh.name
        moduleVersion = variant.getVersion()
        dokkaSourceSets.create(camelCase(variant.name, "main")) {
            reportUndocumented = false
            sourceLink {
                val path = "src/main/kotlin"
                localDirectory = file(path)
                remoteUrl = gh.url().resolve("tree", moduleVersion.get(), "lib", path)
            }
            jdkVersion.set(Version.jvmTarget.toInt())
        }
        doLast {
            val index = outputDirectory.get()
                .file("index.html")
                .existing()
                .file()
                .filled()
            println("Documentation: ${index.absolutePath}")
        }
    }
}

//fun assembleMavenMetadata(variant: BaseVariant) {
//    task(camelCase("assemble", variant.name, "MavenMetadata")) {
//        doLast {
//            val file = layout.buildDirectory.get()
//                .dir("maven")
//                .dir(variant.name)
//                .file("maven-metadata.xml")
//                .assemble(
//                    Maven.metadata(
//                        artifact = maven,
//                        version = variant.getVersion(),
//                    ),
//                )
//            println("Maven metadata: ${file.absolutePath}")
//        }
//    }
//}

fun checkReadme(variant: BaseVariant) {
    tasks.create("check", variant.name, "Readme") {
        doLast {
            when (variant.name) {
                "unstableDebug" -> {
                    val badge = Markdown.image(
                        text = "version",
                        url = Badge.url(
                            label = "version",
                            message = variant.getVersion(),
                            color = "2962ff",
                        ),
                    )
                    val expected = setOf(
                        badge,
                        Markdown.link("Maven", Maven.Snapshot.url(maven, variant.getVersion())),
                        "implementation(\"${maven.moduleName(variant.getVersion())}\")",
                    )
                    val report = buildDir()
                        .dir("reports/analysis/readme")
                        .dir(variant.name)
                        .asFile("index.html")
                    rootDir.resolve("README.md").check(
                        expected = expected,
                        report = report,
                    )
                }
                else -> error("Variant \"${variant.name}\" is not supported!")
            }
        }
    }
}

fun assemblePom(variant: BaseVariant) {
    tasks.create("assemble", variant.name, "Pom") {
        doLast {
            val file = buildDir()
                .dir("xml")
                .dir(variant.name)
                .file("maven.pom.xml")
                .assemble(
                    maven.pom(
                        version = variant.getVersion(),
                        packaging = "aar",
                    ),
                )
            println("POM: ${file.absolutePath}")
        }
    }
}

fun assembleSource(variant: BaseVariant) {
    task<Jar>("assemble", variant.name, "Source") {
        val sourceSets = variant.sourceSets.flatMap { it.kotlinDirectories }.distinctBy { it.absolutePath }
        from(sourceSets)
        val dir = buildDir()
            .dir("sources")
            .asFile(variant.name)
        val file = File(dir, "${maven.name(variant.getVersion())}-sources.jar")
        outputs.upToDateWhen {
            file.exists()
        }
        doLast {
            dir.mkdirs()
            val renamed = archiveFile.get().asFile.existing().file().filled().renameTo(file)
            check(renamed)
            println("Archive: ${file.absolutePath}")
        }
    }
}

fun assembleMetadata(variant: BaseVariant) {
    task(camelCase("assemble", variant.name, "Metadata")) {
        doLast {
            val file = layout.buildDirectory.get()
                .dir("yml")
                .dir(variant.name)
                .file("metadata.yml")
                .assemble(
                    """
                        repository:
                         owner: '${gh.owner}'
                         name: '${gh.name}'
                        version: '${variant.getVersion()}'
                    """.trimIndent(),
                )
            println("Metadata: ${file.absolutePath}")
        }
    }
}

android {
    namespace = "sp.ax.jc.clicks"
    compileSdk = Version.Android.compileSdk

    testOptions {
        unitTests {
            isIncludeAndroidResources = true
            all {
                // https://stackoverflow.com/a/71834475/4398606
                it.configure<JacocoTaskExtension> {
                    isIncludeNoLocationClasses = true
                    excludes = listOf("jdk.internal.*")
                }
            }
        }
    }

    defaultConfig {
        minSdk = Version.Android.minSdk
    }

    productFlavors {
        mapOf(
            "stability" to setOf(
                "unstable",
            ),
        ).forEach { (dimension, flavors) ->
            flavorDimensions += dimension
            flavors.forEach { flavor ->
                create(flavor) {
                    this.dimension = dimension
                }
            }
        }
    }

    buildTypes.getByName(testBuildType) {
        isTestCoverageEnabled = true
    }

    buildFeatures.compose = true

    composeOptions.kotlinCompilerExtensionVersion = Version.Android.compose

    fun onVariant(variant: LibraryVariant) {
        val supported = setOf(
            "unstableDebug",
        )
        if (!supported.contains(variant.name)) {
            tasks.getByName(camelCase("pre", variant.name, "Build")) {
                doFirst {
                    error("Variant \"${variant.name}\" is not supported!")
                }
            }
            return
        }
        val output = variant.outputs.single()
        check(output is com.android.build.gradle.internal.api.LibraryVariantOutputImpl)
        output.outputFileName = variant.getOutputFileName("aar")
        checkReadme(variant)
        if (variant.buildType.name == testBuildType) {
            checkCoverage(variant)
        }
        checkCodeQuality(variant)
        checkDocumentation(variant)
        assembleDocumentation(variant)
        assemblePom(variant)
        assembleSource(variant)
        assembleMetadata(variant)
        afterEvaluate {
            tasks.getByName<JavaCompile>(camelCase("compile", variant.name, "JavaWithJavac")) {
                targetCompatibility = Version.jvmTarget
            }
            tasks.getByName<KotlinCompile>(camelCase("compile", variant.name, "Kotlin")) {
                kotlinOptions {
                    jvmTarget = Version.jvmTarget
                    freeCompilerArgs = freeCompilerArgs + setOf("-module-name", colonCase(maven.group, maven.id))
                }
            }
            tasks.getByName<JavaCompile>(camelCase("compile", variant.name, "UnitTestJavaWithJavac")) {
                targetCompatibility = Version.jvmTarget
            }
            tasks.getByName<KotlinCompile>(camelCase("compile", variant.name, "UnitTestKotlin")) {
                kotlinOptions.jvmTarget = Version.jvmTarget
            }
        }
    }

    libraryVariants.all {
        onVariant(this)
    }
}

dependencies {
    implementation("androidx.compose.foundation:foundation:${Version.Android.compose}")
    testImplementation("org.robolectric:robolectric:4.11")
    testImplementation("androidx.compose.ui:ui-test-junit4:${Version.Android.compose}")
    camelCase("test", android.testBuildType, "Implementation")("androidx.compose.ui:ui-test-manifest:${Version.Android.compose}")
}
