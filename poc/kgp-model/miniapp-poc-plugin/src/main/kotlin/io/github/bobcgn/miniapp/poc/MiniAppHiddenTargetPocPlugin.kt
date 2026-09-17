package io.github.bobcgn.miniapp.poc

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension

/**
 * PoC-only plugin used to evaluate the "hide the internal Kotlin/JS target name" variant of Model C.
 *
 * It creates the standard `js` target internally and then creates `miniappMain` / `miniappTest` as
 * extra source sets, expecting the JS compilation to consume them. No public Kotlin Gradle Plugin
 * API attaches an arbitrary source set to a compilation, so this is expected to produce source sets
 * that no compilation compiles.
 */
public class MiniAppHiddenTargetPocPlugin : Plugin<Project> {

    override fun apply(target: Project): Unit = with(target) {
        pluginManager.withPlugin(KOTLIN_MULTIPLATFORM_PLUGIN_ID) {
            val kotlin = extensions.getByType(KotlinMultiplatformExtension::class.java)

            kotlin.js(INTERNAL_TARGET_NAME) {
                nodejs()
                useCommonJs()
                binaries.library()
            }

            // The public API can create the source sets and their `dependsOn` edges, but nothing
            // public can hand them to the existing `js` compilation.
            val miniappMain = kotlin.sourceSets.maybeCreate(MINIAPP_MAIN_SOURCE_SET)
            val miniappTest = kotlin.sourceSets.maybeCreate(MINIAPP_TEST_SOURCE_SET)
            kotlin.sourceSets.maybeCreate(COMMON_MAIN_SOURCE_SET).let { miniappMain.dependsOn(it) }
            kotlin.sourceSets.maybeCreate(COMMON_TEST_SOURCE_SET).let { miniappTest.dependsOn(it) }

            kotlin.sourceSets
                .matching { it.name == MINIAPP_TEST_SOURCE_SET || it.name == COMMON_TEST_SOURCE_SET }
                .configureEach { sourceSet ->
                    sourceSet.dependencies {
                        implementation("$KOTLIN_TEST_GROUP:$KOTLIN_TEST_MODULE:$KOTLIN_VERSION")
                    }
                }
        }
    }

    private companion object {
        const val KOTLIN_MULTIPLATFORM_PLUGIN_ID = "org.jetbrains.kotlin.multiplatform"
        const val INTERNAL_TARGET_NAME = "js"
        const val MINIAPP_MAIN_SOURCE_SET = "miniappMain"
        const val MINIAPP_TEST_SOURCE_SET = "miniappTest"
        const val COMMON_MAIN_SOURCE_SET = "commonMain"
        const val COMMON_TEST_SOURCE_SET = "commonTest"
        const val KOTLIN_TEST_GROUP = "org.jetbrains.kotlin"
        const val KOTLIN_TEST_MODULE = "kotlin-test"
        const val KOTLIN_VERSION = "2.4.20"
    }
}
