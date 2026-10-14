package io.github.bobcgn.miniapp.gradle

import java.io.File
import java.util.Properties
import kotlin.io.path.createTempDirectory
import kotlin.test.Test
import kotlin.test.assertContains
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import org.gradle.testkit.runner.GradleRunner

/** Root selection is explicit, name-independent, and uses the existing compiler and host checks. */
class MiniAppRootProjectTest {
    private val sdkRepository: String = checkNotNull(System.getProperty("miniappPlugin.sdkRepository"))
    private val kotlinVersion: String = checkNotNull(System.getProperty("miniappPlugin.kotlinVersion"))

    @Test
    fun `an arbitrary logic module builds from root and reuses configuration cache`() {
        val root = fixture()
        run(root, "help", "--configuration-cache").build()
        assertFalse(root.resolve("src/miniappMain").exists())
        assertFalse(root.resolve("core").exists(), "a module name must never be inferred")
        assertFalse(root.resolve("app/miniApp").exists(), "configuration does not create host UI")
        assertTrue(root.resolve("domain/src/miniappTest/kotlin").isDirectory)
        run(root, "initMiniApp", "--configuration-cache").build()
        val source = root.resolve("app/miniApp")
        val page = source.resolve("miniprogram/pages/index/index.js")
        val handwritten = page.readText() + "\n// developer-owned\n"
        page.writeText(handwritten)
        val repeated = run(root, "initMiniApp", "--configuration-cache").build()
        assertContains(repeated.output, "Reusing configuration cache")
        assertEquals(handwritten, page.readText())
        val built = run(root, ":domain:miniappTest", ":domain:assembleMiniAppBundle", "syncMiniApp", "verifyMiniApp", "--configuration-cache").build()
        assertContains(built.output, "miniApp.verify.verified=true")
        assertTrue(root.resolve("domain/build/miniapp/bundle/package.json").isFile)
        assertFalse(root.resolve("build/miniapp/bundle").exists())
        assertTrue(root.resolve("domain/build/test-results/miniappNodeTest").walkTopDown().any { it.extension == "xml" && "tests=\"1\"" in it.readText() })
        val cached = run(root, ":domain:miniappTest", ":domain:assembleMiniAppBundle", "syncMiniApp", "verifyMiniApp", "--configuration-cache").build()
        assertContains(cached.output, "Reusing configuration cache")
        for (host in listOf(source, root.resolve("build/miniapp-dist"))) {
            val process = ProcessBuilder("node", "-e", "const m=require('./miniprogram/libs/root-selection-domain-miniapp.js'); if(m.consumer.greeting()!=='shared logic'||!m.consumer.sdkVersion())process.exit(1)")
                .directory(host).redirectErrorStream(true).start()
            val output = process.inputStream.bufferedReader().readText()
            assertEquals(0, process.waitFor(), output)
        }
        run(root, "clean", "--configuration-cache").build()
        assertTrue(page.isFile)
        assertFalse(root.resolve("build/miniapp-dist").exists())
    }

    @Test
    fun `an unrelated server module is neither selected nor modified`() {
        val root = fixture()
        root.resolve("settings.gradle.kts").appendText("\ninclude(\":server\")\n")
        root.resolve("server/build.gradle.kts").apply { parentFile.mkdirs(); writeText("plugins { java }\n") }
        val original = root.resolve("server/build.gradle.kts").readText()
        run(root, "initMiniApp").build()
        assertEquals(original, root.resolve("server/build.gradle.kts").readText())
        assertFalse(root.resolve("server/src/miniappMain").exists())
        assertTrue(root.resolve("app/miniApp/project.config.json").isFile)
    }

    @Test
    fun `a selected non KMP project is rejected without creating a host`() {
        val root = fixture(kmp = false)
        val output = run(root, "initMiniApp").buildAndFail().output
        assertContains(output, "Project ':domain' does not apply 'org.jetbrains.kotlin.multiplatform'")
        assertFalse(root.resolve("app/miniApp").exists())
    }

    @Test
    fun `a missing project is not silently created`() {
        val root = fixture()
        root.resolve("build.gradle.kts").writeText(root.resolve("build.gradle.kts").readText().replace("project(\":domain\")", "project(\":absent\")"))
        assertContains(run(root, "help").buildAndFail().output, "absent")
        assertFalse(root.resolve("absent").exists())
        assertFalse(root.resolve("app/miniApp").exists())
    }

    @Test
    fun `a root cannot select itself or mix coordinator and root KMP roles`() {
        val root = fixture()
        val original = root.resolve("build.gradle.kts").readText()
        root.resolve("build.gradle.kts").writeText(original.replace("project(\":domain\")", "project(\":\")"))
        assertContains(run(root, "help").buildAndFail().output, "must select an existing subproject")
        root.resolve("build.gradle.kts").writeText(original + "\napply(plugin = \"org.jetbrains.kotlin.multiplatform\")\n")
        assertContains(run(root, "help").buildAndFail().output, "must not also compile Kotlin Multiplatform")
        assertFalse(root.resolve("app/miniApp").exists())
    }

