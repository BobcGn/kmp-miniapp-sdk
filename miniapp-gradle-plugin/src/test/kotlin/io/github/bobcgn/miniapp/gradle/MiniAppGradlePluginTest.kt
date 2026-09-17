package io.github.bobcgn.miniapp.gradle

import org.gradle.testkit.runner.BuildResult
import org.gradle.testkit.runner.GradleRunner
import org.gradle.testkit.runner.TaskOutcome
import java.io.File
import java.util.Properties
import kotlin.io.path.createTempDirectory
import kotlin.test.Test
import kotlin.test.assertContains
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Gradle TestKit coverage for the BOB-76 behaviour: a consumer that applies the Kotlin Multiplatform
 * plugin and the Mini App plugin gets `miniappMain` and `miniappTest` as real, compilation-owned
 * source sets, and `miniappTest` actually runs tests.
 *
 * The fixtures put the Kotlin Gradle Plugin and the plugin under test on the *same* buildscript
 * classpath. That is what a consumer build gives them when it applies both plugins together, and it
 * is required here: `withPluginClasspath()` injects the plugin under test into the plugin-resolution
 * classpath only, so the plugin under test could not see a plugin the fixture resolves separately.
 *
 * The consumer build script deliberately contains no `kotlin { }` target declaration, no
 * `sourceSets.create(...)` and no Kotlin/JS configuration. `assertConsumerScriptDoesNothing` guards
 * that, so a future change cannot make a failing test pass by adding manual wiring to the fixture.
 */
class MiniAppGradlePluginTest {

    private val kotlinVersion: String = checkNotNull(System.getProperty("miniappPlugin.kotlinVersion")) {
        "miniappPlugin.kotlinVersion is not set; miniapp-gradle-plugin/build.gradle.kts sets it."
    }

    @Test
    fun `the plugin provisions miniappMain and miniappTest as compilation-owned source sets`() {
        val projectDir = newFixture("kmp-provisioning")
        writeConsumerBuildScript(projectDir)

        val result = runner(projectDir, "miniAppModelReport").build()
        val report = MiniAppModelReport(result)

        assertEquals(listOf("metadata:common", "miniapp:js"), report.targets)

        assertContains(report.sourceSets, "commonMain")
        assertContains(report.sourceSets, "commonTest")
        assertContains(report.sourceSets, "miniappMain")
        assertContains(report.sourceSets, "miniappTest")

        // Owned by a real compilation, not merely present in the model: a source-set-only
        // registration is the model ADR 0010 rejected.
        assertEquals(listOf("miniapp:main"), report.owningCompilations("miniappMain"))
        assertEquals(listOf("miniapp:test"), report.owningCompilations("miniappTest"))

        // `commonMain -> miniappMain` and `commonTest -> miniappTest`, whether KGP expresses them
        // directly or through the default hierarchy template's grouping source sets.
        assertContains(report.transitiveDependencies("miniappMain"), "commonMain")
        assertContains(report.transitiveDependencies("miniappTest"), "commonTest")

        // The stable task surface of the chosen model.
        assertContains(report.testTasks, MiniAppPluginDiagnostics.MINIAPP_TEST_TASK_NAME)
        assertContains(report.testTasks, MiniAppPluginDiagnostics.MINIAPP_NODE_TEST_TASK_NAME)
    }

    @Test
    fun `miniappTest runs its own tests and the reused commonTest tests`() {
        val projectDir = newFixture("kmp-test-execution")
        writeConsumerBuildScript(projectDir)
        writeConsumerSources(projectDir)
        assertConsumerScriptDoesNothing(projectDir)

        val result = runner(projectDir, "miniappTest").build()

        assertEquals(TaskOutcome.SUCCESS, result.task(":compileKotlinMiniapp")?.outcome)
        assertEquals(TaskOutcome.SUCCESS, result.task(":miniappNodeTest")?.outcome)
        assertEquals(TaskOutcome.SUCCESS, result.task(":miniappTest")?.outcome)

        val executed = executedTests(projectDir)
        // Only in miniappTest.
        assertContains(executed, "miniAppMainIsCompiledAndSeesCommonMain")
        // Declared in commonTest and reused by the miniappTest compilation.
        assertContains(executed, "commonTestIsReusedByTheMiniAppTestCompilation")
        assertEquals(2, executed.size, "expected exactly the two fixture tests, got $executed")
    }

