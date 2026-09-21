package io.github.bobcgn.miniapp.gradle

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
 * Gradle TestKit coverage for the 0.1.1 distribution pipeline: `initMiniApp`, `syncMiniApp`,
 * `assembleMiniApp` and `verifyMiniApp`.
 *
 * The split with the unit suites is deliberate. [MiniAppSourceHostTest] and
 * [MiniAppDistributionVerificationTest] own the *rules* — what a template writes, what a verifier
 * rejects — because those are pure functions over a directory and a broken `require()` is a five-line
 * file there rather than a Kotlin/JS compile here. This class owns the *wiring*: that the tasks exist
 * under the documented names, that they are connected the way the contract says, that the filesystem
 * after a real run has the shape a developer depends on, and that nothing a `Sync` owns leaks into
 * something a developer owns.
 *
 * The fixtures put the Kotlin Gradle Plugin and the plugin under test on the same buildscript
 * classpath, exactly as a consumer build does, and resolve the runtime SDK through a composite build
 * of this repository.
 */
class MiniAppDistributionTest {

    private val kotlinVersion: String = checkNotNull(System.getProperty("miniappPlugin.kotlinVersion")) {
        "miniappPlugin.kotlinVersion is not set; miniapp-gradle-plugin/build.gradle.kts sets it."
    }

    private val sdkRepository: File = File(
        checkNotNull(System.getProperty("miniappPlugin.sdkRepository")) {
            "miniappPlugin.sdkRepository is not set; miniapp-gradle-plugin/build.gradle.kts sets it."
        },
    )

    // --- initMiniApp ------------------------------------------------------------------------------

    @Test
    fun `initMiniApp creates the source host and a second run changes nothing`() {
        val projectDir = newFixture("distribution-init")
        writeConsumerBuildScript(projectDir)

        assertTrue(!projectDir.resolve("miniApp").exists(), "nothing may exist before the task runs")

        val first = runner(projectDir, "initMiniApp").build()
        assertContains(first.output, "miniApp.init.createdCount=${MiniAppSourceHost.templates.size}")
        assertContains(first.output, "miniApp.init.skippedCount=0")

        val host = projectDir.resolve("miniApp")
        MiniAppSourceHost.templateDestinations.forEach { relativePath ->
            assertTrue(host.resolve(relativePath).isFile, "initMiniApp did not create $relativePath")
        }
        // An empty generated area would make an unbuilt project look built.
        assertTrue(!host.resolve("miniprogram/libs").exists())

        val before = snapshot(host)
        val second = runner(projectDir, "initMiniApp").build()

        assertContains(second.output, "miniApp.init.createdCount=0")
        assertContains(second.output, "miniApp.init.skippedCount=${MiniAppSourceHost.templates.size}")
        assertEquals(before, snapshot(host), "a repeated initMiniApp must not rewrite a single file")

        // The plugin's own wiring is untouched: creating a source host is all this task does.
        assertTrue(projectDir.resolve("build.gradle.kts").readText().contains("io.github.bobcgn.miniapp"))
    }

    @Test
    fun `initMiniApp completes a partial project and never restores an edited file`() {
        val projectDir = newFixture("distribution-init-partial")
        writeConsumerBuildScript(projectDir)
        runner(projectDir, "initMiniApp").build()

        val host = projectDir.resolve("miniApp")
        val page = host.resolve("miniprogram/pages/index/index.js")
        val edited = "// edited by hand\nPage({ data: {}, onLoad() {} });\n"
        page.writeText(edited)
        host.resolve("project.config.json").delete()

        val result = runner(projectDir, "initMiniApp").build()

        assertEquals(edited, page.readText(), "the template must not overwrite an edited file")
        assertContains(result.output, "miniApp.init.created=project.config.json")
        assertContains(result.output, "miniApp.init.createdCount=1")
        assertContains(result.output, "miniApp.init.skipped=miniprogram/pages/index/index.js")
    }

    @Test
    fun `initMiniApp honours a configured source directory`() {
        val projectDir = newFixture("distribution-init-custom")
        writeConsumerBuildScript(
            projectDir,
            wechatConfiguration = listOf(
                "sourceDirectory.set(layout.projectDirectory.dir(\"wechatHost\"))",
            ),
        )

        runner(projectDir, "initMiniApp").build()

        assertTrue(projectDir.resolve("wechatHost/project.config.json").isFile)
        assertTrue(projectDir.resolve("wechatHost/miniprogram/app.json").isFile)
        assertTrue(!projectDir.resolve("miniApp").exists(), "the default must not also be created")
    }

