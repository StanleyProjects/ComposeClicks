import com.android.build.gradle.api.BaseVariant
import java.net.URL

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

fun BaseVariant.getVersionName(): String {
    return when (buildType.name) {
        "release" -> {
            when (flavorName) {
                "snapshot" -> kebabCase(Version.Application.name, flavorName.toUpperCase())
                else -> error("Flavor \"$flavorName\" is not supported!")
            }
        }
        "debug" -> kebabCase(Version.Application.name, name)
        else -> error("Build type \"${buildType.name}\" is not supported!")
    }
}

fun BaseVariant.getVersion(): String {
    return kebabCase(getVersionName(), Version.Application.code.toString())
}

fun BaseVariant.getOutputFileName(extension: String): String {
    check(extension.isNotEmpty())
    return "${kebabCase(rootProject.name, getVersion())}.$extension"
}

jacoco {
    toolVersion = Version.jacoco
}

fun BaseVariant.checkCoverage() {
    val variant = this
    val taskUnitTest = tasks.getByName<Test>(camelCase("test", variant.name, "UnitTest"))
    val taskCoverageReport = task<JacocoReport>(camelCase("assemble", variant.name, "CoverageReport")) {
        dependsOn(taskUnitTest)
        reports {
            csv.required.set(false)
            html.required.set(true)
            xml.required.set(false)
        }
        sourceDirectories.setFrom(file("src/main/kotlin"))
        val dirs = fileTree(buildDir.resolve("tmp/kotlin-classes/${variant.name}"))
        classDirectories.setFrom(dirs)
        executionData(buildDir.resolve("outputs/unit_test_code_coverage/${variant.name}UnitTest/${taskUnitTest.name}.exec"))
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

fun BaseVariant.checkCodeQuality() {
    val variant = this
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
            config.setFrom(configs)
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
        moduleName.set(Repository.name)
        moduleVersion.set(getVersion())
        dokkaSourceSets {
            create("${variant.name}Main") {
                reportUndocumented.set(false)
                sourceLink {
                    val path = "src/main/kotlin"
                    localDirectory.set(file(path))
                    remoteUrl.set(URL("${Repository.url()}/tree/${moduleVersion.get()}/lib/$path"))
                }
                jdkVersion.set(Version.jvmTarget.toInt())
            }
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
            val text = MavenUtil.pom(
                groupId = Maven.groupId,
                artifactId = Maven.artifactId,
                version = getVersion(),
                packaging = "aar",
            )
            target.writeText(text)
        }
    }
}

fun BaseVariant.checkReadme() {
    task(camelCase("check", name, "Readme")) {
        doLast {
            val badge = MarkdownUtil.image(
                text = "version",
                url = BadgeUtil.url(
                    label = "version",
                    message = getVersion(),
                    color = "2962ff",
                ),
            )
            val expected = setOf(
                badge,
                MarkdownUtil.url("Maven", MavenUtil.Snapshot.url(Maven, getVersion())),
                MarkdownUtil.url("Documentation", GitHubUtil.pages(Repository.owner, Repository.name, "doc/${getVersion()}")),
                "implementation(\"${Maven.groupId}:${Maven.artifactId}:${getVersion()}\")",
            )
            rootDir.resolve("README.md").check(
                expected = expected,
                report = buildDir.resolve("reports/analysis/readme/$name/index.html"),
            )
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
        manifestPlaceholders["appName"] = "@string/app_name"
    }

    buildTypes.getByName(testBuildType) {
        isTestCoverageEnabled = true
    }

    productFlavors {
        mapOf("version" to setOf("snapshot")).forEach { (dimension, flavors) ->
            flavorDimensions += dimension
            flavors.forEach { flavor ->
                create(flavor) {
                    this.dimension = dimension
                }
            }
        }
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
            checkCoverage()
        }
        checkCodeQuality()
        checkDocumentation()
        assembleDocumentation()
        assemblePom()
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
