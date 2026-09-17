// Model B — consumer keeps a default Kotlin/JS target and only adds custom source sets.
plugins {
    id("org.jetbrains.kotlin.multiplatform")
}

kotlin {
    js {
        nodejs()
        useCommonJs()
        binaries.library()
    }

    sourceSets {
        commonTest.dependencies {
            implementation(kotlin("test"))
        }

        val miniappMain by creating {
            dependsOn(commonMain.get())
        }

        val miniappTest by creating {
            dependsOn(commonTest.get())
        }
    }
}