    @Test
    fun `root selected paths cannot be overridden silently in the logic module`() {
        val root = fixture()
        root.resolve("domain/build.gradle.kts").appendText("""
            configure<io.github.bobcgn.miniapp.gradle.MiniAppExtension> {
                wechat { sourceDirectory.set(layout.projectDirectory.dir("elsewhere")) }
            }
        """.trimIndent())
        assertContains(run(root, "help").buildAndFail().output, "cannot be changed")
        assertFalse(root.resolve("domain/elsewhere").exists())
    }

    @Test
    fun `root selection does not bypass renderer or path validation`() {
        val root = fixture()
        root.resolve("domain/build.gradle.kts").appendText("\ndependencies { add(\"commonMainImplementation\", \"org.jetbrains.compose.runtime:runtime:1.7.0\") }\n")
        val renderer = run(root, "syncMiniApp").buildAndFail().output
        assertContains(renderer, MiniAppHostBoundary.MESSAGE_HEADER)
        assertContains(renderer, "org.jetbrains.compose.runtime:runtime")
        assertFalse(root.resolve("app/miniApp").exists())
        root.resolve("build.gradle.kts").writeText(root.resolve("build.gradle.kts").readText().replace("app/miniApp", "../outside-host"))
        assertContains(run(root, "initMiniApp").buildAndFail().output, MiniAppPathDiagnostics.SOURCE_HEADER)
    }

    @Test
    fun `selection is root only and does not accept disabling root host`() {
        val root = fixture()
        val original = root.resolve("build.gradle.kts").readText()
        root.resolve("build.gradle.kts").writeText(original.replace("sourceDirectory.set", "rootHost.set(false)\nsourceDirectory.set"))
        assertContains(run(root, "help").buildAndFail().output, "requires wechat.rootHost=true")
        root.resolve("build.gradle.kts").writeText(original)
        root.resolve("domain/build.gradle.kts").appendText("\nconfigure<io.github.bobcgn.miniapp.gradle.MiniAppExtension> { logicProject.set(project(\":domain\")) }\n")
        assertContains(run(root, "help").buildAndFail().output, "may only be configured on the root project")
    }

    private fun fixture(kmp: Boolean = true): File {
        val root = createTempDirectory("miniapp-root-selection").toFile()
        root.resolve("settings.gradle.kts").writeText("""
            pluginManagement { repositories { gradlePluginPortal(); mavenCentral() } }
            dependencyResolutionManagement { repositories { mavenCentral() } }
            includeBuild("${sdkRepository.replace("\\", "\\\\")}")
            rootProject.name = "root-selection"
            include(":domain")
        """.trimIndent())
        root.resolve("build.gradle.kts").writeText("""
            buildscript {
                repositories { gradlePluginPortal(); mavenCentral() }
                dependencies {
                    classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:$kotlinVersion")
                    classpath(${pluginClasspath()})
                }
            }
            apply(plugin = "io.github.bobcgn.miniapp")
            configure<io.github.bobcgn.miniapp.gradle.MiniAppExtension> {
                logicProject.set(project(":domain"))
                wechat { sourceDirectory.set(layout.projectDirectory.dir("app/miniApp")) }
            }
        """.trimIndent())
        root.resolve("domain/build.gradle.kts").apply {
            parentFile.mkdirs()
            writeText(if (kmp) "apply(plugin = \"org.jetbrains.kotlin.multiplatform\")\ndependencies { add(\"commonTestImplementation\", \"org.jetbrains.kotlin:kotlin-test:$kotlinVersion\") }\n" else "plugins { java }\n")
        }
        root.resolve("domain/src/commonMain/kotlin/consumer/Logic.kt").apply {
            parentFile.mkdirs(); writeText("package consumer\npublic fun sharedGreeting(): String = \"shared logic\"\n")
        }
        root.resolve("domain/src/miniappMain/kotlin/consumer/Entry.kt").apply {
            parentFile.mkdirs()
            writeText("@file:OptIn(kotlin.js.ExperimentalJsExport::class)\npackage consumer\n@kotlin.js.JsExport public fun greeting(): String = sharedGreeting()\n@kotlin.js.JsExport public fun sdkVersion(): String = io.github.bobcgn.miniapp.api.MiniAppSdk.VERSION\n")
        }
        root.resolve("domain/src/miniappTest/kotlin/consumer/LogicTest.kt").apply {
            parentFile.mkdirs(); writeText("package consumer\nclass LogicTest { @kotlin.test.Test fun sharedLogicWorks() { kotlin.test.assertEquals(\"shared logic\", greeting()) } }\n")
        }
        return root
    }

    private fun pluginClasspath(): String {
        val properties = Properties().apply {
            checkNotNull(MiniAppRootProjectTest::class.java.classLoader.getResourceAsStream("plugin-under-test-metadata.properties")).use { load(it) }
        }
        return properties.getProperty("implementation-classpath").split(File.pathSeparator)
            .filter { it.isNotBlank() }.joinToString(prefix = "files(", postfix = ")") { "\"${it.replace("\\", "\\\\")}\"" }
    }

    private fun run(root: File, vararg arguments: String): GradleRunner = GradleRunner.create()
        .withProjectDir(root).withArguments(*arguments, "--console=plain", "--stacktrace")
}