    @Test
    fun `initMiniApp refuses a source directory outside the project`() {
        val projectDir = newFixture("distribution-init-unsafe")
        writeConsumerBuildScript(
            projectDir,
            wechatConfiguration = listOf(
                "sourceDirectory.set(layout.projectDirectory.dir(\"../\" + project.name + \"-elsewhere\"))",
            ),
        )

        val result = runner(projectDir, "initMiniApp").buildAndFail()

        assertContains(result.output, MiniAppPathDiagnostics.SOURCE_HEADER)
        assertTrue(
            !projectDir.parentFile.resolve("${projectDir.name}-elsewhere").exists(),
            "a rejected configuration must not have written anything",
        )
    }

    @Test
    fun `clean removes the build directory and leaves the source host alone`() {
        val projectDir = newFixture("distribution-clean")
        writeConsumerBuildScript(projectDir)
        runner(projectDir, "initMiniApp").build()

        val host = projectDir.resolve("miniApp")
        val page = host.resolve("miniprogram/pages/index/index.js")
        page.writeText("// edited by hand\nPage({ data: {}, onLoad() {} });\n")
        val before = snapshot(host)

        runner(projectDir, "clean").build()

        assertEquals(before, snapshot(host), "clean must not touch the source host")
        assertTrue(!projectDir.resolve("build").exists(), "clean must remove the build directory")
    }

    // --- the pipeline -----------------------------------------------------------------------------

    @Test
    fun `syncMiniApp fills the generated area, clears stale modules and is up to date afterwards`() {
        val projectDir = newFixture("distribution-sync")
        prepareSourceHost(projectDir)

        val host = projectDir.resolve("miniApp")
        val handwritten = host.resolve("miniprogram/pages/index/index.js")
        val edited = "// edited by hand\nconst consumer = require('../../libs/${projectDir.name}-miniapp.js').consumer;\n"
        handwritten.writeText(edited)
        val handwrittenFiles = snapshot(host.resolve("miniprogram/pages"))

        val sync = runner(projectDir, "syncMiniApp").build()
        assertEquals(TaskOutcome.SUCCESS, sync.task(":syncMiniApp")?.outcome)

        val library = host.resolve("miniprogram/libs")
        val modules = library.listFiles()!!.map { it.name }.toSet()
        assertContains(modules, "${projectDir.name}-miniapp.js")
        assertContains(modules, "${projectDir.name}-miniapp.d.ts")
        assertContains(modules, "package.json")
        assertContains(modules, "kmp-miniapp-sdk-kotlin.js")
        assertContains(modules, "kotlin-kotlin-stdlib.js")

        // A module the bundle no longer contains must leave the source host too, or the host keeps
        // loading code the build does not produce.
        //
        // The bundle has to really change for that to happen: `Sync` only deletes when it runs, and
        // Gradle correctly skips it when its inputs are unchanged. So the consumer's Kotlin changes
        // first — which is also the only way a module leaves a bundle in practice.
        library.resolve("stale-module.js").writeText("module.exports = {};\n")
        projectDir.resolve("src/miniappMain/kotlin/sample/MiniAppEntry.kt").appendText(
            "\n@JsExport\npublic fun afterTheChange(): String = \"changed\"\n",
        )
        runner(projectDir, "syncMiniApp").build()
        assertTrue(!library.resolve("stale-module.js").exists(), "a stale generated module survived a sync")
        assertContains(library.resolve("${projectDir.name}-miniapp.js").readText(), "afterTheChange")

        // Everything a developer wrote is exactly as they left it.
        assertEquals(edited, handwritten.readText())
        assertEquals(handwrittenFiles, snapshot(host.resolve("miniprogram/pages")))

        assertEquals(TaskOutcome.UP_TO_DATE, runner(projectDir, "syncMiniApp").build().task(":syncMiniApp")?.outcome)
    }

    @Test
    fun `syncMiniApp refuses a generated area that is not inside the source host`() {
        val projectDir = newFixture("distribution-sync-unsafe")
        writeConsumerBuildScript(
            projectDir,
            wechatConfiguration = listOf(
                "generatedLibraryDirectory.set(layout.projectDirectory.dir(\"elsewhere/libs\"))",
            ),
        )

        val result = runner(projectDir, "syncMiniApp").buildAndFail()

        assertContains(result.output, MiniAppPathDiagnostics.GENERATED_LIBRARY_HEADER)
        // Gradle creates a declared output directory before the task action runs, so the rejected
        // destination may exist as an empty directory. What the validation has to prevent is content:
        // nothing may be copied or deleted before the path has been accepted.
        val written = projectDir.resolve("elsewhere").walkTopDown().filter { it.isFile }.toList()
        assertEquals(emptyList(), written, "a rejected configuration must not have written any file")
    }

