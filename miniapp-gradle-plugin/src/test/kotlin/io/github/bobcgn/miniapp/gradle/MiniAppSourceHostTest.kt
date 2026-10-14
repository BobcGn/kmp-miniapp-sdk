package io.github.bobcgn.miniapp.gradle

import java.io.File
import kotlin.io.path.createTempDirectory
import kotlin.test.Test
import kotlin.test.assertContains
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import org.gradle.api.GradleException

/**
 * Unit coverage for the source host: what creating it writes, what it refuses to write, and where it
 * refuses to be created.
 *
 * Idempotency is asserted by content and by last-modified time rather than by "the task succeeded",
 * because a second run that rewrote identical text would pass the weaker check and still be the bug
 * this contract exists to prevent — a developer's edit silently replaced by the template.
 */
class MiniAppSourceHostTest {

    @Test
    fun `creating the source host writes every template`() {
        val source = newDirectory()
        val result = MiniAppSourceHost.initialize(source, "sample")

        assertEquals(MiniAppSourceHost.templateDestinations.sorted(), result.created.sorted())
        assertEquals(emptyList(), result.skipped)
        MiniAppSourceHost.templateDestinations.forEach { relativePath ->
            assertTrue(File(source, relativePath).isFile, "'$relativePath' was not created")
        }

        // The generated library area is deliberately not part of the template: an empty one would
        // make an unbuilt project look built.
        assertTrue(!File(source, "miniprogram/libs").exists())
    }

    @Test
    fun `a second run changes nothing`() {
        val source = newDirectory()
        MiniAppSourceHost.initialize(source, "sample")

        val before = snapshot(source)
        val result = MiniAppSourceHost.initialize(source, "sample")
        val after = snapshot(source)

        assertEquals(emptyList(), result.created)
        assertEquals(MiniAppSourceHost.templateDestinations.sorted(), result.skipped.sorted())
        assertEquals(before, after, "a repeated initialization must not rewrite a single file")
    }

    @Test
    fun `a file a developer edited is never restored`() {
        val source = newDirectory()
        MiniAppSourceHost.initialize(source, "sample")

        val page = File(source, "miniprogram/pages/index/index.js")
        val edited = "// edited by hand\nPage({ data: {}, onLoad() {} });\n"
        page.writeText(edited)
        val config = File(source, "miniprogram/jsconfig.json")
        val customConfig = """{"compilerOptions":{"allowJs":true},"include":["pages/**/*.js"]}"""
        config.writeText(customConfig)
        File(source, "project.config.json").delete()

        val result = MiniAppSourceHost.initialize(source, "sample")

        assertEquals(edited, page.readText(), "the template must not overwrite an edited file")
        assertEquals(customConfig, config.readText(), "initialization must preserve user type configuration")
        assertEquals(listOf("project.config.json"), result.created)
        assertTrue(result.skipped.contains("miniprogram/pages/index/index.js"))
    }

    @Test
    fun `a partial project is completed rather than restarted`() {
        val source = newDirectory()
        File(source, "miniprogram").mkdirs()
        File(source, "miniprogram/app.json").writeText("""{"pages": ["pages/custom/index"]}""")

        val result = MiniAppSourceHost.initialize(source, "sample")

        assertEquals("""{"pages": ["pages/custom/index"]}""", File(source, "miniprogram/app.json").readText())
        assertTrue(result.skipped.contains("miniprogram/app.json"))
        assertEquals(MiniAppSourceHost.templateDestinations.size - 1, result.created.size)
    }

    @Test
    fun `the template names the module and the project it was generated for`() {
        val source = newDirectory()
        MiniAppSourceHost.initialize(source, "sample")

        val page = File(source, "miniprogram/pages/index/index.js").readText()
        assertContains(page, "sample-miniapp.js")
        assertTrue(!page.contains("__MINIAPP_MODULE__"), "the module placeholder was left in the page")
        assertTrue(!page.contains("__MINIAPP_PROJECT__"), "the project placeholder was left in the page")

        assertContains(File(source, "miniprogram/app.json").readText(), "sample")
    }

    @Test
    fun `the generated library area is refused unless it is strictly inside the source host`() {
        val project = newDirectory()
        val source = File(project, "miniApp").apply { mkdirs() }

        // Strictly inside is the only accepted relationship, and the relative path it yields is what
        // the assembled project puts the library at.
        assertEquals(
            "miniprogram/libs",
            MiniAppPathDiagnostics.relativeGeneratedLibraryPath(File(source, "miniprogram/libs"), source),
        )

        listOf(
            source,
            project,
            File(project, "elsewhere/libs"),
        ).forEach { rejected ->
            val failure = runCatching {
                MiniAppPathDiagnostics.relativeGeneratedLibraryPath(rejected, source)
            }.exceptionOrNull()

            assertTrue(failure is GradleException, "$rejected must be refused as a generated area")
            assertContains(failure?.message.orEmpty(), MiniAppPathDiagnostics.GENERATED_LIBRARY_HEADER)
        }
    }

    @Test
    fun `the source host and the distribution are refused outside the project`() {
        val project = newDirectory()
        val outside = newDirectory()

        MiniAppPathDiagnostics.requireSourceInsideProject(File(project, "miniApp"), project)
        MiniAppPathDiagnostics.requireDistributionInsideProject(File(project, "build/miniapp-dist"), project)

        listOf(project, outside, File(project.parentFile, "sibling")).forEach { rejected ->
            val sourceFailure = runCatching {
                MiniAppPathDiagnostics.requireSourceInsideProject(rejected, project)
            }.exceptionOrNull()
            assertContains(sourceFailure?.message.orEmpty(), MiniAppPathDiagnostics.SOURCE_HEADER)

            val distributionFailure = runCatching {
                MiniAppPathDiagnostics.requireDistributionInsideProject(rejected, project)
            }.exceptionOrNull()
            assertContains(distributionFailure?.message.orEmpty(), MiniAppPathDiagnostics.DISTRIBUTION_HEADER)
        }
    }

    private fun newDirectory(): File = createTempDirectory("miniapp-source-host").toFile()

    /** Every file under [root] with its text and its last-modified time. */
    private fun snapshot(root: File): Map<String, Pair<String, Long>> = root.walkTopDown()
        .filter { it.isFile }
        .associate { file ->
            file.relativeTo(root).invariantSeparatorsPath to (file.readText() to file.lastModified())
        }
}
