import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    `java-gradle-plugin`
    // Version comes from the root build's `libs.plugins.kotlin.multiplatform` alias, which already
    // puts the whole Kotlin Gradle Plugin distribution on this build's classpath.
    id("org.jetbrains.kotlin.jvm")
}

group = "io.github.bobcgn"
version = "0.1.0-SNAPSHOT"

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

kotlin {
    explicitApi()

    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_17)
    }
}

dependencies {
    // Supplied by the consumer's build at runtime; needed only to compile against the Kotlin
    // Multiplatform DSL. The plugin must not ship the Kotlin Gradle Plugin.
    compileOnly(libs.kotlin.gradle.plugin)

    testImplementation(kotlin("test"))
    testImplementation(gradleTestKit())
}

gradlePlugin {
    plugins {
        create("miniapp") {
            id = "io.github.bobcgn.miniapp"
            implementationClass = "io.github.bobcgn.miniapp.gradle.MiniAppGradlePlugin"
            displayName = "Mini App Gradle plugin"
            description = "Adds Mini App platform build integration to a Kotlin Multiplatform project."
        }
    }
}

// The Kotlin Multiplatform fixtures the plugin tests apply must use the same Kotlin version as this
// repository, so the version is read from the catalog rather than duplicated in a fixture.
tasks.withType<Test>().configureEach {
    systemProperty("miniappPlugin.kotlinVersion", libs.versions.kotlin.get())
}
