pluginManagement {
    repositories {
        gradlePluginPortal()
        mavenCentral()
    }

    // Neither the plugin nor the runtime SDK is published yet, so an ordinary consumer build cannot
    // resolve them from a repository. A composite build of this repository stands in for
    // publication: the plugin is resolved by its id, and the runtime by its public module
    // coordinate (`io.github.bobcgn:kmp-miniapp-sdk`), exactly as a published consumer would
    // resolve them. When they are published, both includeBuild calls are what change — the build
    // script and the source sets do not.
    includeBuild("../..")
}

dependencyResolutionManagement {
    repositories {
        mavenCentral()
    }
}

// A plugin-management composite resolves plugins only; dependency substitution needs the build to
// be included at settings level as well. Gradle treats these as the same build, so it is configured
// once and serves both.
includeBuild("../..")

rootProject.name = "miniapp-consumer"