    @Test
    fun `assembleMiniApp injects the current bundle without a sync and excludes what is local`() {
        val projectDir = newFixture("distribution-assemble")
        prepareSourceHost(projectDir)

        val host = projectDir.resolve("miniApp")
        // Machine-local and IDE leftovers a real working copy accumulates.
        host.resolve("project.private.config.json").writeText("{}")
        host.resolve("node_modules/leftover").mkdirs()
        host.resolve("build/intermediate").mkdirs()
        host.resolve("stray.iml").writeText("")

        // No `syncMiniApp` in this invocation, and no generated area on disk: the distribution must
        // still carry the compiler's current output.
        runner(projectDir, "assembleMiniApp").build()

        val distribution = projectDir.resolve("build/miniapp-dist")
        val moduleName = "${projectDir.name}-miniapp"
        val files = distribution.walkTopDown().filter { it.isFile }
            .map { it.relativeTo(distribution).invariantSeparatorsPath }
            .toSet()

        assertContains(files, "project.config.json")
        assertContains(files, "miniprogram/app.json")
        assertContains(files, "miniprogram/pages/index/index.js")
        assertContains(files, "miniprogram/pages/index/index.wxml")
        assertContains(files, "miniprogram/libs/$moduleName.js")
        assertContains(files, "miniprogram/libs/$moduleName.d.ts")
        assertContains(files, "miniprogram/libs/package.json")
        assertContains(files, "miniprogram/libs/kmp-miniapp-sdk-kotlin.js")
        assertContains(files, "miniprogram/libs/kotlin-kotlin-stdlib.js")

        listOf(
            "project.private.config.json",
            "node_modules/leftover",
            "build/intermediate",
            "stray.iml",
        ).forEach { excluded ->
            assertTrue(
                files.none { it == excluded || it.startsWith("$excluded/") },
                "'$excluded' must not be shipped; the distribution carried ${files.filter { it.contains("private") }}",
            )
        }

        // The source host is read, never written.
        assertTrue(host.resolve("project.private.config.json").isFile)
        assertTrue(host.resolve("stray.iml").isFile)
        assertTrue(!host.resolve("miniprogram/libs").exists(), "assembleMiniApp must not write into the source host")

        assertEquals(
            TaskOutcome.UP_TO_DATE,
            runner(projectDir, "assembleMiniApp").build().task(":assembleMiniApp")?.outcome,
        )
    }

    @Test
    fun `assembleMiniApp fails with an actionable message when there is no source host`() {
        val projectDir = newFixture("distribution-assemble-missing")
        writeConsumerBuildScript(projectDir)

        val result = runner(projectDir, "assembleMiniApp").buildAndFail()

        assertContains(result.output, "The Mini App source host does not exist")
        assertContains(result.output, "./gradlew initMiniApp")
    }

    @Test
    fun `verifyMiniApp accepts the assembled distribution and reads nothing else`() {
        val projectDir = newFixture("distribution-verify")
        prepareSourceHost(projectDir)

        val result = runner(projectDir, "verifyMiniApp").build()
        val moduleName = "${projectDir.name}-miniapp"

        assertContains(result.output, "miniApp.verify.miniprogramRoot=miniprogram/")
        assertContains(result.output, "miniApp.verify.module=$moduleName")
        assertContains(result.output, "miniApp.verify.verified=true")
        // A verification that could be skipped is a verification of the previous run.
        assertEquals(TaskOutcome.SUCCESS, result.task(":verifyMiniApp")?.outcome)
        assertEquals(
            TaskOutcome.SUCCESS,
            runner(projectDir, "verifyMiniApp").build().task(":verifyMiniApp")?.outcome,
        )
    }

    @Test
    fun `verifyMiniApp rejects a distribution that cannot be loaded`() {
        val projectDir = newFixture("distribution-verify-broken")
        prepareSourceHost(projectDir)

        // A page declared in app.json with no entry file: structurally the project looks complete, and
        // WeChat would fail to open it.
        projectDir.resolve("miniApp/miniprogram/pages/index/index.wxml").delete()

        val result = runner(projectDir, "verifyMiniApp").buildAndFail()

        assertContains(result.output, "page 'pages/index/index' has no '.wxml' file")
        assertContains(result.output, "The assembled Mini App distribution is not a complete, self-contained host project")
    }

    // --- the detached distribution -----------------------------------------------------------------

