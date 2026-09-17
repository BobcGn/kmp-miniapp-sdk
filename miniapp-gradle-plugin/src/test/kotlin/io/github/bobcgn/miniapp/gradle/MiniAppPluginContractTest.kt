package io.github.bobcgn.miniapp.gradle

import org.gradle.api.GradleException
import org.gradle.api.Plugin
import org.gradle.api.Project
import java.io.File
import java.lang.reflect.ParameterizedType
import java.util.Properties
import kotlin.io.path.createTempDirectory
import java.util.jar.JarFile
import kotlin.test.Test
import kotlin.test.assertContains
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * Contract and responsibility-boundary checks for the BOB-78 skeleton.
 *
 * The boundary checks exist because the plugin owns build integration only: it must not ship, or be
 * able to load, the WeChat runtime. Wiring the runtime SDK in is BOB-82's responsibility and is
 * expected to change nothing here.
 */
class MiniAppPluginContractTest {

    @Test
    fun `the plugin id resolves to the documented implementation class`() {
        val descriptorName =
            "META-INF/gradle-plugins/${MiniAppPluginDiagnostics.PLUGIN_ID}.properties"
        val descriptor = checkNotNull(
            MiniAppGradlePlugin::class.java.classLoader.getResourceAsStream(descriptorName),
        ) { "plugin descriptor '$descriptorName' is missing from the plugin classpath" }

        val properties = Properties().apply { descriptor.use { load(it) } }

        assertEquals(
            "io.github.bobcgn.miniapp.gradle.MiniAppGradlePlugin",
            properties.getProperty("implementation-class"),
        )
    }

    @Test
    fun `the implementation class is a Gradle plugin for Project`() {
        val pluginInterface = MiniAppGradlePlugin::class.java.genericInterfaces
            .filterIsInstance<ParameterizedType>()
            .single { it.rawType == Plugin::class.java }

        assertEquals(Project::class.java, pluginInterface.actualTypeArguments.single())
    }

    @Test
    fun `the host boundary rejects renderers without rejecting shared runtime`() {
        listOf(
            "org.jetbrains.compose" to "runtime",
            "org.jetbrains.compose.ui" to "ui",
            "androidx.compose.ui" to "ui",
            "org.jetbrains.androidx.lifecycle" to "lifecycle-runtime-compose",
            "org.jetbrains.androidx.lifecycle" to "lifecycle-runtime-compose-js",
            "org.jetbrains.androidx.lifecycle" to "lifecycle-viewmodel-compose",
            "org.jetbrains.androidx.savedstate" to "savedstate-compose",
            "org.jetbrains.androidx.navigationevent" to "navigationevent-compose",
            "org.jetbrains.skiko" to "skiko",
            "org.jetbrains.kotlinx" to "kotlinx-browser",
            "org.jetbrains.kotlinx" to "kotlinx-html-js",
        ).forEach { (group, name) ->
            assertTrue(
                MiniAppHostBoundary.isForbiddenModule(group, name),
                "$group:$name belongs to a client renderer and must be rejected",
            )
        }

        // The runtime a Mini App legitimately carries must survive the same classifier.
        listOf(
            "org.jetbrains.kotlinx" to "kotlinx-coroutines-core",
            "org.jetbrains.kotlinx" to "atomicfu",
            "org.jetbrains.kotlin" to "kotlin-stdlib",
            "org.jetbrains.kotlin" to "kotlin-test",
            "org.jetbrains.kotlin" to "kotlin-dom-api-compat",
            "org.jetbrains.androidx.lifecycle" to "lifecycle-common",
            "org.jetbrains.androidx.lifecycle" to "lifecycle-runtime",
            "org.jetbrains.androidx.lifecycle" to "lifecycle-viewmodel",
            "org.jetbrains.androidx.lifecycle" to "lifecycle-viewmodel-savedstate",
            "org.jetbrains.androidx.savedstate" to "savedstate",
            "org.jetbrains.androidx.navigationevent" to "navigationevent",
        ).forEach { (group, name) ->
            assertTrue(
                !MiniAppHostBoundary.isForbiddenModule(group, name),
                "$group:$name is legitimate shared runtime and must be allowed",
            )
        }
    }

