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

fun String.join(vararg postfix: String): String {
    check(isNotEmpty())
    return postfix.filter { it.isNotEmpty() }.joinToString(separator = "", prefix = this) {
        it.capitalize()
    }
}

fun getVersionName(variant: com.android.build.gradle.api.BaseVariant): String {
    return when (variant.buildType.name) {
        "release" -> "${Version.Application.name}-${variant.flavorName}"
        else -> "${Version.Application.name}-${variant.name}"
    }
}

fun getVersion(variant: com.android.build.gradle.api.BaseVariant): String {
    return "${getVersionName(variant)}-${Version.Application.code}"
}

fun getOutputFileName(variant: com.android.build.gradle.api.BaseVariant, extension: String): String {
    check(extension.isNotEmpty())
    return "${rootProject.name}-${getVersion(variant)}.$extension"
}

jacoco {
    toolVersion = Version.jacoco
}

fun setCoverage(variant: com.android.build.gradle.api.BaseVariant) {
    val taskUnitTest = tasks.getByName<Test>("test".join(variant.name, "UnitTest"))
    val taskCoverageReport = task<JacocoReport>("test".join(variant.name, "CoverageReport")) {
        dependsOn(taskUnitTest)
        reports {
            csv.required.set(false)
            html.required.set(true)
            xml.required.set(false)
        }
        sourceDirectories.setFrom(file("src/main/kotlin"))
        val dirs = fileTree(File(buildDir, "tmp/kotlin-classes/" + variant.name))
        classDirectories.setFrom(dirs)
//        executionData(taskUnitTest)
        executionData(File(buildDir, "outputs/unit_test_code_coverage/${variant.name}UnitTest/${taskUnitTest.name}.exec"))
    }
    task<JacocoCoverageVerification>("test".join(variant.name, "CoverageVerification")) {
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

fun setCodeQuality(variant: com.android.build.gradle.api.BaseVariant) {
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
        File(rootDir, "buildSrc/src/main/resources/detekt/config/$config.yml").also {
            check(it.exists() && !it.isDirectory)
        }
    }
    setOf("main", "test").forEach { source ->
        task<io.gitlab.arturbosch.detekt.Detekt>("check".join(variant.name, "CodeQuality", source)) {
            jvmTarget = Version.jvmTarget
            setSource(files("src/$source/kotlin"))
            config.setFrom(configs)
            reports {
                html {
                    required.set(true)
                    outputLocation.set(File(buildDir, "reports/analysis/code/quality/${variant.name}/$source/html/index.html"))
                }
                md.required.set(false)
                sarif.required.set(false)
                txt.required.set(false)
                xml.required.set(false)
            }
            val postfix = when (source) {
                "main" -> ""
                "test" -> "UnitTest"
                else -> error("Source \"$source\" is not supported!")
            }
            val detektTask = tasks.getByName<io.gitlab.arturbosch.detekt.Detekt>("detekt".join(variant.name, postfix))
            classpath.setFrom(detektTask.classpath)
        }
    }
}

fun checkDocumentation(variant: com.android.build.gradle.api.BaseVariant) {
    val configs = setOf(
        "common",
        "documentation",
    ).map { config ->
        File(rootDir, "buildSrc/src/main/resources/detekt/config/$config.yml").also {
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
                outputLocation.set(File(buildDir, "reports/analysis/documentation/${variant.name}/html/index.html"))
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

fun assembleDocumentation(variant: com.android.build.gradle.api.BaseVariant) {
    task<org.jetbrains.dokka.gradle.DokkaTask>("assemble".join(variant.name, "Documentation")) {
        outputDirectory.set(buildDir.resolve("documentation/${variant.name}"))
        moduleName.set(Repository.name)
        moduleVersion.set(getVersion(variant))
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
        output.outputFileName = getOutputFileName(variant, "aar")
        afterEvaluate {
            setCoverage(variant)
            setCodeQuality(variant)
            checkDocumentation(variant)
            assembleDocumentation(variant)
            tasks.getByName<JavaCompile>("compile".join(variant.name, "JavaWithJavac")) {
                targetCompatibility = Version.jvmTarget
            }
            tasks.getByName<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>("compile".join(variant.name, "Kotlin")) {
                kotlinOptions.jvmTarget = Version.jvmTarget
            }
            task("assemble".join(variant.name, "Pom")) {
                doLast {
                    val target = File(buildDir, "libs").let {
                        it.mkdirs()
                        File(it, getOutputFileName(variant, "pom"))
                    }
                    if (target.exists()) target.delete()
                    val text = MavenUtil.pom(
                        groupId = Maven.groupId,
                        artifactId = Maven.artifactId,
                        version = getVersion(variant),
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
