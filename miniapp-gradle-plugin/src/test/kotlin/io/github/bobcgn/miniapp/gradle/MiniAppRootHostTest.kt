package io.github.bobcgn.miniapp.gradle

import org.gradle.testkit.runner.GradleRunner
import java.io.File
import java.util.Properties
import kotlin.io.path.createTempDirectory
import kotlin.test.Test
import kotlin.test.assertContains
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/** Multi-module consumer checks: root entry points, declared owner, safe writes and legacy defaults. */
class MiniAppRootHostTest {
    private val sdkRepository: String = checkNotNull(System.getProperty("miniappPlugin.sdkRepository"))
    private val kotlinVersion: String = checkNotNull(System.getProperty("miniappPlugin.kotlinVersion"))

    @Test
    fun `root host lifecycle preserves handwritten files and uses the real module name`() {
        val root = fixture()
        assertFalse(root.resolve("miniApp").exists())
        run(root, "help", "--configuration-cache").build()
        assertFalse(root.resolve("miniApp").exists(), "configuration must not initialize a host")
        run(root, "initMiniApp", "--configuration-cache").build()
        val source = root.resolve("miniApp")
        assertTrue(source.resolve("project.config.json").isFile)
        assertFalse(root.resolve("shared/miniApp").exists())
        val page = source.resolve("miniprogram/pages/index/index.js")
        assertContains(page.readText(), "root-host-fixture.js")
        val handwritten = page.readText() + "\n// developer-owned\n"
        page.writeText(handwritten)
        assertContains(run(root, "initMiniApp", "--configuration-cache").build().output, "createdCount=0")
        assertEquals(handwritten, page.readText())

        run(root, "syncMiniApp", "--configuration-cache").build()
        val libraries = source.resolve("miniprogram/libs")
        assertTrue(libraries.resolve("root-host-fixture.js").isFile)
        assertTrue(libraries.resolve("root-host-fixture.d.ts").isFile)
        libraries.resolve("stale.js").writeText("// stale output")
        // A new compilation changes the inputs, so the development sync really executes.
        root.resolve("shared/src/miniappMain/kotlin/consumer/Entry.kt")
            .appendText("\n@kotlin.js.JsExport\npublic fun changed(): String = \"updated\"\n")
        run(root, "syncMiniApp", "--configuration-cache").build()
        assertFalse(libraries.resolve("stale.js").exists())
        assertEquals(handwritten, page.readText())
        val verified = run(root, "verifyMiniApp", "--configuration-cache").build()
        assertContains(verified.output, "miniApp.verify.verified=true")
        val distribution = root.resolve("build/miniapp-dist")
        assertTrue(distribution.resolve("project.config.json").isFile)
        assertFalse(root.resolve("shared/build/miniapp-dist").exists())
        assertEquals(handwritten, page.readText())

        // Same export loaded from each host, without reading shared/build. Node is wiring evidence.
        for (host in listOf(source, distribution)) {
            val process = ProcessBuilder("node", "-e", "const m=require('./miniprogram/libs/root-host-fixture.js'); if(m.consumer.greeting()!=='Hello, WeChat!')process.exit(1)")
                .directory(host).redirectErrorStream(true).start()
            val output = process.inputStream.bufferedReader().readText()
            assertEquals(0, process.waitFor(), output)
        }
        run(root, "clean", "--configuration-cache").build()
        assertFalse(distribution.exists())
        assertEquals(handwritten, page.readText(), "root clean must preserve handwritten host")
    }

    @Test
    fun `a second root host owner is rejected before host initialization`() {
        val root = fixture()
        root.resolve("settings.gradle.kts").appendText("\ninclude(\":other\")\n")
        module(root, "other", "rootHost.set(true)")
        val failed = run(root, "initMiniApp").buildAndFail()
        assertContains(failed.output, "Only one module may set wechat.rootHost=true")
        assertFalse(root.resolve("miniApp").exists())
    }

    @Test
    fun `existing root tasks are not replaced`() {
        val root = fixture()
        root.resolve("build.gradle.kts").appendText("\ntasks.register(\"initMiniApp\")\n")
        assertContains(run(root, "help").buildAndFail().output, "Root Mini App task names are already owned")
        assertFalse(root.resolve("miniApp").exists())
    }

