pluginManagement {
    includeBuild("../..")
    repositories { gradlePluginPortal(); mavenCentral() }
}
dependencyResolutionManagement { repositories { mavenCentral() } }
includeBuild("../..")
rootProject.name = "miniapp-root-consumer"
include(":shared")
