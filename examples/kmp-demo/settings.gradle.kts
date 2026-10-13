rootProject.name = "kmp-demo"

pluginManagement {
    // Resolve the SDK plugin from this checkout, as the repository consumer fixture does.
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

// Resolve the runtime public coordinate against the same SDK checkout.
includeBuild("../..")

include(":androidApp")
include(":shared")
