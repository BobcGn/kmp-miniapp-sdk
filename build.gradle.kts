plugins {
    alias(libs.plugins.kotlin.multiplatform) apply false
}

val miniAppSdkArtifacts = listOf(
    "kmp-miniapp-sdk-kotlin.js",
    "kmp-miniapp-sdk-kotlin.js.map",
    "kmp-miniapp-sdk-kotlin.d.ts",
    "kotlin-kotlin-stdlib.js",
    "kotlin-kotlin-stdlib.js.map",
    "kotlinx-atomicfu.js",
    "kotlinx-atomicfu.js.map",
    "kotlinx-coroutines-core.js",
    "kotlinx-coroutines-core.js.map",
)

tasks.register<Sync>("buildMiniAppSdk") {
    group = "distribution"
    description = "Builds and copies the Kotlin/JS SDK artifacts into the WeChat integration host."

    dependsOn(":kmp-miniapp-sdk:jsNodeProductionLibraryDistribution")

    val compilerDistribution = project(":kmp-miniapp-sdk").layout.buildDirectory.dir("dist/js/productionLibrary")
    val consumerDistribution = layout.projectDirectory.dir(
        "examples/wechat-miniprogram/miniprogram/libs",
    )

    from(compilerDistribution) {
        include(miniAppSdkArtifacts)
    }
    into(consumerDistribution)

    preserve {
        include("kmp-miniapp-sdk.js", "kmp-miniapp-sdk.d.ts")
    }
}

// The consumer integration fixture is an ordinary Gradle build beside this one, not a module of it:
// it must consume the plugin and the runtime the way an external project does. These tasks run it
// from the repository so the fixture is part of the repository's verification rather than a
// directory somebody remembers to open. They are not wired into `check`, because they compile
// Kotlin/JS and install npm dependencies.
val consumerFixtureDirectory = layout.projectDirectory.dir("fixtures/miniapp-consumer")
val consumerFixtureWrapper = layout.projectDirectory.file("gradlew").asFile.absolutePath

tasks.register<Exec>("verifyMiniAppConsumer") {
    group = "verification"
    description = "Builds the consumer fixture from clean and asserts the default bundle contract."

    workingDir(consumerFixtureDirectory)
    commandLine(
        consumerFixtureWrapper,
        "--no-daemon",
        "--console=plain",
        "clean",
        "miniappTest",
        "assembleMiniAppBundle",
        "verifyConsumerContract",
    )
}

tasks.register<Exec>("verifyMiniAppConsumerHostBundle") {
    group = "verification"
    description = "Assembles the consumer fixture's bundle into the WeChat host's mini program."

    workingDir(consumerFixtureDirectory)
    commandLine(
        consumerFixtureWrapper,
        "--no-daemon",
        "--console=plain",
        "clean",
        "assembleMiniAppBundle",
        "verifyConsumerContract",
        "-PminiappBundleDirectory=host/miniprogram/libs",
    )
}

tasks.register<Exec>("verifyMiniAppConsumerHostSmoke") {
    group = "verification"
    description = "Loads the consumer fixture's bundle the way the WeChat host's page does."

    dependsOn("verifyMiniAppConsumerHostBundle")
    workingDir(consumerFixtureDirectory.dir("host"))
    commandLine("node", "scripts/host-smoke.cjs")
}

tasks.register("verifyMiniAppConsumerFixture") {
    group = "verification"
    description = "Runs every consumer integration fixture check from clean."

    dependsOn("verifyMiniAppConsumer", "verifyMiniAppConsumerHostSmoke")
}

// Both fixture runs start with `clean`, so their order is fixed rather than left to the task graph.
tasks.named("verifyMiniAppConsumerHostBundle") {
    mustRunAfter("verifyMiniAppConsumer")
}
