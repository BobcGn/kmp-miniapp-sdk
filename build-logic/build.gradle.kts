// Build logic for this repository only: it is not published and is not applied by a consumer.
//
// It lives in its own included build so that the bundle-size report can be a typed, tested task
// instead of another script-level Exec, without putting anything into the consumer-facing plugin.
plugins {
    `java-gradle-plugin`
    id("org.jetbrains.kotlin.jvm") version "2.4.20"
}

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

kotlin {
    compilerOptions {
        jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17)
    }
}

dependencies {
    testImplementation(kotlin("test"))
}

gradlePlugin {
    plugins {
        create("miniappSize") {
            id = "io.github.bobcgn.miniapp.size"
            implementationClass = "io.github.bobcgn.miniapp.buildlogic.MiniAppSizePlugin"
        }
    }
}