    @Test
    fun `the extension models WeChat as a host rather than as the platform`() {
        assertEquals("miniapp", MiniAppExtension.NAME)
        assertEquals("wechat", WeChatHostConfiguration.NAME)
        assertTrue(
            MiniAppHostConfiguration::class.java.isAssignableFrom(WeChatHostConfiguration::class.java),
            "WeChat must be a host configuration",
        )
        assertTrue(
            !MiniAppHostConfiguration::class.java.isAssignableFrom(MiniAppExtension::class.java),
            "the platform extension must not itself be a host configuration",
        )
    }

    @Test
    fun `the bundle directory must stay inside the project`() {
        val project = createTempDirectory("miniapp-bundle-validation").toFile()
        project.resolve("build").mkdirs()

        // Inside the project is allowed, including a host folder of the consumer's own.
        MiniAppBundleDirectoryDiagnostics.requireInsideProject(project.resolve("build/miniapp/bundle"), project)
        MiniAppBundleDirectoryDiagnostics.requireInsideProject(project.resolve("wechat/libs"), project)

        // The project directory itself and anything outside it are not: Sync would delete them.
        listOf(project, project.parentFile, project.parentFile.resolve("elsewhere")).forEach { rejected ->
            val failure = try {
                MiniAppBundleDirectoryDiagnostics.requireInsideProject(rejected, project)
                null
            } catch (exception: GradleException) {
                exception
            }
            assertNotNull(failure, "$rejected must be rejected as a bundle directory")
            assertContains(failure.message!!, MiniAppBundleDirectoryDiagnostics.MESSAGE_HEADER)
        }
    }

    @Test
    fun `the runtime coordinate is the decided public artifact id`() {
        // The one place the public coordinate is written outside the plugin's build script, and it
        // is a compatibility contract: consumers resolve exactly this module, and composite-build
        // substitution matches on it. The version is deliberately not pinned here — it comes from
        // the version catalog.
        assertEquals(
            "io.github.bobcgn:kmp-miniapp-sdk",
            "${MiniAppRuntimeDependency.group}:${MiniAppRuntimeDependency.name}",
        )
    }

    @Test
    fun `the plugin ships only its own Gradle integration classes`() {
        // The runtime SDK and the WeChat host are dependencies a consumer resolves, not code this
        // plugin carries. Anything outside the plugin's own package, the metadata resource it
        // generates, and META-INF means a runtime has been shaded into the plugin.
        val unexpected = pluginOutputEntries().filterNot { entry ->
            entry.startsWith("META-INF/") ||
                entry == PLUGIN_METADATA_RESOURCE ||
                PLUGIN_CLASS_ENTRY.matches(entry)
        }

        assertTrue(
            unexpected.isEmpty(),
            "the plugin output must contain only its own Gradle integration, found: $unexpected",
        )
    }

    @Test
    fun `the WeChat host runtime is not on the plugin classpath`() {
        val loaded = try {
            Class.forName("io.github.bobcgn.miniapp.host.wechat.WechatHost", false, javaClass.classLoader)
        } catch (_: ClassNotFoundException) {
            null
        }

        assertNull(loaded, "the plugin classpath must not contain the WeChat host runtime")
    }

    @Test
    fun `the runtime SDK is not on the plugin classpath`() {
        // The plugin adds the runtime to a consumer's compilation; it never links against it. The
        // only SDK class it could reach is the one it wires in, and that must not be here.
        val loaded = try {
            Class.forName("io.github.bobcgn.miniapp.api.MiniAppSdk", false, javaClass.classLoader)
        } catch (_: ClassNotFoundException) {
            null
        }

        assertNull(loaded, "the plugin classpath must not contain the runtime SDK")
    }

    private fun pluginOutputEntries(): List<String> {
        val location = File(
            MiniAppGradlePlugin::class.java.protectionDomain.codeSource.location.toURI(),
        )
        return if (location.isDirectory) {
            location.walkTopDown()
                .filter { it.isFile }
                .map { it.relativeTo(location).invariantSeparatorsPath }
                .toList()
        } else {
            JarFile(location).use { jar -> jar.entries().asSequence().map { it.name }.toList() }
        }
    }

    private companion object {
        /** Generated by the plugin build so the runtime version has exactly one source. */
        const val PLUGIN_METADATA_RESOURCE: String = "miniapp-plugin-metadata.properties"

        val PLUGIN_CLASS_ENTRY: Regex = Regex("""io/github/bobcgn/miniapp/gradle/[^/]+\.class""")
    }
}
