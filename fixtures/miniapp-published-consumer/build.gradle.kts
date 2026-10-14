import java.security.MessageDigest
import java.util.zip.ZipFile
import org.gradle.api.artifacts.component.ModuleComponentIdentifier

plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.miniapp)
}

// Runtime dependency and miniappMain come from the released plugin, not the SDK checkout.
val releasedVersion = libs.versions.miniapp.get()
val sdkCompileClasspath = configurations.named("miniappCompileClasspath")
val publishedJsSources = configurations.detachedConfiguration(
    dependencies.create("io.github.bobcgn:kmp-miniapp-sdk-js:$releasedVersion:sources@jar"),
).apply { isTransitive = false }
val navigationReport = layout.buildDirectory.file("reports/published-sdk-navigation.txt")

tasks.register("verifyPublishedSdkSources") {
    group = "verification"
    description = "Checks released SDK binary identity and source/KDoc content; not IDE navigation."
    // Resolution and archive inspection are intentionally rerun for each acceptance invocation.
    notCompatibleWithConfigurationCache("Acceptance inspects the resolved external dependency graph")
    doLast {
        check(gradle.includedBuilds.isEmpty()) { "Published acceptance must not use included builds" }
        val artifacts = sdkCompileClasspath.get().incoming.artifacts.artifacts.filter {
            val id = it.id.componentIdentifier
            id is ModuleComponentIdentifier && id.group == "io.github.bobcgn" &&
                id.module.startsWith("kmp-miniapp-sdk")
        }
        check(artifacts.isNotEmpty()) { "Published SDK artifacts were not resolved" }
        check(sdkCompileClasspath.get().incoming.resolutionResult.allComponents.none {
            it.id is org.gradle.api.artifacts.component.ProjectComponentIdentifier && it.id != sdkCompileClasspath.get().incoming.resolutionResult.rootComponent.get().id
        }) { "A project dependency replaced the published runtime" }
        artifacts.forEach {
            val id = it.id.componentIdentifier as ModuleComponentIdentifier
            check(id.version == releasedVersion) { "Unexpected SDK version: $id" }
        }
        val sources = publishedJsSources.singleFile
        val requiredSources = listOf(
            "jsMain/io/github/bobcgn/miniapp/export/MiniAppExports.kt",
            "commonMain/io/github/bobcgn/miniapp/api/MiniAppSdk.kt",
        )
        ZipFile(sources).use { archive ->
            requiredSources.forEach { name ->
                val entry = checkNotNull(archive.getEntry(name)) { "Missing source: $name" }
                val content = archive.getInputStream(entry).bufferedReader().use { it.readText() }
                check(content.contains("/**")) { "Missing KDoc in $name" }
                if (name.endsWith("MiniAppExports.kt")) {
                    check(content.contains("public fun wechatRuntimeInfo()"))
                    check(content.contains("public fun sdkVersion()"))
                    check(content.contains("The base-library version is"))
                }
            }
        }
        fun sha256(file: java.io.File): String = MessageDigest.getInstance("SHA-256")
            .digest(file.readBytes()).joinToString("") { "%02x".format(it) }
        val lines = listOf(
            "sdk.version=$releasedVersion",
            "includedBuilds=0",
        ) + artifacts.flatMap {
            listOf("sdk.binary=${it.id.componentIdentifier}", "sdk.binary.path=${it.file}", "sdk.binary.sha256=${sha256(it.file)}")
        } + listOf(
            "sdk.sources=$sources", "sdk.sources.sha256=${sha256(sources)}",
            "sdk.sourceEntries=${requiredSources.joinToString()}",
            "sdk.sources.verified=true", "scope=artifact contents only; IDEA source attachment and KDoc acceptance pending",
        )
        navigationReport.get().asFile.apply { parentFile.mkdirs(); writeText(lines.joinToString("\n", postfix = "\n")) }
        lines.forEach(::println)
    }
}
