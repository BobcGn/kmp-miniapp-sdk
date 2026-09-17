package io.github.bobcgn.miniapp.poc

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension

/**
 * PoC-only plugin used to evaluate Model C of BOB-83.
 *
 * It owns the Kotlin/JS compilation so the consumer never writes `js { }`, `useCommonJs()`,
 * `nodejs()`, `binaries.library()`, `generateTypeScriptDefinitions()` or `sourceSets.create(...)`.
 *
 * This is NOT the production `io.github.bobcgn.miniapp` plugin: it has no WeChat host
 * configuration, no SDK dependency wiring and no artifact assembly.
 */
public class MiniAppPocPlugin : Plugin<Project> {

    override fun apply(target: Project): Unit = with(target) {
        pluginManager.withPlugin(KOTLIN_MULTIPLATFORM_PLUGIN_ID) {
            configureMiniAppTarget(this)
        }
        afterEvaluate {
            require(pluginManager.hasPlugin(KOTLIN_MULTIPLATFORM_PLUGIN_ID)) {
                "The '$PLUGIN_ID' plugin requires the Kotlin Multiplatform plugin. " +
                    "Apply 'org.jetbrains.kotlin.multiplatform' to this project."
            }
        }
    }

    private fun configureMiniAppTarget(project: Project) {
        val kotlin = project.extensions.getByType(KotlinMultiplatformExtension::class.java)

        // Naming the Kotlin/JS target after the platform is what makes KGP create real
        // KotlinSourceSets called 'miniappMain' / 'miniappTest' instead of 'jsMain' / 'jsTest'.
        kotlin.js(MINIAPP_TARGET_NAME) {
            nodejs()
            useCommonJs()
            binaries.library()
            generateTypeScriptDefinitions()
        }

        kotlin.sourceSets
            .matching { it.name == MINIAPP_TEST_SOURCE_SET || it.name == COMMON_TEST_SOURCE_SET }
            .configureEach { sourceSet ->
                sourceSet.dependencies {
                    implementation("$KOTLIN_TEST_GROUP:$KOTLIN_TEST_MODULE:$KOTLIN_VERSION")
                }
            }
    }

    private companion object {
        const val PLUGIN_ID = "io.github.bobcgn.miniapp.poc"
        const val KOTLIN_MULTIPLATFORM_PLUGIN_ID = "org.jetbrains.kotlin.multiplatform"

        const val MINIAPP_TARGET_NAME = "miniapp"
        const val MINIAPP_TEST_SOURCE_SET = "miniappTest"
        const val COMMON_TEST_SOURCE_SET = "commonTest"

        const val KOTLIN_TEST_GROUP = "org.jetbrains.kotlin"
        const val KOTLIN_TEST_MODULE = "kotlin-test"
        // The production plugin resolves this from the applied KGP version instead.
        const val KOTLIN_VERSION = "2.4.20"
    }
}
