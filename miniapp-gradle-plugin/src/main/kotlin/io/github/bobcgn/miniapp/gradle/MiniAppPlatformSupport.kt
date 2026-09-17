package io.github.bobcgn.miniapp.gradle

import org.gradle.api.Project
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension

/**
 * Registers the Mini App platform target, its source sets, and its local test run.
 *
 * ADR 0010 chose a Kotlin/JS target named after the platform: the Kotlin Gradle Plugin then derives
 * the real `miniappMain` and `miniappTest` source sets, the compilations that own them, and the
 * `commonMain` / `commonTest` edges, from that name. The plugin creates the target and the test run;
 * it never creates a source set or a `dependsOn` edge, because hand-built ones are exactly the
 * "unused source set" model ADR 0010 rejected.
 *
 * `js(name, configure)` creates the target when it is absent and reconfigures it when an existing
 * target was produced from the same preset. That is what makes applying this plugin twice harmless:
 * the second call reaches the same target rather than adding a second one.
 *
 * The `nodejs()` test run is what gives `miniappTest` a real execution task (`miniappNodeTest`).
 * Node.js is the local Kotlin/JS test environment, not a deployment host; ADR 0010 records that
 * choice and its naming consequence.
 *
 * Deliberately still absent, because each belongs to a later issue:
 *
 * - SDK dependency wiring belongs to BOB-82.
 * - CommonJS output, library binaries, TypeScript declarations and the WeChat artifact assembly
 *   belong to BOB-81, and the host-side build configuration belongs to BOB-84. Nothing here decides
 *   how the host consumes an artifact.
 * - No Mini App Gradle DSL exists yet; that is BOB-84.
 *
 * This class references Kotlin Gradle Plugin types, so it is kept separate from
 * [MiniAppGradlePlugin]: the plugin must be able to report a missing Kotlin Multiplatform plugin
 * without the Kotlin Gradle Plugin being on the build classpath at all.
 */
internal object MiniAppPlatformSupport {

    public fun registerMiniAppPlatformTarget(project: Project) {
        val kotlin = project.extensions.getByType(KotlinMultiplatformExtension::class.java)
        kotlin.js(MiniAppPluginDiagnostics.MINIAPP_TARGET_NAME) {
            nodejs()
        }
    }
}
