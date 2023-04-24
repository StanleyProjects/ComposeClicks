repositories {
    google()
    mavenCentral()
}

plugins {
    id("com.android.library")
    id("kotlin-android")
}

android {
    namespace = "sp.ax.clicks"
    compileSdk = Version.Android.compileSdk

    defaultConfig {
        minSdk = Version.Android.minSdk
//        targetSdk = Version.Android.targetSdk
        manifestPlaceholders["appName"] = "@string/app_name"
    }

//    buildTypes {
//        getByName("debug") {
//            isMinifyEnabled = false
//            isShrinkResources = false
//            manifestPlaceholders["buildType"] = name
//        }
//    }
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
        check(variant.flavorName in setOf("snapshot"))
        val versionName = when (variant.buildType.name) {
            "release" -> "${Version.Application.name}-${variant.flavorName}"
            else -> "${Version.Application.name}-${variant.name}"
        }
        output.outputFileName = "${rootProject.name}-${versionName}-${Version.Application.code}.aar"
        afterEvaluate {
            tasks.getByName<JavaCompile>("compile${variant.name.capitalize()}JavaWithJavac") {
                targetCompatibility = Version.jvmTarget
            }
            tasks.getByName<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>("compile${variant.name.capitalize()}Kotlin") {
                kotlinOptions.jvmTarget = Version.jvmTarget
            }
        }
    }
}

//dependencies {
//    implementation("androidx.activity:activity-compose:1.6.1")
//    implementation("androidx.appcompat:appcompat:1.6.1")
//    implementation("androidx.compose.foundation:foundation:${Version.Android.compose}")
//    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.5.1")
//}
