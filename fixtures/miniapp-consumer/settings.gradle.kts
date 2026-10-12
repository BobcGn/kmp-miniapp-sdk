pluginManagement {
    repositories {
        gradlePluginPortal()
        mavenCentral()
    }

    // Resolve the plugin by id and substitute the runtime's public module coordinate with
    // this checkout. Public releases exist, but repository verification must test current source.
    // An online consumer removes both includeBuild calls; the source sets and build script stay.
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
