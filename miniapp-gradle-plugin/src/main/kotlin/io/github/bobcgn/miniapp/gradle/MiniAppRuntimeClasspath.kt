package io.github.bobcgn.miniapp.gradle

/**
 * The Mini App runtime classpath, as the host-boundary check needs to see it.
 *
 * The two halves are reported separately because they answer different questions. [coordinates] is
 * what the bundle will carry; [unresolved] is what the resolution could not produce. Gradle's
 * [org.gradle.api.artifacts.result.ResolutionResult.allComponents] lists only the first, so a check
 * that reads it alone cannot distinguish a classpath without a renderer from one it never saw.
 */
internal data class MiniAppRuntimeClasspath(
    val coordinates: List<String>,
    val unresolved: List<String>,
)