    @Test
    fun `the plugin does not duplicate the target when it is applied twice or the consumer declared it`() {
        val projectDir = newFixture("kmp-existing-target")
        writeConsumerBuildScript(
            projectDir,
            consumerPreamble = """
            // A consumer that declared the platform target before applying the plugin.
            val consumerKotlin = project.extensions.getByType(
                org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension::class.java,
            )
            consumerKotlin.js("miniapp")
            """.trimIndent(),
            consumerEpilogue = """
            // Applying the plugin a second time must not add a second target or source-set pair.
            apply(plugin = "io.github.bobcgn.miniapp")
            """.trimIndent(),
        )

        val result = runner(projectDir, "miniAppModelReport").build()
        val report = MiniAppModelReport(result)

        assertEquals(listOf("miniapp:js"), report.targets.filter { it.startsWith("miniapp") })
        assertEquals(1, report.sourceSets.count { it == "miniappMain" })
        assertEquals(1, report.sourceSets.count { it == "miniappTest" })
        assertEquals(1, report.owningCompilations("miniappMain").size)
        assertEquals(1, report.owningCompilations("miniappTest").size)
        // The plugin still configures the pre-existing target rather than skipping it: without the
        // test run the consumer's `miniappTest` would exist and never execute.
        assertContains(report.testTasks, MiniAppPluginDiagnostics.MINIAPP_TEST_TASK_NAME)
        assertContains(report.testTasks, MiniAppPluginDiagnostics.MINIAPP_NODE_TEST_TASK_NAME)
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

    // --- fixtures -------------------------------------------------------------------------------

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

    private fun writeConsumerBuildScript(
        projectDir: File,
        consumerPreamble: String = "",
        consumerEpilogue: String = "",
    ) {
        // The consumer script is exactly what a consumer would write. The model inspection lives in
        // a separate script, so `assertConsumerScriptDoesNothing` can check the consumer's own words
        // rather than the test harness's.
        projectDir.resolve("inspection.gradle").writeText(INSPECTION_SCRIPT)
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
            $consumerPreamble

            apply(plugin = "io.github.bobcgn.miniapp")
            $consumerEpilogue

            dependencies {
                add("commonTestImplementation", "org.jetbrains.kotlin:kotlin-test:$kotlinVersion")
            }

            apply(from = "inspection.gradle")
            """.trimIndent(),
        )
    }

    private fun writeConsumerSources(projectDir: File) {
        projectDir.resolve("src/commonMain/kotlin/sample/Shared.kt").apply {
            parentFile.mkdirs()
            writeText(
                """
                package sample

                fun sharedGreeting(): String = "hello from commonMain"
                """.trimIndent() + "\n",
            )
        }
        projectDir.resolve("src/miniappMain/kotlin/sample/MiniAppOnly.kt").apply {
            parentFile.mkdirs()
            writeText(
                """
                package sample

                // Compiles only when miniappMain can see commonMain.
                fun miniAppGreeting(): String = "miniapp:" + sharedGreeting()
                """.trimIndent() + "\n",
            )
        }
        projectDir.resolve("src/commonTest/kotlin/sample/SharedTest.kt").apply {
            parentFile.mkdirs()
            writeText(
                """
                package sample

                import kotlin.test.Test
                import kotlin.test.assertEquals

                class SharedTest {
                    @Test
                    fun commonTestIsReusedByTheMiniAppTestCompilation() {
                        assertEquals("hello from commonMain", sharedGreeting())
                    }
                }
                """.trimIndent() + "\n",
            )
        }
        projectDir.resolve("src/miniappTest/kotlin/sample/MiniAppTest.kt").apply {
            parentFile.mkdirs()
            writeText(
                """
                package sample

                import kotlin.test.Test
                import kotlin.test.assertEquals

                class MiniAppTest {
                    @Test
                    fun miniAppMainIsCompiledAndSeesCommonMain() {
                        assertEquals("miniapp:hello from commonMain", miniAppGreeting())
                    }
                }
                """.trimIndent() + "\n",
            )
        }
    }

    private fun assertConsumerScriptDoesNothing(projectDir: File) {
        val script = projectDir.resolve("build.gradle.kts").readText()
        listOf("sourceSets", "js(", "js {", "nodejs(", "useCommonJs", "binaries.library").forEach { token ->
            assertTrue(
                !script.contains(token),
                "the consumer build script must not contain '$token'; the plugin owns that wiring",
            )
        }
    }

    // --- helpers --------------------------------------------------------------------------------

    private fun executedTests(projectDir: File): List<String> {
        val resultsDir = projectDir.resolve("build/test-results/miniappNodeTest")
        assertTrue(resultsDir.isDirectory, "no miniappNodeTest results at $resultsDir")
        return resultsDir.listFiles { file -> file.extension == "xml" }
            ?.flatMap { file ->
                Regex("""<testcase name="([^"]+)"""")
                    .findAll(file.readText())
                    .map { it.groupValues[1].substringBefore('[') }
                    .toList()
            }
            .orEmpty()
            .sorted()
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

    private class MiniAppModelReport(result: BuildResult) {
        private val lines = result.output.lineSequence().toList()

        val targets: List<String> = valuesAfter("miniapp.targets=")
        val sourceSets: List<String> = valuesAfter("miniapp.sourceSets=")
        val testTasks: List<String> = valuesAfter("miniapp.testTasks=")

        fun owningCompilations(sourceSet: String): List<String> =
            valuesAfter("miniapp.$sourceSet.owningCompilations=")

        fun transitiveDependencies(sourceSet: String): List<String> =
            valuesAfter("miniapp.$sourceSet.transitiveDependsOn=")

        private fun valuesAfter(prefix: String): List<String> {
            val line = lines.firstOrNull { it.startsWith(prefix) }
                ?: error("no line starting with '$prefix' in:\n${lines.joinToString("\n")}")
            return line.removePrefix(prefix).removeSurrounding("[", "]")
                .split(", ")
                .filter { it.isNotBlank() }
        }
    }

    private companion object {
        /**
         * Written into each fixture as `inspection.gradle` and applied from the consumer script, so
         * the consumer's own build file stays free of anything the plugin is supposed to own. Groovy
         * rather than Kotlin DSL on purpose: a script plugin compiled against the target project's
         * classpath does not need the Kotlin Gradle Plugin at compilation time.
         */
        val INSPECTION_SCRIPT: String = """
            tasks.register("miniAppModelReport") {
                doLast {
                    def kotlin = project.extensions.getByName("kotlin")
                    println("miniapp.targets=" + kotlin.targets.collect { it.name + ":" + it.platformType }.sort())
                    println("miniapp.sourceSets=" + kotlin.sourceSets.collect { it.name }.sort())
                    ["miniappMain", "miniappTest"].each { name ->
                        def sourceSet = kotlin.sourceSets.findByName(name)
                        def direct = sourceSet == null ? [] : sourceSet.dependsOn.collect { it.name }.sort()
                        def seen = [] as Set
                        def queue = new ArrayDeque(sourceSet == null ? [] : sourceSet.dependsOn)
                        while (!queue.isEmpty()) {
                            def next = queue.poll()
                            if (seen.add(next.name)) {
                                queue.addAll(next.dependsOn)
                            }
                        }
                        def owning = kotlin.targets.collectMany { target ->
                            target.compilations.findAll { compilation ->
                                compilation.allKotlinSourceSets.any { it.name == name }
                            }.collect { target.name + ":" + it.name }
                        }.sort()
                        println("miniapp." + name + ".owningCompilations=" + owning)
                        println("miniapp." + name + ".directDependsOn=" + direct)
                        println("miniapp." + name + ".transitiveDependsOn=" + seen.toList().sort())
                    }
                    println(
                        "miniapp.testTasks=" +
                            project.tasks.names.findAll { it == "miniappTest" || it == "miniappNodeTest" }.sort()
                    )
                }
            }
        """.trimIndent()
    }
}
