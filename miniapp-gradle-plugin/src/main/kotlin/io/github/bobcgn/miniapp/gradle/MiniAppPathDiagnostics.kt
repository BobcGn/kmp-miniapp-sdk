package io.github.bobcgn.miniapp.gradle

import java.io.File
import org.gradle.api.GradleException

/**
 * Validates the three directories a Mini App build writes to or reads from.
 *
 * Every one of them reaches a `Sync` or a file-creating task, and each of those has a way to destroy
 * something: `Sync` deletes whatever its destination holds and its sources do not, and creating a
 * source host in the wrong place writes files a developer did not ask for.
 *
 * The rules are therefore stated per directory rather than as one generic "must be inside the
 * project", because the interesting case is the *relationship* between two of them:
 *
 * ```text
 * sourceDirectory            strictly inside the project directory
 * generatedLibraryDirectory  strictly inside sourceDirectory
 * distributionDirectory      strictly inside the project directory
 * ```
 *
 * "Strictly" excludes the container itself: pointing the generated area at the whole source host, or
 * the distribution at the project directory, would have the `Sync` delete the very tree it is meant
 * to fill. Each failure names the configured path, the path it must stay inside, and what would have
 * been deleted.
 */
internal object MiniAppPathDiagnostics {

    public const val SOURCE_HEADER: String =
        "The Mini App source directory must be a directory inside the project."

    public const val GENERATED_LIBRARY_HEADER: String =
        "The Mini App generated library directory must be a directory strictly inside the source directory."

    public const val DISTRIBUTION_HEADER: String =
        "The Mini App distribution directory must be a directory inside the project."

    private const val SOURCE_PROPERTY: String = "sourceDirectory"
    private const val GENERATED_LIBRARY_PROPERTY: String = "generatedLibraryDirectory"
    private const val DISTRIBUTION_PROPERTY: String = "distributionDirectory"

    /** The source host is created here, so it must not be the project directory or anything outside it. */
    public fun requireSourceInsideProject(
        sourceDirectory: File,
        projectDirectory: File,
    ) {
        requireStrictlyInside(
            header = SOURCE_HEADER,
            property = SOURCE_PROPERTY,
            configured = sourceDirectory,
            container = projectDirectory,
            containerLabel = "project",
            consequence = listOf(
                "initMiniApp creates the Mini App source host there, and a source host that is the",
                "project directory or lives outside it is not a project the plugin can own.",
            ),
        )
    }

    /** The generated area is a `Sync` destination, so it must not be its own container. */
    public fun requireGeneratedLibraryInsideSource(
        generatedLibraryDirectory: File,
        sourceDirectory: File,
    ) {
        requireStrictlyInside(
            header = GENERATED_LIBRARY_HEADER,
            property = GENERATED_LIBRARY_PROPERTY,
            configured = generatedLibraryDirectory,
            container = sourceDirectory,
            containerLabel = "source",
            consequence = listOf(
                "syncMiniApp uses Gradle's Sync, which deletes files in the destination that the",
                "bundle does not contain. Pointed at the source host itself, or anywhere outside it,",
                "it would delete handwritten files rather than fill the generated area.",
            ),
        )
    }

    /** The distribution is a `Sync` destination, so it must not be the project directory. */
    public fun requireDistributionInsideProject(
        distributionDirectory: File,
        projectDirectory: File,
    ) {
        requireStrictlyInside(
            header = DISTRIBUTION_HEADER,
            property = DISTRIBUTION_PROPERTY,
            configured = distributionDirectory,
            container = projectDirectory,
            containerLabel = "project",
            consequence = listOf(
                "assembleMiniApp uses Gradle's Sync, which deletes files in the destination that the",
                "assembled project does not contain. Pointed at the project directory, or anywhere",
                "outside it, it would delete source rather than assemble an artifact.",
            ),
        )
    }

    /**
     * The generated area's path relative to the source host, as the distribution lays it out.
     *
     * `assembleMiniApp` places the bundle at the same relative location it occupies in the source
     * host, so the host project it produces has the shape the developer's own project has. A pair
     * that is not in an ancestor/descendant relationship has no such path and is refused here rather
     * than silently producing a distribution with the bundle somewhere else.
     */
    public fun relativeGeneratedLibraryPath(
        generatedLibraryDirectory: File,
        sourceDirectory: File,
    ): String {
        val source = sourceDirectory.canonicalFile.toPath()
        val generated = generatedLibraryDirectory.canonicalFile.toPath()
        if (generated == source || !generated.startsWith(source)) {
            requireGeneratedLibraryInsideSource(generatedLibraryDirectory, sourceDirectory)
        }
        return source.relativize(generated).joinToString("/") { element -> element.toString() }
    }

    /** A disposable Sync output may neither contain nor be contained by handwritten source. */
    public fun requireSourceAndDistributionDisjoint(sourceDirectory: File, distributionDirectory: File) {
        val source = sourceDirectory.canonicalFile.toPath()
        val distribution = distributionDirectory.canonicalFile.toPath()
        if (source.startsWith(distribution) || distribution.startsWith(source)) {
            throw GradleException("Mini App sourceDirectory and distributionDirectory must not overlap: $source and $distribution")
        }
    }

    /** Root clean must never own the handwritten source host. */
    public fun requireSourceOutsideBuild(sourceDirectory: File, buildDirectory: File) {
        val source = sourceDirectory.canonicalFile.toPath()
        val build = buildDirectory.canonicalFile.toPath()
        if (source.startsWith(build) || build.startsWith(source)) {
            throw GradleException("The root Mini App sourceDirectory must stay outside the root build directory: $source")
        }
    }

    /** Reject an existing UI/source tree before Sync can erase it as if it were compiler output. */
    public fun requireNoHandwrittenFilesInGeneratedLibrary(generatedLibraryDirectory: File) {
        if (!generatedLibraryDirectory.exists()) return
        val protectedNames = setOf("app.js", "app.ts", "app.json", "project.config.json", "sitemap.json")
        val protectedExtensions = setOf("wxml", "wxss", "kt", "swift", "java")
        val handwritten = generatedLibraryDirectory.walkTopDown().firstOrNull {
            it.isFile && (it.name in protectedNames || it.extension in protectedExtensions)
        }
        if (handwritten != null) {
            throw GradleException("The Mini App generated library directory contains handwritten host/source files: $handwritten")
        }
    }

    private fun requireStrictlyInside(
        header: String,
        property: String,
        configured: File,
        container: File,
        containerLabel: String,
        consequence: List<String>,
    ) {
        val resolved = configured.canonicalFile
        val root = container.canonicalFile
        if (resolved != root && resolved.toPath().startsWith(root.toPath())) return

        throw GradleException(
            buildString {
                appendLine(header)
                appendLine("  configured: $resolved")
                appendLine("  $containerLabel: $root")
                appendLine()
                consequence.forEach { appendLine(it) }
                appendLine()
                appendLine("Set it to a path inside the $containerLabel, for example:")
                appendLine("    miniapp {")
                appendLine("        wechat {")
                appendLine("            $property.set(<a directory inside the $containerLabel>)")
                appendLine("        }")
                append("    }")
            },
        )
    }
}
