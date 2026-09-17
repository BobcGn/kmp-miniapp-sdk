package io.github.bobcgn.miniapp.gradle

import org.gradle.api.GradleException

/**
 * Stable names and messages of the Mini App Gradle plugin.
 *
 * Kept deliberately free of any Kotlin Gradle Plugin type so that the missing-Kotlin-Multiplatform
 * failure path loads and runs even when the Kotlin Gradle Plugin is absent from the build.
 */
internal object MiniAppPluginDiagnostics {

    public const val PLUGIN_ID: String = "io.github.bobcgn.miniapp"

    public const val KOTLIN_MULTIPLATFORM_PLUGIN_ID: String = "org.jetbrains.kotlin.multiplatform"

    /** The platform target name. ADR 0010 makes it the source of `miniappMain` / `miniappTest`. */
    public const val MINIAPP_TARGET_NAME: String = "miniapp"

    /** The lifecycle test task the Kotlin Gradle Plugin derives from the target name. */
    public const val MINIAPP_TEST_TASK_NAME: String = "miniappTest"

    /** The task that actually runs the `miniappTest` compilation's tests on Node.js. */
    public const val MINIAPP_NODE_TEST_TASK_NAME: String = "miniappNodeTest"

    /** First line of the missing-Kotlin-Multiplatform error; tests assert on this exact text. */
    public const val MISSING_KOTLIN_MULTIPLATFORM_MESSAGE: String =
        "The 'io.github.bobcgn.miniapp' plugin requires the Kotlin Multiplatform plugin."

    public fun missingKotlinMultiplatform(projectPath: String): GradleException = GradleException(
        buildString {
            appendLine(MISSING_KOTLIN_MULTIPLATFORM_MESSAGE)
            appendLine("Project '$projectPath' does not apply '$KOTLIN_MULTIPLATFORM_PLUGIN_ID'.")
            appendLine("Apply both plugins to the same project, for example:")
            appendLine("    plugins {")
            appendLine("        kotlin(\"multiplatform\")")
            appendLine("        id(\"$PLUGIN_ID\")")
            append("    }")
        },
    )
}
