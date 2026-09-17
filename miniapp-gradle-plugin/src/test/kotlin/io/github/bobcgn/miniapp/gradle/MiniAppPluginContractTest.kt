package io.github.bobcgn.miniapp.gradle

import org.gradle.api.Plugin
import org.gradle.api.Project
import java.io.File
import java.lang.reflect.ParameterizedType
import java.util.Properties
import java.util.jar.JarFile
import kotlin.test.Test
import kotlin.test.assertEquals
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
    fun `the plugin ships no WeChat runtime classes`() {
        val offenders = pluginOutputEntries().filter { entry ->
            val lower = entry.lowercase()
            lower.contains("wechat") || lower.split('/').any { it == "wx" }
        }

        assertTrue(
            offenders.isEmpty(),
            "the plugin output must not contain WeChat runtime classes, found: $offenders",
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
}
