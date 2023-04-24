repositories {
    google()
    mavenCentral()
}

plugins {
    id("com.android.library")
    id("kotlin-android")
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

android {
    namespace = "sp.ax.jc.clicks"
    compileSdk = Version.Android.compileSdk

    defaultConfig {
        minSdk = Version.Android.minSdk
        manifestPlaceholders["appName"] = "@string/app_name"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
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
    androidTestImplementation("androidx.compose.ui:ui-test-junit4:${Version.Android.compose}")
    "${android.testBuildType}Implementation"("androidx.compose.ui:ui-test-manifest:${Version.Android.compose}")
}
