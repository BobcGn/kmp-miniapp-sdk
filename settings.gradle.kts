pluginManagement {
    repositories {
        gradlePluginPortal()
        mavenCentral()
    }
}

dependencyResolutionManagement {
    repositories {
        mavenCentral()
    }
}

rootProject.name = "kmp-miniapp-sdk"

// The runtime module's directory says where its code lives; its project name is the public
// coordinate it publishes under. Composite-build dependency substitution matches on `group:name`,
// so the name must be the artifact id a consumer will resolve: `io.github.bobcgn:kmp-miniapp-sdk`.
include(":kmp-miniapp-sdk")
project(":kmp-miniapp-sdk").projectDir = file("sdk")

include(":miniapp-gradle-plugin")
