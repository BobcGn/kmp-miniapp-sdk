package io.github.bobcgn.miniapp.gradle

import org.gradle.testkit.runner.BuildResult
import org.gradle.testkit.runner.GradleRunner
import java.io.File
import java.util.Properties
import kotlin.io.path.createTempDirectory
import kotlin.test.Test
import kotlin.test.assertContains
import kotlin.test.assertEquals

/**
 * Gradle TestKit coverage for the BOB-78 skeleton: an ordinary Kotlin Multiplatform project can
 * apply the plugin and get the Mini App platform target, and a project without the Kotlin
 * Multiplatform plugin fails with a message naming the missing plugin.
 *
 * Both fixtures put the Kotlin Gradle Plugin and the plugin under test on the *same* buildscript
 * classpath. That is what a consumer build gives them when it applies both plugins together, and it
 * is required here: `withPluginClasspath()` injects the plugin under test into the plugin-resolution
 * classpath only, so the plugin under test could not see a plugin the fixture resolves separately.
 */
class MiniAppGradlePluginTest {

    private val kotlinVersion: String = checkNotNull(System.getProperty("miniappPlugin.kotlinVersion")) {
        "miniappPlugin.kotlinVersion is not set; miniapp-gradle-plugin/build.gradle.kts sets it."
    }

    @Test
    fun `an ordinary Kotlin Multiplatform project can apply the plugin`() {
        val projectDir = newFixture("kmp-project")
        projectDir.resolve("build.gradle.kts").writeText(
            """
            buildscript {
                repositories {
                    gradlePluginPortal()
                    mavenCentral()
                }
                dependencies {
                    classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:$kotlinVersion")
                    classpath(${pluginUnderTestClasspathExpression()})
                }
            }

            apply(plugin = "org.jetbrains.kotlin.multiplatform")
            apply(plugin = "io.github.bobcgn.miniapp")

            tasks.register("miniAppModelReport") {
                doLast {
                    val kotlin = project.extensions.getByType(
                        org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension::class.java,
                    )
                    println("miniapp.sourceSets=" + kotlin.sourceSets.names.sorted())
                    val owning = kotlin.targets.flatMap { target ->
                        target.compilations
                            .filter { compilation ->
                                compilation.allKotlinSourceSets.any { sourceSet -> sourceSet.name == "miniappMain" }
                            }
                            .map { compilation -> target.name + ":" + compilation.name }
                    }
                    println("miniapp.miniappMain.owningCompilations=" + owning.sorted())
                }
            }
            """.trimIndent(),
        )

        val result = runner(projectDir, "miniAppModelReport").build()

        val sourceSets = result.lineAfter("miniapp.sourceSets=")
        assertContains(sourceSets, "commonMain")
        assertContains(sourceSets, "commonTest")
        assertContains(sourceSets, "miniappMain")
        assertContains(sourceSets, "miniappTest")

        // The source set must be owned by a real compilation, not merely present in the model:
        // a source-set-only registration is the model ADR 0010 rejects.
        assertEquals("[miniapp:main]", result.lineAfter("miniapp.miniappMain.owningCompilations="))
    }

    @Test
    fun `a project without the Kotlin Multiplatform plugin fails with a clear message`() {
        val projectDir = newFixture("missing-kmp-project")
        projectDir.resolve("build.gradle.kts").writeText(
            """
            buildscript {
                dependencies {
                    classpath(${pluginUnderTestClasspathExpression()})
                }
            }

            apply(plugin = "io.github.bobcgn.miniapp")
            """.trimIndent(),
        )

        val result = runner(projectDir, "help").buildAndFail()

        assertContains(result.output, MiniAppPluginDiagnostics.MISSING_KOTLIN_MULTIPLATFORM_MESSAGE)
        assertContains(result.output, "Project ':' does not apply 'org.jetbrains.kotlin.multiplatform'.")
        assertContains(result.output, "id(\"io.github.bobcgn.miniapp\")")
    }

    private fun newFixture(name: String): File {
        val projectDir = createTempDirectory(name).toFile()
        projectDir.resolve("settings.gradle.kts").writeText(
            """
            pluginManagement {
                repositories {
                    gradlePluginPortal()
                    mavenCentral()
                }
            }

            dependencyResolutionManagement {
                repositories {
                    mavenCentral()
                }
            }

            rootProject.name = "$name"
            """.trimIndent(),
        )
        return projectDir
    }

    /**
     * Renders the plugin-under-test classpath as a Kotlin `files(...)` expression, so a fixture can
     * put the plugin on its buildscript classpath explicitly.
     */
    private fun pluginUnderTestClasspathExpression(): String {
        val metadata = checkNotNull(
            javaClass.classLoader.getResourceAsStream("plugin-under-test-metadata.properties"),
        ) { "plugin-under-test-metadata.properties is missing from the test classpath" }

        val properties = Properties().apply { metadata.use { load(it) } }
        val entries = properties.getProperty("implementation-classpath")
            .split(File.pathSeparator)
            .filter { it.isNotBlank() }

        return entries.joinToString(prefix = "files(", postfix = ")") { entry ->
            "\"${entry.replace("\\", "\\\\")}\""
        }
    }

    private fun runner(projectDir: File, vararg arguments: String): GradleRunner =
        GradleRunner.create()
            .withProjectDir(projectDir)
            .withArguments(*arguments)

    private fun BuildResult.lineAfter(prefix: String): String =
        output.lineSequence()
            .firstOrNull { it.startsWith(prefix) }
            ?.removePrefix(prefix)
            ?: error("no line starting with '$prefix' in:\n$output")
}
