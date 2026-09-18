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
    fun `the plugin exposes a stable assembly task and the bundle matches its contract`() {
        val projectDir = newFixture("kmp-bundle")
        writeConsumerBuildScript(projectDir)
        writeConsumerSources(projectDir)
        assertConsumerScriptDoesNothing(projectDir)

        val result = runner(
            projectDir,
            MiniAppPluginDiagnostics.ASSEMBLE_MINIAPP_BUNDLE_TASK_NAME,
            "miniAppModelReport",
        ).build()
        val report = MiniAppModelReport(result)

        assertEquals(
            TaskOutcome.SUCCESS,
            result.task(":${MiniAppPluginDiagnostics.ASSEMBLE_MINIAPP_BUNDLE_TASK_NAME}")?.outcome,
        )

        val bundleDir = projectDir.resolve("build/${MiniAppPluginDiagnostics.MINIAPP_BUNDLE_DIRECTORY}")
        assertTrue(bundleDir.isDirectory, "expected the bundle at $bundleDir")
        val bundle = bundleDir.listFiles()!!.map { it.name }.sorted()

        // Exactly one declaration identifies the consumer's own module; everything else is runtime.
        val declarations = bundle.filter { it.endsWith(".d.ts") }
        assertEquals(1, declarations.size, "expected one TypeScript declaration in $bundle")
        val moduleName = declarations.single().removeSuffix(".d.ts")
        assertContains(bundle, "$moduleName.js")

        // The Kotlin runtime travels with the module.
        assertContains(bundle, "kotlin-kotlin-stdlib.js")
        assertContains(bundle, "kotlinx-coroutines-core.js")
        assertContains(bundle, "kotlinx-atomicfu.js")
        assertContains(bundle, "package.json")

        // The runtime SDK is a separate module in the bundle, not inlined into the consumer's, so a
        // host must load it too; the consumer's entry module requires it by relative path.
        val sdkModule = "${MiniAppRuntimeDependency.name}-kotlin.js"
        assertContains(bundle, sdkModule)
        val moduleJs = bundleDir.resolve("$moduleName.js").readText()
        assertContains(moduleJs, "./$sdkModule")

        // The bundle really carries the consumer's own code and its host-facing exports...
        assertContains(moduleJs, "hello from commonMain")
        assertContains(moduleJs, "sample")
        assertContains(moduleJs, "miniAppGreeting")
        // ...and the SDK runtime it calls.
        assertContains(moduleJs, MiniAppRuntimeDependency.version)

        // Conclusion 1: the plugin generates no host markup. This says nothing about renderers.
        assertTrue(
            bundle.none { it.endsWith(".wxml") || it.endsWith(".wxss") },
            "the SDK must not generate host markup, found: $bundle",
        )

        // Conclusion 2, kept separate on purpose: the bundle carries no renderer. That is a statement
        // about the dependency graph, not about the file list, so it is asserted where it is true —
        // the host-boundary check passed on the classpath this bundle was built from.
        assertEquals(
            TaskOutcome.SUCCESS,
            result.task(":${MiniAppPluginDiagnostics.CHECK_MINIAPP_HOST_BOUNDARY_TASK_NAME}")?.outcome,
        )
        // The boundary check is what makes the assembly safe to trust, so it must run first. The edge
        // is asserted here because reproducing a leaky assembly would drag the npm install into a
        // test that is about the dependency graph.
        assertContains(report.valuesAfter("miniapp.bundleDependencies="), MiniAppPluginDiagnostics.CHECK_MINIAPP_HOST_BOUNDARY_TASK_NAME)
    }

    /**
     * Compose UI drags its renderer, Skiko, in transitively, so one build covers the forbidden-group
     * rule for both — and shows the shared primitives the same graph carries are not misfiled.
     */
    @Test
    fun `a client renderer on the Mini App runtime classpath is rejected`() {
        val output = boundaryCheckFailure("org.jetbrains.compose.runtime:runtime:1.7.0")
        val offenders = offendersIn(output)

        // The whole Compose group is rejected, including the modules Compose pulls in itself.
        assertContains(offenders, "org.jetbrains.compose.runtime:runtime")
        assertTrue(
            offenders.all { it.startsWith("org.jetbrains.compose") || it.startsWith("org.jetbrains.skiko") },
            "only renderer coordinates may be reported, got: $offenders",
        )
        // The same graph carries the shared runtime a Mini App legitimately uses, and the check must
        // not misfire on it. This is the integration-level half of the classifier's allow-list.
        listOf("kotlinx-coroutines-core", "atomicfu", "kotlin-stdlib", "kotlin-dom-api-compat", "kmp-miniapp-sdk")
            .forEach { shared ->
                assertTrue(
                    offenders.none { it.contains(shared) },
                    "'$shared' is shared runtime and must not be reported: $offenders",
                )
            }
        assertContains(output, "Compose is a client UI consumer")
        assertContains(output, MiniAppHostBoundary.MESSAGE_HEADER)
    }

    /**
     * A classpath the check could not resolve is refused rather than read.
     *
     * This is the silent pass the check exists to prevent: Gradle's resolution result lists only the
     * dependencies that resolved, so a check that reads it alone reports "no renderer" for a graph
     * it never saw.
     *
     * The fixture names a coordinate that cannot exist, rather than a real artifact that happens to
     * be unresolvable today. That distinction is the point: an earlier version of this test used a
     * published coordinate whose variant did not match a Kotlin/JS compilation, which made the test
     * depend on the state of a third-party repository instead of on the plugin.
     */
    @Test
    fun `the boundary check refuses a classpath it could not resolve`() {
        val output = boundaryCheckFailure("org.example.nowhere:not-a-real-module:1.0")

        assertContains(output, MiniAppHostBoundary.UNRESOLVED_HEADER)
        assertContains(output, "org.example.nowhere:not-a-real-module:1.0")
        assertTrue(
            !output.contains(MiniAppHostBoundary.MESSAGE_HEADER),
            "an unresolved classpath must not be reported as a renderer finding:\n$output",
        )
    }

    /**
     * The boundary check is about the dependency graph, not about the consumer's own code, so a
     * classpath carrying only shared runtime passes it — and the assertion is not vacuous, because
     * the same fixture reports the classpath the check read.
     */
    @Test
    fun `a classpath carrying only shared runtime passes the boundary check`() {
        val projectDir = newFixture("kmp-shared-runtime")
        writeConsumerBuildScript(
            projectDir,
            consumerEpilogue = consumerDependency("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.11.0"),
        )
        writeConsumerSources(projectDir)

        val task = ":${MiniAppPluginDiagnostics.CHECK_MINIAPP_HOST_BOUNDARY_TASK_NAME}"
        val result = runner(projectDir, MiniAppPluginDiagnostics.CHECK_MINIAPP_HOST_BOUNDARY_TASK_NAME, "miniAppModelReport").build()
        val report = MiniAppModelReport(result)

        assertEquals(TaskOutcome.SUCCESS, result.task(task)?.outcome)
        listOf("kotlinx-coroutines-core", MiniAppRuntimeDependency.name).forEach { shared ->
            assertTrue(
                report.runtimeClasspath.any { it.contains(shared) },
                "the check passed, so '$shared' must really be on the classpath it read: ${report.runtimeClasspath}",
            )
        }
    }

    @Test
    fun `the canonical miniapp wechat DSL compiles and executes`() {
        // The Kotlin fixtures drive the extension through `configure<...>` because they apply the
        // plugin from the buildscript classpath, which does not generate type-safe accessors. A
        // consumer applies the plugin in the `plugins { }` block and gets them, so the bare
        // `miniapp { }` syntax is checked here, in Groovy, where it needs no accessor at all.
        val projectDir = newFixture("kmp-groovy-dsl")
        projectDir.resolve("build.gradle").writeText(
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

            apply plugin: 'org.jetbrains.kotlin.multiplatform'
            apply plugin: 'io.github.bobcgn.miniapp'

            miniapp {
                wechat {
                    bundleDirectory.set(layout.buildDirectory.dir('wechat-host-bundle'))
                }
            }

            tasks.register('reportBundleDirectory') {
                doLast {
                    def extension = project.extensions.getByName('miniapp')
                    println('miniapp.bundleDirectory=' + extension.wechat.bundleDirectory.get().asFile.name)
                }
            }
            """.trimIndent(),
        )

        val result = runner(projectDir, "reportBundleDirectory").build()

        assertContains(result.output, "miniapp.bundleDirectory=wechat-host-bundle")
    }

    @Test
    fun `the extension configures where the bundle is written`() {
        val projectDir = newFixture("kmp-extension")
        writeConsumerBuildScript(
            projectDir,
            consumerEpilogue = "configure<io.github.bobcgn.miniapp.gradle.MiniAppExtension> {\n" +
                "    wechat {\n" +
                "        bundleDirectory.set(project.layout.buildDirectory.dir(\"host-bundle\"))\n" +
                "    }\n" +
                "}",
        )
        writeConsumerSources(projectDir)

        val result = runner(projectDir, MiniAppPluginDiagnostics.ASSEMBLE_MINIAPP_BUNDLE_TASK_NAME).build()

        assertEquals(
            TaskOutcome.SUCCESS,
            result.task(":${MiniAppPluginDiagnostics.ASSEMBLE_MINIAPP_BUNDLE_TASK_NAME}")?.outcome,
        )
        val configured = projectDir.resolve("build/host-bundle")
        assertTrue(configured.isDirectory, "expected the bundle at the configured directory")
        val bundle = configured.listFiles()!!.map { it.name }
        assertTrue(bundle.any { it.endsWith(".d.ts") }, "expected a declaration in $bundle")
        assertTrue(bundle.any { it == "kotlin-kotlin-stdlib.js" }, "expected the runtime in $bundle")
        // The default location must not also be written.
        assertTrue(
            !projectDir.resolve("build/${MiniAppPluginDiagnostics.MINIAPP_BUNDLE_DIRECTORY}").exists(),
            "the default bundle directory must not be used when one is configured",
        )
    }

    @Test
    fun `a bundle directory outside the project fails with an actionable error`() {
        val projectDir = newFixture("kmp-extension-invalid")
        writeConsumerBuildScript(
            projectDir,
            consumerEpilogue = "configure<io.github.bobcgn.miniapp.gradle.MiniAppExtension> {\n" +
                "    wechat {\n" +
                "        bundleDirectory.set(project.layout.projectDirectory.dir(\"../\" + project.name + \"-outside-bundle\"))\n" +
                "    }\n" +
                "}",
        )
        writeConsumerSources(projectDir)

        val result = runner(projectDir, MiniAppPluginDiagnostics.ASSEMBLE_MINIAPP_BUNDLE_TASK_NAME).buildAndFail()

        assertContains(result.output, MiniAppBundleDirectoryDiagnostics.MESSAGE_HEADER)
        assertContains(result.output, "configured:")
        assertContains(result.output, "bundleDirectory.set")
        // Nothing outside the project may be created by a rejected configuration.
        assertTrue(
            !projectDir.parentFile.resolve("${projectDir.name}-outside-bundle").exists(),
            "a rejected configuration must not have written anything",
        )
    }

    @Test
    fun `applying the plugin twice does not create a second extension`() {
        val projectDir = newFixture("kmp-extension-idempotent")
        writeConsumerBuildScript(
            projectDir,
            consumerEpilogue = "apply(plugin = \"io.github.bobcgn.miniapp\")",
        )

        // The build succeeding at all is the idempotency evidence: Gradle refuses a second
        // extension with the same name, so a duplicate registration would have failed the build.
        val report = MiniAppModelReport(runner(projectDir, "miniAppModelReport").build())

        assertContains(report.valuesAfter("miniapp.extensionType=").single(), "MiniAppExtension")
    }

    @Test
    fun `the assembly task is up to date on a repeat run`() {
        val projectDir = newFixture("kmp-bundle-incremental")
        writeConsumerBuildScript(projectDir)
        writeConsumerSources(projectDir)

        val task = ":${MiniAppPluginDiagnostics.ASSEMBLE_MINIAPP_BUNDLE_TASK_NAME}"
        assertEquals(TaskOutcome.SUCCESS, runner(projectDir, task).build().task(task)?.outcome)
        assertEquals(TaskOutcome.UP_TO_DATE, runner(projectDir, task).build().task(task)?.outcome)
    }

    @Test
    fun `the assembly task fails on the compilation when the consumer code does not compile`() {
        val projectDir = newFixture("kmp-bundle-broken")
        writeConsumerBuildScript(projectDir)
        writeConsumerSources(projectDir)
        projectDir.resolve("src/miniappMain/kotlin/sample/Broken.kt").apply {
            parentFile.mkdirs()
            writeText(
                """
                package sample

                fun broken(): String = undefinedSymbol()
                """.trimIndent() + "\n",
            )
        }

        val result = runner(projectDir, MiniAppPluginDiagnostics.ASSEMBLE_MINIAPP_BUNDLE_TASK_NAME).buildAndFail()

        assertContains(result.output, "compileKotlinMiniapp")
        assertContains(result.output, "undefinedSymbol")
    }

    @Test
    fun `the assembly task works under the configuration cache`() {
        val projectDir = newFixture("kmp-bundle-configuration-cache")
        writeConsumerBuildScript(projectDir, includeInspection = false)
        writeConsumerSources(projectDir)

        val task = MiniAppPluginDiagnostics.ASSEMBLE_MINIAPP_BUNDLE_TASK_NAME
        runner(projectDir, task, "--configuration-cache").build()
        val second = runner(projectDir, task, "--configuration-cache").build()

        assertContains(second.output, "Reusing configuration cache")
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

    /**
     * The plugin defers a target-name collision to the Kotlin Gradle Plugin rather than replacing
     * what the consumer declared: a project that already owns the name for another platform fails
     * during plugin application, with Kotlin's own diagnostic, instead of acquiring a second target,
     * a half-configured build, or a platform silently swapped underneath it.
     */
    @Test
    fun `a target already using the platform name for another platform fails`() {
        val projectDir = newFixture("kmp-incompatible-target")
        writeConsumerBuildScript(
            projectDir,
            consumerPreamble = """
            val consumerKotlin = project.extensions.getByType(
                org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension::class.java,
            )
            consumerKotlin.jvm("miniapp")
            """.trimIndent(),
        )
        writeConsumerSources(projectDir)

        val result = runner(projectDir, "miniAppModelReport").buildAndFail()

        assertContains(result.output, "Failed to apply plugin 'io.github.bobcgn.miniapp'")
        assertContains(result.output, "The target 'miniapp' already exists")
        assertContains(result.output, "not created with the 'js' preset")
    }

    /**
     * BOB-83 recorded the cross-module constraint and left its diagnosis to this issue: a consumer
     * whose `commonMain` depends on another Kotlin Multiplatform project needs that project to offer
     * a Mini App variant too. Nothing may quietly substitute a different variant or move the
     * dependency out of `commonMain`, so the failure has to be a resolution failure that names what
     * the consumer must change.
     */
    @Test
    fun `a shared dependency without a Mini App variant fails variant resolution`() {
        val projectDir = newFixture("kmp-shared-module")
        writeConsumerBuildScript(
            projectDir,
            consumerEpilogue = """
            dependencies {
                add("commonMainImplementation", project(":shared"))
            }
            """.trimIndent(),
        )
        writeConsumerSources(projectDir)
        projectDir.resolve("settings.gradle.kts").appendText("\ninclude(\":shared\")\n")
        // A Kotlin Multiplatform project that does not apply the Mini App plugin, so it offers no
        // Mini App variant: the case BOB-83 recorded and left to this issue.
        projectDir.resolve("shared/build.gradle.kts").apply {
            parentFile.mkdirs()
            writeText(
                """
                apply(plugin = "org.jetbrains.kotlin.multiplatform")

                project.extensions.getByType(
                    org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension::class.java,
                ).jvm()
                """.trimIndent() + "\n",
            )
        }
        projectDir.resolve("shared/src/commonMain/kotlin/shared/Shared.kt").apply {
            parentFile.mkdirs()
            writeText("package shared\n\nfun sharedValue(): String = \"shared\"\n")
        }

        val result = runner(projectDir, "compileKotlinMiniapp").buildAndFail()

        // A resolution failure, not a silent substitution and not an empty classpath: the consumer
        // is told which project offers no Mini App variant and what attribute it was missing. It
        // fails before the compilation runs, which is why no task outcome is asserted here.
        assertContains(result.output, "Could not resolve project :shared")
        assertContains(result.output, "miniappCompileClasspath")
        assertContains(result.output, "No matching variant of project :shared was found")
        assertContains(result.output, "org.jetbrains.kotlin.platform.type")
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
        includeInspection: Boolean = true,
    ) {
        // The consumer script is exactly what a consumer would write. The model inspection lives in
        // a separate script, so `assertConsumerScriptDoesNothing` can check the consumer's own words
        // rather than the test harness's.
        // The inspection script reads the project at execution time, so it is not
        // configuration-cache compatible; a fixture that tests the configuration cache omits it.
        val inspection = if (includeInspection) {
            projectDir.resolve("inspection.gradle").writeText(INSPECTION_SCRIPT)
            "apply(from = \"inspection.gradle\")"
        } else {
            ""
        }
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

            $inspection
            """.trimIndent(),
        )
    }
    /**
     * A dependency the consumer declares itself, used to reproduce a leak the plugin does not cause.
     * The fixture's own build script still names the runtime nowhere.
     */
    private fun consumerDependency(coordinate: String): String = """
        dependencies {
            add("commonMainImplementation", "$coordinate")
        }
    """.trimIndent()

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
                import kotlin.js.JsExport

                // An exported declaration is what a Kotlin/JS library hands to the host; unexported
                // code is eliminated from the module, so a consumer's host-facing surface is its
                // exports. Only compiles when miniappMain can see commonMain.
                @JsExport
                fun miniAppGreeting(): String = "miniapp:" + sharedGreeting()

                // Compiles only when the plugin wired the runtime SDK into miniappMain.
                @JsExport
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

    /**
     * Runs the boundary check against a consumer that declares [coordinate] itself — a leak the
     * plugin does not cause — and returns the failing build's output.
     *
     * The check alone, deliberately: reaching the assembly task would run the npm install behind the
     * Kotlin/JS distribution, and a network hiccup there must not turn a boundary assertion into a
     * resolution failure.
     */
    private fun boundaryCheckFailure(coordinate: String): String {
        val projectDir = newFixture("kmp-renderer-leak")
        writeConsumerBuildScript(projectDir, consumerEpilogue = consumerDependency(coordinate))
        writeConsumerSources(projectDir)

        val result = runner(projectDir, MiniAppPluginDiagnostics.CHECK_MINIAPP_HOST_BOUNDARY_TASK_NAME)
            .buildAndFail()

        assertEquals(
            TaskOutcome.FAILED,
            result.task(":${MiniAppPluginDiagnostics.CHECK_MINIAPP_HOST_BOUNDARY_TASK_NAME}")?.outcome,
        )
        return result.output
    }

    /**
     * The module coordinates the boundary check listed, read from the lines it prints beneath its
     * heading. Reading the report is what makes the assertion about *which* dependency was rejected
     * rather than about the build merely failing.
     */
    private fun offendersIn(output: String): List<String> = output.lineSequence()
        .map { it.trim() }
        .filter { it.startsWith("- ") }
        .map { it.removePrefix("- ").trim() }
        .toList()

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
        /** What the host-boundary check reads, so a passing check can be shown to be non-vacuous. */
        val runtimeClasspath: List<String> = valuesAfter("miniapp.runtimeClasspath=")

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
                    println("miniapp.extensionType=" +
                        project.extensions.getByName("miniapp").getClass().name)
                    println(
                        "miniapp.bundleDependencies=" +
                            project.tasks.getByName("assembleMiniAppBundle")
                                .taskDependencies.getDependencies(null)
                                .collect { it.name }.sort()
                    )
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
                    println(
                        "miniapp.runtimeClasspath=" + project.configurations
                            .getByName("miniappRuntimeClasspath")
                            .incoming.resolutionResult.allComponents
                            .collect { it.id.displayName }
                            .sort()
                    )
                }
            }
        """.trimIndent()
    }
}
