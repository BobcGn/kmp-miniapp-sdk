rootProject.name = "kmp-demo-with-server"

pluginManagement {
    // Consume the SDK plugin from this checkout, including unreleased root-host support.
    includeBuild("../..")
    repositories {
        google {
            mavenContent {
                includeGroupAndSubgroups("androidx")
                includeGroupAndSubgroups("com.android")
                includeGroupAndSubgroups("com.google")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositories {
        google {
            mavenContent {
                includeGroupAndSubgroups("androidx")
                includeGroupAndSubgroups("com.android")
                includeGroupAndSubgroups("com.google")
            }
        }
        mavenCentral()
    }
}

// Resolve the plugin-injected runtime coordinate against the same SDK checkout.
includeBuild("../..")

include(":app:androidApp")
include(":app:shared")
include(":core")
include(":server")