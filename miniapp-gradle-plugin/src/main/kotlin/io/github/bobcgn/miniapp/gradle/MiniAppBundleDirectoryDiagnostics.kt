package io.github.bobcgn.miniapp.gradle

import org.gradle.api.GradleException
import java.io.File

/**
 * Validates the consumer-chosen bundle directory.
 *
 * `assembleMiniAppBundle` is a Gradle `Sync`, which deletes files in its destination that the bundle
 * does not contain. That is the right behaviour for a build output and a dangerous one for a
 * directory the build does not own: pointing a `Sync` at a source tree, a home directory or the
 * project root would delete files rather than assemble an artifact.
 *
 * The rule is therefore that the bundle directory is a directory *inside* the project. A bundle
 * written into the project's own mini program folder is allowed; writing outside the project is not,
 * and neither is the project directory itself. Relaxing that needs a real external case, and this is
 * the check that should be relaxed deliberately rather than removed.
 */
internal object MiniAppBundleDirectoryDiagnostics {

    public const val MESSAGE_HEADER: String =
        "The Mini App bundle directory must be a directory inside the project."

    public fun requireInsideProject(bundleDirectory: File, projectDirectory: File) {
        val bundle = bundleDirectory.canonicalFile
        val project = projectDirectory.canonicalFile
        if (bundle != project && bundle.toPath().startsWith(project.toPath())) return

        throw GradleException(
            buildString {
                appendLine(MESSAGE_HEADER)
                appendLine("  configured: $bundle")
                appendLine("  project:    $project")
                appendLine()
                appendLine("'assembleMiniAppBundle' uses Gradle's Sync, which deletes files in the")
                appendLine("destination that the bundle does not contain. A directory outside the")
                appendLine("project, or the project directory itself, would be deleted rather than")
                appendLine("assembled into.")
                appendLine()
                appendLine("Set it to a path inside the project, for example:")
                appendLine("    miniapp {")
                appendLine("        wechat {")
                appendLine("            bundleDirectory.set(layout.buildDirectory.dir(\"miniapp/bundle\"))")
                appendLine("        }")
                append("    }")
            },
        )
    }
}
