import java.net.URL
import com.android.build.gradle.api.BaseVariant

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

fun String.join(vararg postfix: String): String {
    check(isNotEmpty())
    return postfix.filter { it.isNotEmpty() }.joinToString(separator = "", prefix = this) {
        it.capitalize()
    }
}

fun BaseVariant.getVersionName(): String {
    return when (buildType.name) {
        "release" -> "${Version.Application.name}-$flavorName"
        else -> "${Version.Application.name}-$name"
    }
}

fun BaseVariant.getVersion(): String {
    return "${getVersionName()}-${Version.Application.code}"
}

fun BaseVariant.getOutputFileName(extension: String): String {
    check(extension.isNotEmpty())
    return "${rootProject.name}-${getVersion()}.$extension"
}

jacoco {
    toolVersion = Version.jacoco
}

fun BaseVariant.checkCoverage() {
    val variant = this
    val taskUnitTest = tasks.getByName<Test>("test".join(variant.name, "UnitTest"))
    val taskCoverageReport = task<JacocoReport>("assemble".join(variant.name, "CoverageReport")) {
        dependsOn(taskUnitTest)
        reports {
            csv.required.set(false)
            html.required.set(true)
            xml.required.set(false)
        }
        sourceDirectories.setFrom(file("src/main/kotlin"))
        val dirs = fileTree(buildDir.resolve("tmp/kotlin-classes/" + variant.name))
        classDirectories.setFrom(dirs)
        executionData(buildDir.resolve("outputs/unit_test_code_coverage/${variant.name}UnitTest/${taskUnitTest.name}.exec"))
    }
    task<JacocoCoverageVerification>("check".join(variant.name, "Coverage")) {
        dependsOn(taskCoverageReport)
        violationRules {
            rule {
                limit {
                    minimum = BigDecimal(0.96)
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
        rootDir.resolve("buildSrc/src/main/resources/detekt/config/$config.yml").also {
            check(it.exists() && !it.isDirectory)
        }
    }
    setOf(
        Triple("main", variant.sourceSets.flatMap { it.kotlinDirectories }, ""),
        Triple("test", setOf(file("src/test/kotlin")), "UnitTest"),
    ).forEach { (type, sources, postfix) ->
        task<io.gitlab.arturbosch.detekt.Detekt>("check".join(variant.name, "CodeQuality", postfix)) {
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
            val detektTask = tasks.getByName<io.gitlab.arturbosch.detekt.Detekt>("detekt".join(variant.name, postfix))
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
        rootDir.resolve("buildSrc/src/main/resources/detekt/config/$config.yml").also {
            check(it.exists() && !it.isDirectory)
        }
    }
    task<io.gitlab.arturbosch.detekt.Detekt>("check".join(variant.name, "Documentation")) {
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
        val detektTask = tasks.getByName<io.gitlab.arturbosch.detekt.Detekt>("detekt".join(variant.name))
        classpath.setFrom(detektTask.classpath)
    }
}

fun BaseVariant.assembleDocumentation() {
    val variant = this
    task<org.jetbrains.dokka.gradle.DokkaTask>("assemble".join(variant.name, "Documentation")) {
        outputDirectory.set(buildDir.resolve("documentation/${variant.name}"))
        moduleName.set(Repository.name)
        moduleVersion.set(getVersion())
        dokkaSourceSets {
            create(variant.name.join("main")) {
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
//        enableUnitTestCoverage = true
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
        afterEvaluate {
            checkCoverage()
            checkCodeQuality()
            checkDocumentation()
            assembleDocumentation()
            tasks.getByName<JavaCompile>("compile".join(variant.name, "JavaWithJavac")) {
                targetCompatibility = Version.jvmTarget
            }
            tasks.getByName<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>("compile".join(variant.name, "Kotlin")) {
                kotlinOptions.jvmTarget = Version.jvmTarget
            }
            task("assemble".join(variant.name, "Pom")) {
                doLast {
                    val target = buildDir.resolve("libs").let {
                        it.mkdirs()
                        it.resolve(getOutputFileName("pom"))
                    }
                    if (target.exists()) target.delete()
                    val text = MavenUtil.pom(
                        groupId = Maven.groupId,
                        artifactId = Maven.artifactId,
                        version = variant.getVersion(),
                        packaging = "aar"
                    )
                    target.writeText(text)
                }
            }
        }
    }
}

dependencies {
    implementation("androidx.compose.foundation:foundation:${Version.Android.compose}")
    testImplementation("org.robolectric:robolectric:4.10")
    testImplementation("androidx.compose.ui:ui-test-junit4:${Version.Android.compose}")
    "test".join(android.testBuildType, "Implementation")("androidx.compose.ui:ui-test-manifest:${Version.Android.compose}")
}
