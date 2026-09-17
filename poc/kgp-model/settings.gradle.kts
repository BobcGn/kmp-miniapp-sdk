pluginManagement {
    repositories {
        gradlePluginPortal()
        mavenCentral()
    }
    includeBuild("miniapp-poc-plugin")
}

dependencyResolutionManagement {
    repositories {
        mavenCentral()
    }
}

rootProject.name = "kgp-model-poc"

include(":model-a")
include(":model-b")
include(":model-c")
include(":model-c2")
