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

    dependsOn(":sdk:jsNodeProductionLibraryDistribution")

    val compilerDistribution = project(":sdk").layout.buildDirectory.dir("dist/js/productionLibrary")
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