    @Test
    fun `root host rejects outside and overlapping paths without modifying source`() {
        val root = fixture()
        module(root, "shared", "rootHost.set(true)\nsourceDirectory.set(rootProject.layout.projectDirectory.dir(\"../outside-miniapp\"))")
        assertContains(run(root, "initMiniApp").buildAndFail().output, MiniAppPathDiagnostics.SOURCE_HEADER)
        assertFalse(root.resolve("miniApp").exists())
        module(root, "shared", "rootHost.set(true)\ndistributionDirectory.set(sourceDirectory)")
        assertContains(run(root, "initMiniApp").buildAndFail().output, "must not overlap")
        assertFalse(root.resolve("miniApp").exists())
        module(root, "shared", "rootHost.set(true)\nsourceDirectory.set(rootProject.layout.buildDirectory.dir(\"source-host\"))")
        assertContains(run(root, "initMiniApp").buildAndFail().output, "outside the root build directory")
        assertFalse(root.resolve("build/source-host").exists())
    }

    @Test
    fun `a generated area pointing at handwritten pages is rejected`() {
        val root = fixture()
        run(root, "initMiniApp").build()
        val page = root.resolve("miniApp/miniprogram/pages/index/index.wxml")
        val original = page.readText()
        module(root, "shared", "rootHost.set(true)\ngeneratedLibraryDirectory.set(sourceDirectory.dir(\"miniprogram/pages\"))")
        assertContains(run(root, "initMiniApp").buildAndFail().output, "contains handwritten host/source files")
        assertEquals(original, page.readText())
    }

    @Test
    fun `a module that does not opt in keeps its original host directory`() {
        val root = fixture("rootHost.set(false)")
        run(root, ":shared:initMiniApp").build()
        assertTrue(root.resolve("shared/miniApp/project.config.json").isFile)
        assertFalse(root.resolve("miniApp").exists())
    }

    private fun fixture(configuration: String = "rootHost.set(true)"): File {
        val root = createTempDirectory("miniapp-root-consumer").toFile()
        root.resolve("settings.gradle.kts").writeText("""
            pluginManagement { repositories { gradlePluginPortal(); mavenCentral() } }
            dependencyResolutionManagement { repositories { mavenCentral() } }
            includeBuild("${sdkRepository.replace("\\", "\\\\")}")
            rootProject.name = "root-consumer"
            include(":shared")
        """.trimIndent())
        root.resolve("build.gradle.kts").writeText("")
        module(root, "shared", configuration)
        root.resolve("shared/src/commonMain/kotlin/consumer/Shared.kt").apply {
            parentFile.mkdirs()
            writeText("package consumer\npublic fun sharedGreeting(): String = \"Hello, WeChat!\"\n")
        }
        root.resolve("shared/src/miniappMain/kotlin/consumer/Entry.kt").apply {
            parentFile.mkdirs()
            writeText("@file:OptIn(kotlin.js.ExperimentalJsExport::class)\npackage consumer\n@kotlin.js.JsExport\npublic fun greeting(): String = sharedGreeting()\n")
        }
        return root
    }

    private fun module(root: File, name: String, configuration: String) {
        root.resolve("$name/build.gradle.kts").apply {
            parentFile.mkdirs()
            writeText("""
                buildscript {
                    repositories { gradlePluginPortal(); mavenCentral() }
                    dependencies {
                        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:$kotlinVersion")
                        classpath(${pluginClasspath()})
                    }
                }
                apply(plugin = "org.jetbrains.kotlin.multiplatform")
                apply(plugin = "io.github.bobcgn.miniapp")
                configure<io.github.bobcgn.miniapp.gradle.MiniAppExtension> {
                    wechat { $configuration }
                }
                configure<org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension> {
                    (targets.getByName("miniapp") as org.jetbrains.kotlin.gradle.targets.js.dsl.KotlinJsTargetDsl)
                        .outputModuleName.set("root-host-fixture")
                }
            """.trimIndent() + "\n")
        }
    }

    private fun pluginClasspath(): String {
        val properties = Properties().apply {
            checkNotNull(MiniAppRootHostTest::class.java.classLoader.getResourceAsStream("plugin-under-test-metadata.properties")).use { load(it) }
        }
        return properties.getProperty("implementation-classpath").split(File.pathSeparator)
            .filter { it.isNotBlank() }.joinToString(prefix = "files(", postfix = ")") {
                "\"${it.replace("\\", "\\\\")}\""
            }
    }

    private fun run(root: File, vararg arguments: String): GradleRunner = GradleRunner.create()
        .withProjectDir(root).withArguments(*arguments, "--console=plain", "--stacktrace")
}
