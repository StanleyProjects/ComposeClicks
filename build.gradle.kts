import sp.gx.core.check

buildscript {
    repositories {
        google()
        mavenCentral()
    }

    dependencies {
        classpath("com.android.tools.build:gradle:8.1.2")
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:${Version.kotlin}")
    }
}

task<Delete>("clean") {
    delete = setOf(layout.buildDirectory.get(), "buildSrc/build")
}

repositories.mavenCentral()

val ktlint: Configuration by configurations.creating

dependencies {
    ktlint("com.pinterest:ktlint:${Version.ktlint}") {
        attributes {
            attribute(Bundling.BUNDLING_ATTRIBUTE, objects.named(Bundling.EXTERNAL))
        }
    }
}

task<JavaExec>("checkCodeStyle") {
    classpath = ktlint
    mainClass = "com.pinterest.ktlint.Main"
    val reporter = "html"
    val output = layout.buildDirectory.get()
        .dir("reports/analysis/code/style/html")
        .file("index.html")
        .asFile
    args(
        "build.gradle.kts",
        "settings.gradle.kts",
        "buildSrc/src/main/kotlin/**/*.kt",
        "buildSrc/build.gradle.kts",
        "lib/src/main/kotlin/**/*.kt",
        "lib/src/test/kotlin/**/*.kt",
        "lib/build.gradle.kts",
        "--reporter=$reporter,output=${output.absolutePath}",
    )
}

task("checkLicense") {
    doLast {
        val author = "Stanley Wintergreen" // todo
        val report = layout.buildDirectory.get()
            .dir("reports/analysis/license")
            .file("index.html")
            .asFile
        rootDir.resolve("LICENSE").check(
            expected = emptySet(),
            regexes = setOf("^Copyright 2\\d{3} $author${'$'}".toRegex()),
            report = report,
        )
    }
}
