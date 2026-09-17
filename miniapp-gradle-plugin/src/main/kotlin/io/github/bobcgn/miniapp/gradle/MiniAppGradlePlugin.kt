package io.github.bobcgn.miniapp.gradle

import org.gradle.api.Plugin
import org.gradle.api.Project

/**
 * Entry point of the `io.github.bobcgn.miniapp` plugin.
 *
 * BOB-78 establishes the skeleton: the plugin detects the Kotlin Multiplatform plugin and registers
 * the Mini App platform target chosen in BOB-83 / ADR 0010. It owns build integration and developer
 * experience only. The runtime SDK owns Mini App APIs and host capabilities, and the presentation
 * runtime belongs to P2.
 */
public class MiniAppGradlePlugin : Plugin<Project> {

    override fun apply(target: Project) {
        // Registered before the Kotlin Multiplatform plugin arrives, so the extension exists even on
        // the failure path, and guarded so applying this plugin twice does not create a second one.
        if (target.extensions.findByType(MiniAppExtension::class.java) == null) {
            target.extensions.create(MiniAppExtension.NAME, MiniAppExtension::class.java)
        }

        target.pluginManager.withPlugin(MiniAppPluginDiagnostics.KOTLIN_MULTIPLATFORM_PLUGIN_ID) {
            MiniAppPlatformSupport.registerMiniAppPlatformTarget(target)
        }

        // Deferred on purpose: applying this plugin before `kotlin("multiplatform")` is valid, so
        // the missing-plugin case is only knowable once the project has been evaluated.
        target.afterEvaluate { evaluated ->
            if (!evaluated.pluginManager.hasPlugin(MiniAppPluginDiagnostics.KOTLIN_MULTIPLATFORM_PLUGIN_ID)) {
                throw MiniAppPluginDiagnostics.missingKotlinMultiplatform(evaluated.path)
            }
        }
    }
}
