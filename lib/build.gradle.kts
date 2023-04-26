repositories {
    google()
    mavenCentral()
}

plugins {
    id("com.android.library")
    id("kotlin-android")
    id("org.gradle.jacoco")
}

fun getVersionName(variant: com.android.build.gradle.api.LibraryVariant): String {
    return when (variant.buildType.name) {
        "release" -> "${Version.Application.name}-${variant.flavorName}"
        else -> "${Version.Application.name}-${variant.name}"
    }
}

fun getVersion(variant: com.android.build.gradle.api.LibraryVariant): String {
    return "${getVersionName(variant)}-${Version.Application.code}"
}

fun getOutputFileName(variant: com.android.build.gradle.api.LibraryVariant, extension: String): String {
    check(extension.isNotEmpty())
    return "${rootProject.name}-${getVersion(variant)}.$extension"
}

jacoco {
    toolVersion = Version.jacoco
}

fun setCoverage(variant: com.android.build.gradle.api.LibraryVariant) {
    val capitalize = variant.name.capitalize()
    val taskUnitTest = tasks.getByName<Test>("test${capitalize}UnitTest")
    val taskCoverageReport = task<JacocoReport>("test${capitalize}CoverageReport") {
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
        executionData("${buildDir}/outputs/unit_test_code_coverage/${variant.name}UnitTest/${taskUnitTest.name}.exec")
    }
    task<JacocoCoverageVerification>("test${capitalize}CoverageVerification") {
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
            tasks.getByName<JavaCompile>("compile${variant.name.capitalize()}JavaWithJavac") {
                targetCompatibility = Version.jvmTarget
            }
            tasks.getByName<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>("compile${variant.name.capitalize()}Kotlin") {
                kotlinOptions.jvmTarget = Version.jvmTarget
            }
            task("assemble${variant.name.capitalize()}Pom") {
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
    "test${android.testBuildType.capitalize()}Implementation"("androidx.compose.ui:ui-test-manifest:${Version.Android.compose}")
}
