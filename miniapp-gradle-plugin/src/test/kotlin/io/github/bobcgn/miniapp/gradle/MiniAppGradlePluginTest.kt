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

    private val sdkRepository: File = File(
        checkNotNull(System.getProperty("miniappPlugin.sdkRepository")) {
            "miniappPlugin.sdkRepository is not set; miniapp-gradle-plugin/build.gradle.kts sets it."
        },
    )

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
        // Only in miniappTest, and it calls the runtime SDK's public API.
        assertContains(executed, "miniAppMainIsCompiledAndSeesCommonMain")
        assertContains(executed, "theMiniAppTestCompilationSeesTheRuntimeSdk")
        // Declared in commonTest and reused by the miniappTest compilation.
        assertContains(executed, "commonTestIsReusedByTheMiniAppTestCompilation")
        assertEquals(3, executed.size, "expected exactly the three fixture tests, got $executed")
    }

    @Test
    fun `the plugin wires the runtime SDK into miniappMain and nowhere else`() {
        val projectDir = newFixture("kmp-dependency-wiring")
        writeConsumerBuildScript(projectDir)
        writeConsumerSources(projectDir)
        assertConsumerScriptDeclaresNoRuntime(projectDir)

        val result = runner(projectDir, "miniAppModelReport").build()
        val report = MiniAppModelReport(result)

        // Declared exactly once, on miniappMain's api configuration. miniappTest reaches it through
        // the source-set hierarchy rather than through a second declaration that could resolve
        // differently.
        assertEquals(listOf("miniappMainApi"), report.configurationsDeclaring(MiniAppRuntimeDependency.coordinate))

        assertContains(report.compileClasspaths, "miniappCompileClasspath")
        assertContains(report.compileClasspaths, "miniappTestCompileClasspath")
        assertContains(report.compileClasspaths, "metadataCompileClasspath")

        // Resolved through the composite build rather than from a repository, because nothing is
        // published yet; the coordinate is the same one publication would use.
        assertEquals(
            1,
            report.classpathEntries("miniappCompileClasspath").count {
                it.contains(MiniAppRuntimeDependency.name)
            },
            "miniappMain must compile against exactly one runtime SDK and no ambiguous variant",
        )
        assertTrue(
            report.classpathEntries("miniappTestCompileClasspath").any {
                it.contains(MiniAppRuntimeDependency.name)
            },
            "miniappTest must inherit the runtime SDK through the source-set hierarchy",
        )
        assertEquals(
            emptyList(),
            report.classpathEntries("metadataCompileClasspath").filter {
                it.contains(MiniAppRuntimeDependency.name)
            },
            "the common/metadata compilation must not receive the Mini App runtime",
        )
    }

    @Test
    fun `the runtime is not injected into another target`() {
        val projectDir = newFixture("kmp-other-target")
        writeConsumerBuildScript(
            projectDir,
            consumerEpilogue = """
            // A consumer that also targets the JVM. The Mini App runtime is a Kotlin/JS artifact, so
            // a leak into commonMain would break this compilation rather than quietly mis-resolve.
            project.extensions.getByType(
                org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension::class.java,
            ).jvm()
            """.trimIndent(),
        )
        writeConsumerSources(projectDir)

        val result = runner(projectDir, "compileKotlinJvm", "miniAppModelReport").build()
        val report = MiniAppModelReport(result)

        assertEquals(TaskOutcome.SUCCESS, result.task(":compileKotlinJvm")?.outcome)
        assertEquals(
            emptyList(),
            report.classpathEntries("jvmCompileClasspath").filter {
                it.contains(MiniAppRuntimeDependency.name)
            },
            "the JVM compilation must not receive the Mini App runtime",
        )
        assertEquals(listOf("miniappMainApi"), report.configurationsDeclaring(MiniAppRuntimeDependency.coordinate))
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
    fun `a missing runtime fails with a resolution error naming the coordinate`() {
        val projectDir = newFixture("kmp-missing-runtime", includeSdkBuild = false)
        writeConsumerBuildScript(projectDir)
        writeConsumerSources(projectDir)

        val result = runner(projectDir, "compileKotlinMiniapp").buildAndFail()

        assertContains(result.output, MiniAppRuntimeDependency.coordinate)
        assertTrue(
            result.output.contains("Could not find") || result.output.contains("Could not resolve"),
            "expected a dependency-resolution failure, got:\n${result.output}",
        )
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

    private fun newFixture(name: String, includeSdkBuild: Boolean = true): File {
        val projectDir = createTempDirectory(name).toFile()
        val sdkBuild = if (includeSdkBuild) {
            """
            // The fixture consumes the Mini App runtime as a public module coordinate. Nothing is
            // published yet, so the coordinate resolves through this composite build; after
            // publication it resolves from a repository with the same group, name and version.
            includeBuild("${sdkRepository.invariantSeparatorsPath}")
            """.trimIndent()
        } else {
            ""
        }
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

            $sdkBuild

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

                import io.github.bobcgn.miniapp.api.MiniAppSdk

                // Compiles only when miniappMain can see commonMain.
                fun miniAppGreeting(): String = "miniapp:" + sharedGreeting()

                // Compiles only when the plugin wired the runtime SDK into miniappMain.
                fun miniAppRuntimeVersion(): String = MiniAppSdk.VERSION
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

                import io.github.bobcgn.miniapp.api.MiniAppSdk
                import kotlin.test.Test
                import kotlin.test.assertEquals
                import kotlin.test.assertTrue

                class MiniAppTest {
                    @Test
                    fun miniAppMainIsCompiledAndSeesCommonMain() {
                        assertEquals("miniapp:hello from commonMain", miniAppGreeting())
                    }

                    @Test
                    fun theMiniAppTestCompilationSeesTheRuntimeSdk() {
                        assertTrue(MiniAppSdk.VERSION.isNotEmpty())
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

    /**
     * The consumer says nothing about the Mini App runtime: no coordinate, no project path, no
     * repository path, no generated directory, and no configuration name. Everything below comes
     * from the plugin, so the same build works against a published artifact.
     */
    private fun assertConsumerScriptDeclaresNoRuntime(projectDir: File) {
        val script = projectDir.resolve("build.gradle.kts").readText()
        listOf(
            MiniAppRuntimeDependency.coordinate,
            "io.github.bobcgn.miniapp.api",
            "project(\":sdk\")",
            "project(\":kmp-miniapp-sdk\")",
            "build/generated",
            "miniappMainApi",
            "miniappMainImplementation",
        ).forEach { token ->
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
        val compileClasspaths: List<String> = valuesAfter("miniapp.compileClasspaths=")

        fun owningCompilations(sourceSet: String): List<String> =
            valuesAfter("miniapp.$sourceSet.owningCompilations=")

        fun transitiveDependencies(sourceSet: String): List<String> =
            valuesAfter("miniapp.$sourceSet.transitiveDependsOn=")

        /** Configurations that declare exactly this module coordinate. */
        fun configurationsDeclaring(coordinate: String): List<String> =
            valuesAfter("miniapp.declaredDependencies=")
                .filter { it.endsWith(coordinate) }
                .map { it.substringBefore('=') }

        fun classpathEntries(configurationName: String): List<String> =
            valuesAfter("miniapp.classpath.$configurationName=")

        fun valuesAfter(prefix: String): List<String> {
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
                    // The fixture reports facts only; it never states the runtime coordinate, so the
                    // assertions are about what the plugin published to the consumer rather than
                    // about a name this harness copied down.
                    def declaredDependencies = project.configurations.collectMany { configuration ->
                        configuration.dependencies
                            .findAll { dependency -> dependency.group != null }
                            .collect { configuration.name + "=" + it.group + ":" + it.name + ":" + it.version }
                    }.sort()
                    println("miniapp.declaredDependencies=" + declaredDependencies)
                    def compileClasspaths = project.configurations.names
                        .findAll { it.toLowerCase().contains("compileclasspath") }
                        .sort()
                    println("miniapp.compileClasspaths=" + compileClasspaths)
                    compileClasspaths.each { configurationName ->
                        def resolved = project.configurations.getByName(configurationName)
                            .incoming.resolutionResult.allComponents
                            .collect { it.id.displayName }
                            .sort()
                        println("miniapp.classpath." + configurationName + "=" + resolved)
                    }
                }
            }
        """.trimIndent()
    }
}
