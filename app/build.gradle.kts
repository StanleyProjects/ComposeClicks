import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import sp.gx.core.camelCase
import sp.gx.core.getByName
import sp.gx.core.kebabCase

repositories {
    google()
    mavenCentral()
    maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
}

plugins {
    id("com.android.application")
    id("kotlin-android")
    id("org.jetbrains.compose") version Version.compose
}

android {
    namespace = "sp.sample.clicks"
    compileSdk = Version.Android.compileSdk

    defaultConfig {
        applicationId = namespace
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

    composeOptions.kotlinCompilerExtensionVersion = "1.5.15"
}

androidComponents.onVariants { variant ->
    val output = variant.outputs.single()
    check(output is com.android.build.api.variant.impl.VariantOutputImpl)
    android.defaultConfig.versionName
    val outputFileName = kebabCase(
        camelCase(rootProject.name, "Sample"),
        android.defaultConfig.versionName!!,
        variant.name,
        android.defaultConfig.versionCode!!.toString(),
    )
    output.outputFileName.set("$outputFileName.apk")
    afterEvaluate {
        tasks.getByName<JavaCompile>("compile", variant.name, "JavaWithJavac") {
            targetCompatibility = Version.jvmTarget
        }
        tasks.getByName<KotlinCompile>("compile", variant.name, "Kotlin") {
            kotlinOptions.jvmTarget = Version.jvmTarget
        }
    }
}

dependencies {
    implementation(project(":lib"))
    implementation("androidx.activity:activity-compose:1.10.1")
    implementation(compose.foundation)
}
