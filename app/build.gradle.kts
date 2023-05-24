import sp.gx.core.camelCase
import sp.gx.core.kebabCase

repositories {
    google()
    mavenCentral()
}

plugins {
    id("com.android.application")
    id("kotlin-android")
}

val appId = "sp.sample.clicks"

android {
    namespace = appId
    compileSdk = Version.Android.compileSdk

    defaultConfig {
        applicationId = appId
        minSdk = Version.Android.minSdk
        targetSdk = Version.Android.targetSdk
        versionName = "0.0.1"
        versionCode = 1
        manifestPlaceholders["appName"] = "@string/app_name"
    }

    buildTypes {
        getByName("debug") {
            applicationIdSuffix = ".$name"
            versionNameSuffix = "-$name"
            isMinifyEnabled = false
            isShrinkResources = false
            manifestPlaceholders["buildType"] = name
        }
    }

    buildFeatures.compose = true

    composeOptions.kotlinCompilerExtensionVersion = Version.Android.compose
}

androidComponents.onVariants { variant ->
    val output = variant.outputs.single()
    check(output is com.android.build.api.variant.impl.VariantOutputImpl)
    android.defaultConfig.versionName
    val outputFileName = kebabCase(
        camelCase(rootProject.name, "Sample"),
        android.defaultConfig.versionName!!,
        variant.name,
        android.defaultConfig.versionCode.toString(),
    )
    output.outputFileName.set("$outputFileName.apk")
    afterEvaluate {
        tasks.getByName<JavaCompile>("compile${variant.name.capitalize()}JavaWithJavac") {
            targetCompatibility = Version.jvmTarget
        }
        tasks.getByName<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>("compile${variant.name.capitalize()}Kotlin") {
            kotlinOptions.jvmTarget = Version.jvmTarget
        }
    }
}

dependencies {
    implementation(project(":lib"))
    implementation("androidx.activity:activity-compose:1.6.1")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("androidx.compose.foundation:foundation:${Version.Android.compose}")
}
