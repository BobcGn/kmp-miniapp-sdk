package io.github.bobcgn.miniapp.gradle

import org.gradle.api.Project
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension

/**
 * Registers the Mini App platform target, its source sets, its local test run, and its runtime.
 *
 * ADR 0010 chose a Kotlin/JS target named after the platform: the Kotlin Gradle Plugin then derives
 * the real `miniappMain` and `miniappTest` source sets, the compilations that own them, and the
 * `commonMain` / `commonTest` edges, from that name. The plugin creates the target, the test run and
 * the runtime dependency; it never creates a source set or a `dependsOn` edge, because hand-built
 * ones are exactly the "unused source set" model ADR 0010 rejected.
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
        addMiniAppRuntimeDependency(project)
    }

    /**
     * Adds the runtime SDK to `miniappMain`, and to nothing else.
     *
     * `miniappTest` reaches the runtime through the source-set hierarchy: the test compilation
     * depends on the main compilation, so one declaration on `miniappMain` gives the test classpath
     * the same artifact. Declaring it twice would let the two resolve to different things.
     *
     * `commonMain`, the metadata compilation and every other target are untouched, so a project that
     * also targets Android, iOS or the JVM does not acquire a Mini App runtime on those platforms.
     *
     * The configuration is matched lazily because the Kotlin Gradle Plugin creates a source set's
     * dependency configurations as the target is configured, and an existing declaration is left
     * alone, so applying the plugin twice — or a consumer that declared the runtime itself — still
     * yields exactly one dependency.
     */
    private fun addMiniAppRuntimeDependency(project: Project) {
        val runtime = MiniAppRuntimeDependency
        project.configurations
            .matching { configuration ->
                configuration.name == MiniAppRuntimeDependency.CONSUMER_CONFIGURATION_NAME
            }
            .configureEach { configuration ->
                val alreadyDeclared = configuration.dependencies.any { dependency ->
                    dependency.group == runtime.group && dependency.name == runtime.name
                }
                if (!alreadyDeclared) {
                    configuration.dependencies.add(project.dependencies.create(runtime.coordinate))
                }
            }
    }
}
