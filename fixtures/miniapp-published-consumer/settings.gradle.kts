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
// Deliberately no includeBuild or mavenLocal: this is release consumer acceptance.
rootProject.name = "miniapp-published-consumer"