    /**
     * The distribution is copied somewhere outside this repository and used there.
     *
     * This is the whole claim of `assembleMiniApp` stated as an experiment: if the copy still loads
     * its module graph, and the script that loads it resolved only paths inside the copy, then the
     * distribution depends on nothing it left behind. It remains module-wiring evidence on Node —
     * WeChat Developer Tools is still the only host acceptance.
     */
    @Test
    fun `the assembled distribution works after being copied outside the repository`() {
        val projectDir = newFixture("distribution-detached")
        prepareSourceHost(projectDir)
        runner(projectDir, "assembleMiniApp", "verifyMiniApp").build()

        val detached = createTempDirectory("miniapp-detached").toFile()
        val distribution = projectDir.resolve("build/miniapp-dist")
        distribution.copyRecursively(detached, overwrite = true)

        // Nothing in the copy may name the project it came from.
        val absoluteReferences = detached.walkTopDown()
            .filter { it.isFile && it.extension == "js" }
            .filter { it.readText().contains(projectDir.canonicalPath) }
            .map { it.relativeTo(detached).invariantSeparatorsPath }
            .toList()
        assertEquals(emptyList(), absoluteReferences, "the copied distribution names its source project")

        val smoke = ProcessBuilder("node", "scripts/smoke.cjs")
            .directory(detached)
            .redirectErrorStream(true)
            .start()
        val output = smoke.inputStream.bufferedReader().readText()
        assertEquals(0, smoke.waitFor(), "the detached smoke failed:\n$output")
        assertContains(output, "fixture.result=PASS")
    }

    // --- fixtures ---------------------------------------------------------------------------------

    /**
     * A consumer whose source host has been created and given one handwritten script.
     *
     * The script is the fixture's own, not the plugin's: it is what proves a file a developer adds
     * inside the source host travels into the distribution, and it is what the detached test runs.
     */
    private fun prepareSourceHost(projectDir: File) {
        writeConsumerBuildScript(projectDir)
        runner(projectDir, "initMiniApp").build()

        val moduleName = "${projectDir.name}-miniapp"
        projectDir.resolve("miniApp/scripts/smoke.cjs").apply {
            parentFile.mkdirs()
            writeText(
                """
                // Loads the compiled module the way the mini program page does. Node only: this is
                // module-wiring evidence, not WeChat host acceptance.
                const path = require('path');

                const root = path.join(__dirname, '..');
                const consumer = require(path.join(root, 'miniprogram', 'libs', '$moduleName.js')).consumer;

                console.log('fixture.greeting=' + consumer.hostGreeting('WeChat'));
                console.log('fixture.counted=' + consumer.countUpTo(5));
                console.log('fixture.sdkVersion=' + consumer.sdkVersion());
                const expected = consumer.hostGreeting('WeChat') === 'Hello, WeChat, from commonMain';
                console.log('fixture.result=' + (expected ? 'PASS' : 'FAIL'));
                process.exit(expected ? 0 : 1);
                """.trimIndent() + "\n",
            )
        }

        projectDir.resolve("src/commonMain/kotlin/sample/Shared.kt").apply {
            parentFile.mkdirs()
            writeText("package consumer\n\npublic fun greeting(name: String): String = \"Hello, \$name, from commonMain\"\n")
        }
        projectDir.resolve("src/miniappMain/kotlin/sample/MiniAppEntry.kt").apply {
            parentFile.mkdirs()
            writeText(
                """
                package consumer

                import kotlin.js.JsExport

                @JsExport
                public fun hostGreeting(name: String): String = greeting(name)

                @JsExport
                public fun countUpTo(limit: Int): Int {
                    var value = 0
                    repeat(limit) { value += 1 }
                    return value
                }

                @JsExport
                public fun sdkVersion(): String = "1.0.0-fixture"
                """.trimIndent() + "\n",
            )
        }
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

            // The fixture consumes the Mini App runtime as a public module coordinate. Nothing is
            // published yet, so the coordinate resolves through this composite build.
            includeBuild("${sdkRepository.invariantSeparatorsPath}")

            // The temporary directory carries a random suffix, so the project name is taken from it
            // rather than from the prefix: the consumer's compiled module is named after this value,
            // and a test that predicted the wrong one would be asserting a name the build never had.
            rootProject.name = "${projectDir.name}"
            """.trimIndent(),
        )
        return projectDir
    }

    private fun writeConsumerBuildScript(
        projectDir: File,
        wechatConfiguration: List<String> = emptyList(),
    ) {
        val configuration = if (wechatConfiguration.isEmpty()) {
            ""
        } else {
            buildString {
                appendLine("configure<io.github.bobcgn.miniapp.gradle.MiniAppExtension> {")
                appendLine("    wechat {")
                wechatConfiguration.forEach { appendLine("        $it") }
                appendLine("    }")
                appendLine("}")
            }
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
            apply(plugin = "io.github.bobcgn.miniapp")

            $configuration
            """.trimIndent() + "\n",
        )
    }

    private fun snapshot(root: File): Map<String, Pair<String, Long>> = root.walkTopDown()
        .filter { it.isFile }
        .associate { file ->
            file.relativeTo(root).invariantSeparatorsPath to (file.readText() to file.lastModified())
        }

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
}
