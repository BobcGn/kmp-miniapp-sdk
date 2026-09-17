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

    /**
     * The Kotlin/JS production library distribution for the Mini App target.
     *
     * The Kotlin Gradle Plugin creates it once the target declares a library binary, and the
     * assembly task consumes its declared output rather than a hand-written path.
     */
    public const val MINIAPP_DISTRIBUTION_TASK_NAME: String = "miniappNodeProductionLibraryDistribution"

    /**
     * The consumer-facing assembly task.
     *
     * Named for the platform rather than for the current host, and deliberately distinct from this
     * repository's internal `buildMiniAppSdk` task, which builds the SDK itself for the WeChat
     * example rather than a consumer's application.
     */
    public const val ASSEMBLE_MINIAPP_BUNDLE_TASK_NAME: String = "assembleMiniAppBundle"

    /** The bundle's stable output directory, relative to the consumer's build directory. */
    public const val MINIAPP_BUNDLE_DIRECTORY: String = "miniapp/bundle"

    /**
     * The Mini App compilation's runtime classpath — exactly the dependency set the Kotlin/JS
     * production library distribution is built from, so it is also the set the host bundle carries.
     */
    public const val MINIAPP_RUNTIME_CLASSPATH_CONFIGURATION_NAME: String = "miniappRuntimeClasspath"

    /** Rejects a runtime classpath that would produce a host bundle carrying a client renderer. */
    public const val CHECK_MINIAPP_HOST_BOUNDARY_TASK_NAME: String = "checkMiniAppHostBoundary"

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
