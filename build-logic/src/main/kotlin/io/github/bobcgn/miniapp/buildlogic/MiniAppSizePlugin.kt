package io.github.bobcgn.miniapp.buildlogic

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.VersionCatalogsExtension
import org.gradle.api.provider.Provider
import java.time.LocalDate

/**
 * Registers this repository's bundle-size tasks.
 *
 * This build logic is not published and is not applied by a consumer: it measures the artifacts
 * this repository produces. It registers nothing on a consumer's build path, and the only task it
 * adds to a lifecycle is none — both tasks are run explicitly.
 */
public class MiniAppSizePlugin : Plugin<Project> {

    override fun apply(target: Project) {
        val distributionTask = ":kmp-miniapp-sdk:jsNodeProductionLibraryDistribution"
        val defaultDirectory = "sdk/build/dist/js/productionLibrary"

        // A relative path, so a report never carries a developer's home directory.
        val directoryLabel: Provider<String> = target.providers
            .gradleProperty("miniappSizeDirectory")
            .orElse(defaultDirectory)

        val measuredDirectory = directoryLabel.map { label -> target.layout.projectDirectory.dir(label) }

        val baselineFile = target.layout.projectDirectory.file("docs/performance-baseline.json")
        val reportFile = target.layout.buildDirectory.file("reports/miniapp-size/bundle-size.json")

        val commit: Provider<String> = target.providers
            .exec { it.commandLine("git", "rev-parse", "--short", "HEAD") }
            .standardOutput
            .asText
            .map { it.trim() }
            .orElse("unknown")

        target.tasks.register("verifyMiniAppBundleSize", MiniAppBundleSizeReportTask::class.java) { task ->
            task.group = "verification"
            task.description =
                "Measures the Mini App distribution, reports raw and gzip bytes per file and " +
                    "category, and compares it with the committed baseline."

            task.bundleDirectory.set(measuredDirectory)
            task.directoryLabel.set(directoryLabel)
            // A distribution that lost one of these is not a smaller distribution.
            task.requiredArtifacts.set(listOf("kmp-miniapp-sdk-kotlin.js", "kotlin-kotlin-stdlib.js"))
            task.gzipLevel.set(DeterministicGzip.LEVEL)
            task.recordedCommit.set(commit)
            task.recordedGradle.set(target.gradle.gradleVersion)
            task.recordedKotlin.set(versionFromCatalog(target, "kotlin"))
            task.recordedSdk.set(versionFromCatalog(target, "miniapp"))
            task.baselineFile.set(baselineFile)
            task.reportFile.set(reportFile)

            task.dependsOn(distributionTask)
        }

        target.tasks.register("updateMiniAppSizeBaseline", MiniAppSizeBaselineTask::class.java) { task ->
            task.group = "verification"
            task.description =
                "Rewrites the committed bundle-size baseline from a fresh measurement. Run it " +
                    "deliberately, and review the diff it produces."

            task.bundleDirectory.set(measuredDirectory)
            task.directoryLabel.set(directoryLabel)
            task.requiredArtifacts.set(listOf("kmp-miniapp-sdk-kotlin.js", "kotlin-kotlin-stdlib.js"))
            task.gzipLevel.set(DeterministicGzip.LEVEL)
            task.recordedCommit.set(commit)
            task.recordedDate.set(LocalDate.now().toString())
            task.recordedGradle.set(target.gradle.gradleVersion)
            task.recordedKotlin.set(versionFromCatalog(target, "kotlin"))
            task.recordedSdk.set(versionFromCatalog(target, "miniapp"))
            task.baselineFile.set(baselineFile)

            task.dependsOn(distributionTask)
        }
    }

    /**
     * Reads a version from the repository's version catalog.
     *
     * The catalog is the single source of the Kotlin and SDK versions, so the baseline records what
     * the build actually used instead of restating a number that could drift from it.
     */
    private fun versionFromCatalog(target: Project, alias: String): String {
        val catalogs = target.rootProject.extensions
            .findByType(VersionCatalogsExtension::class.java)
            ?: return "unknown"
        val catalog = catalogs.find("libs").orElse(null) ?: return "unknown"
        return catalog.findVersion(alias).map { it.requiredVersion }.orElse("unknown")
    }
}
