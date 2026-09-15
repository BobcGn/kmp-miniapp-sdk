plugins {
    alias(libs.plugins.kotlin.multiplatform)
}

group = "io.github.bobcgn"
version = "0.1.0-SNAPSHOT"

kotlin {
    explicitApi()

    js {
        outputModuleName.set("kmp-miniapp-sdk-kotlin")
        nodejs()
        useCommonJs()
        binaries.library()
        generateTypeScriptDefinitions()
    }

    sourceSets {
        commonMain.dependencies {
            implementation(libs.kotlinx.coroutines.core)
        }

        commonTest.dependencies {
            implementation(kotlin("test"))
            implementation(libs.kotlinx.coroutines.test)
        }
    }
}
