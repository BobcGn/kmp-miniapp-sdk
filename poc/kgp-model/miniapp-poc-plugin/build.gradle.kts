import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    `java-gradle-plugin`
    kotlin("jvm") version "2.4.20"
}

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

kotlin {
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_17)
    }
}

dependencies {
    // Provided by the consumer build at runtime; only needed to compile against the KGP DSL.
    compileOnly("org.jetbrains.kotlin:kotlin-gradle-plugin:2.4.20")
    compileOnly("org.jetbrains.kotlin:kotlin-gradle-plugin-api:2.4.20")
}

gradlePlugin {
    plugins {
        create("miniappPoc") {
            id = "io.github.bobcgn.miniapp.poc"
            implementationClass = "io.github.bobcgn.miniapp.poc.MiniAppPocPlugin"
        }
        create("miniappHiddenTargetPoc") {
            id = "io.github.bobcgn.miniapp.poc.hidden"
            implementationClass = "io.github.bobcgn.miniapp.poc.MiniAppHiddenTargetPocPlugin"
        }
    }
}
