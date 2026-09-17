// Model A — consumer declares a named Kotlin/JS target itself.
plugins {
    id("org.jetbrains.kotlin.multiplatform")
}

kotlin {
    js("miniapp") {
        nodejs()
        useCommonJs()
        binaries.library()
        generateTypeScriptDefinitions()
    }

    sourceSets {
        commonTest.dependencies {
            implementation(kotlin("test"))
        }
    }
}
