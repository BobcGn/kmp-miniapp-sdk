package io.github.bobcgn.miniapp.gradle

import org.gradle.api.Project
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension

/**
 * Registers the Mini App platform target in a Kotlin Multiplatform project.
 *
 * ADR 0010 chose a Kotlin/JS target named after the platform: the Kotlin Gradle Plugin then derives
 * the real `miniappMain` and `miniappTest` source sets, and the compilations that own them, from
 * that name. Creating the target is therefore the whole of the registration.
 *
 * Everything else is deliberately absent, because each belongs to a later issue:
 *
 * - source-set hierarchy construction and IDE-level provisioning belong to BOB-76; the Kotlin
 *   Gradle Plugin owns the `commonMain` / `commonTest` edges and the plugin must not rebuild them.
 * - SDK dependency wiring belongs to BOB-82.
 * - CommonJS output, library binaries, TypeScript declarations and the WeChat artifact assembly
 *   belong to BOB-81, and the host-side build configuration belongs to BOB-84.
 *
 * This class references Kotlin Gradle Plugin types, so it is kept separate from
 * [MiniAppGradlePlugin]: the plugin must be able to report a missing Kotlin Multiplatform plugin
 * without the Kotlin Gradle Plugin being on the build classpath at all.
 */
internal object MiniAppPlatformSupport {

    public fun registerMiniAppPlatformTarget(project: Project) {
        val kotlin = project.extensions.getByType(KotlinMultiplatformExtension::class.java)
        kotlin.js(MiniAppPluginDiagnostics.MINIAPP_TARGET_NAME)
    }
}
