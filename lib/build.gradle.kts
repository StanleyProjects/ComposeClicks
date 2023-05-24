import com.android.build.gradle.api.BaseVariant
import sp.gx.core.Badge
import sp.gx.core.GitHub
import sp.gx.core.Markdown
import sp.gx.core.Maven
import sp.gx.core.camelCase
import sp.gx.core.existing
import sp.gx.core.file
import sp.gx.core.filled
import sp.gx.core.kebabCase
import sp.gx.core.resolve
import sp.gx.core.check

version = "0.2.0"

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
    check(flavorName.isEmpty())
    return when (buildType.name) {
        "debug" -> kebabCase(version.toString(), "SNAPSHOT")
        "release" -> version.toString()
        else -> error("Build type \"${buildType.name}\" is not supported!")
    }
}

fun BaseVariant.getOutputFileName(extension: String): String {
    check(extension.isNotEmpty())
    return "${kebabCase(rootProject.name, getVersion())}.$extension"
}

jacoco.toolVersion = Version.jacoco

fun checkCoverage(variant: BaseVariant) {
    val taskUnitTest = tasks.getByName<Test>(camelCase("test", variant.name, "UnitTest"))
    val taskCoverageReport = task<JacocoReport>(camelCase("assemble", variant.name, "CoverageReport")) {
        dependsOn(taskUnitTest)
        reports {
            csv.required.set(false)
            html.required.set(true)
            xml.required.set(false)
        }
        sourceDirectories.setFrom(file("src/main/kotlin"))
        val dirs = fileTree(buildDir.resolve("tmp/kotlin-classes").resolve(variant.name))
        classDirectories.setFrom(dirs)
        executionData(buildDir.resolve("outputs/unit_test_code_coverage/${variant.name}UnitTest/${taskUnitTest.name}.exec"))
        doLast {
            val report = buildDir.resolve("reports/jacoco/$name/html/index.html")
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
                    val test = rootDir.resolve("buildSrc/src/main/resources/detekt/config/android/test.yml")
                        .existing()
                        .file()
                        .filled()
                    config.setFrom(configs + test)
                }
                else -> error("Type \"$type\" is not supported!")
            }
            reports {
                html {
                    required.set(true)
                    outputLocation.set(buildDir.resolve("reports/analysis/code/quality/${variant.name}/$type/html/index.html"))
                }
                md.required.set(false)
                sarif.required.set(false)
                txt.required.set(false)
                xml.required.set(false)
            }
            val detektTask = tasks.getByName<io.gitlab.arturbosch.detekt.Detekt>(camelCase("detekt", variant.name, postfix))
            classpath.setFrom(detektTask.classpath)
        }
    }
}

fun BaseVariant.checkDocumentation() {
    val variant = this
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
        reports {
            html {
                required.set(true)
                outputLocation.set(buildDir.resolve("reports/analysis/documentation/${variant.name}/html/index.html"))
            }
            md.required.set(false)
            sarif.required.set(false)
            txt.required.set(false)
            xml.required.set(false)
        }
        val detektTask = tasks.getByName<io.gitlab.arturbosch.detekt.Detekt>(camelCase("detekt", variant.name))
        classpath.setFrom(detektTask.classpath)
    }
}

fun BaseVariant.assembleDocumentation() {
    val variant = this
    task<org.jetbrains.dokka.gradle.DokkaTask>(camelCase("assemble", variant.name, "Documentation")) {
        outputDirectory.set(buildDir.resolve("documentation/${variant.name}"))
        moduleName.set(gh.name)
        moduleVersion.set(getVersion())
        dokkaSourceSets.create(camelCase(variant.name, "main")) {
            reportUndocumented.set(false)
            sourceLink {
                val path = "src/main/kotlin"
                localDirectory.set(file(path))
                val url = GitHub.url(gh.owner, gh.name)
                remoteUrl.set(url.resolve("tree/${moduleVersion.get()}/lib/$path"))
            }
            jdkVersion.set(Version.jvmTarget.toInt())
        }
    }
}

fun BaseVariant.assemblePom() {
    task(camelCase("assemble", name, "Pom")) {
        doLast {
            val target = buildDir.resolve("libs/${getOutputFileName("pom")}")
            if (target.exists()) {
                check(target.isFile)
                check(target.delete())
            } else {
                target.parentFile?.mkdirs()
            }
            val text = Maven.pom(
                groupId = maven.group,
                artifactId = maven.id,
                version = getVersion(),
                packaging = "aar",
            )
            target.writeText(text)
        }
    }
}

fun BaseVariant.assembleMetadata() {
    task(camelCase("assemble", name, "Metadata")) {
        doLast {
            val target = buildDir.resolve("yml/metadata.yml")
            if (target.exists()) {
                check(target.isFile)
                check(target.delete())
            } else {
                target.parentFile?.mkdirs()
            }
            val text = """
                repository:
                 owner: '${gh.owner}'
                 name: '${gh.name}'
                version: '${getVersion()}'
            """.trimIndent()
            target.writeText(text)
        }
    }
}

fun BaseVariant.assembleMavenMetadata() {
    task(camelCase("assemble", name, "MavenMetadata")) {
        doLast {
            val target = buildDir.resolve("xml/maven-metadata.xml")
            if (target.exists()) {
                check(target.isFile)
                check(target.delete())
            } else {
                target.parentFile?.mkdirs()
            }
            val text = Maven.metadata(
                groupId = maven.group,
                artifactId = maven.id,
                version = getVersion(),
            )
            target.writeText(text)
        }
    }
}

fun BaseVariant.checkReadme() {
    task(camelCase("check", name, "Readme")) {
        doLast {
            val badge = Markdown.image(
                text = "version",
                url = Badge.url(
                    label = "version",
                    message = getVersion(),
                    color = "2962ff",
                ),
            )
            val expected = setOf(
                badge,
                Markdown.link("Maven", Maven.Snapshot.url(maven.group, maven.id, getVersion())),
                Markdown.link("Documentation", GitHub.pages(gh.owner, gh.name).resolve("doc").resolve(getVersion())),
                "implementation(\"${maven.group}:${maven.id}:${getVersion()}\")",
            )
            rootDir.resolve("README.md").check(
                expected = expected,
                report = buildDir.resolve("reports/analysis/readme/$name/index.html"),
            )
        }
    }
}

fun BaseVariant.assembleSource() {
    val variant = this
    task<Jar>(camelCase("assemble", name, "Source")) {
        archiveBaseName.set(maven.id)
        archiveVersion.set(variant.getVersion())
        archiveClassifier.set("sources")
        val sourceSets = variant.sourceSets.flatMap { it.kotlinDirectories }.distinctBy { it.absolutePath }
        from(sourceSets)
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
        manifestPlaceholders["appName"] = "@string/app_name"
    }

    buildTypes.getByName(testBuildType) {
        isTestCoverageEnabled = true
    }

    buildFeatures.compose = true

    composeOptions.kotlinCompilerExtensionVersion = Version.Android.compose

    libraryVariants.all {
        val variant = this
        val output = variant.outputs.single()
        check(output is com.android.build.gradle.internal.api.LibraryVariantOutputImpl)
        output.outputFileName = getOutputFileName("aar")
        checkReadme()
        if (buildType.name == testBuildType) {
            checkCoverage(variant)
        }
        checkCodeQuality(variant)
        checkDocumentation()
        assembleDocumentation()
        assemblePom()
        assembleSource()
        assembleMetadata()
        assembleMavenMetadata()
        afterEvaluate {
            tasks.getByName<JavaCompile>(camelCase("compile", variant.name, "JavaWithJavac")) {
                targetCompatibility = Version.jvmTarget
            }
            tasks.getByName<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>(camelCase("compile", variant.name, "Kotlin")) {
                kotlinOptions.jvmTarget = Version.jvmTarget
            }
        }
    }
}

dependencies {
    implementation("androidx.compose.foundation:foundation:${Version.Android.compose}")
    testImplementation("org.robolectric:robolectric:4.10")
    testImplementation("androidx.compose.ui:ui-test-junit4:${Version.Android.compose}")
    camelCase("test", android.testBuildType, "Implementation")("androidx.compose.ui:ui-test-manifest:${Version.Android.compose}")
}
